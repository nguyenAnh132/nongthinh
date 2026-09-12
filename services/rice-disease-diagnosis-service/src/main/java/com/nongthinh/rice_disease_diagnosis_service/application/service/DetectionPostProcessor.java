package com.nongthinh.rice_disease_diagnosis_service.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.BoundingBox;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceDetection;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelClassManifest;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;

@Component
public class DetectionPostProcessor {

    private final DiagnosisProperties properties;

    public DetectionPostProcessor(DiagnosisProperties properties) {
        this.properties = properties;
    }

    public List<InferenceDetection> parse(
            Object output,
            ActiveModelDeployment deployment,
            ImagePreprocessor.PreprocessedImage image) {
        if (!(output instanceof float[][][] tensor) || tensor.length == 0) {
            return List.of();
        }
        List<ModelClassManifest> classes = deployment.classes();
        int expectedFeatures = 4 + classes.size();
        List<Candidate> candidates = new ArrayList<>();
        if (hasSupportedFeatureCount(tensor[0].length, expectedFeatures)) {
            int count = tensor[0][0].length;
            for (int candidate = 0; candidate < count; candidate++) {
                addCandidate(candidates, values(tensor[0], candidate), classes, image);
            }
        } else if (tensor[0].length > 0 && hasSupportedFeatureCount(tensor[0][0].length, expectedFeatures)) {
            for (float[] row : tensor[0]) {
                addCandidate(candidates, row, classes, image);
            }
        }
        return classAwareNms(candidates).stream().map(candidate -> new InferenceDetection(
                candidate.modelClass.classIndex(), candidate.modelClass.classCode(), candidate.modelClass.displayName(),
                candidate.modelClass.classKind(), candidate.confidence, candidate.boundingBox)).toList();
    }

    private void addCandidate(
            List<Candidate> target,
            float[] values,
            List<ModelClassManifest> classes,
            ImagePreprocessor.PreprocessedImage image) {
        if (values.length < 4 + classes.size()) return;
        boolean includesObjectness = values.length == 5 + classes.size();
        int classOffset = includesObjectness ? 5 : 4;
        float objectness = includesObjectness ? values[4] : 1f;
        int bestClass = -1;
        float confidence = 0;
        for (int index = 0; index < classes.size(); index++) {
            float classConfidence = objectness * values[classOffset + index];
            if (classConfidence > confidence) {
                confidence = classConfidence;
                bestClass = index;
            }
        }
        if (bestClass < 0 || confidence < properties.confidenceThreshold()) return;
        BoundingBox box = inverseLetterbox(values[0], values[1], values[2], values[3], image);
        if (box.area() <= 0) return;
        target.add(new Candidate(classes.get(bestClass), confidence, box));
    }

    private boolean hasSupportedFeatureCount(int actualFeatures, int expectedFeatures) {
        return actualFeatures == expectedFeatures || actualFeatures == expectedFeatures + 1;
    }

    private float[] values(float[][] featureMajor, int candidate) {
        float[] values = new float[featureMajor.length];
        for (int index = 0; index < featureMajor.length; index++) values[index] = featureMajor[index][candidate];
        return values;
    }

    private BoundingBox inverseLetterbox(
            float centerX, float centerY, float width, float height, ImagePreprocessor.PreprocessedImage image) {
        if (Math.abs(centerX) <= 1 && Math.abs(centerY) <= 1 && Math.abs(width) <= 1 && Math.abs(height) <= 1) {
            centerX *= image.inputWidth();
            centerY *= image.inputHeight();
            width *= image.inputWidth();
            height *= image.inputHeight();
        }
        float x = (centerX - width / 2f - image.padX()) / image.scale();
        float y = (centerY - height / 2f - image.padY()) / image.scale();
        float right = (centerX + width / 2f - image.padX()) / image.scale();
        float bottom = (centerY + height / 2f - image.padY()) / image.scale();
        x = clamp(x, 0, image.originalWidth());
        y = clamp(y, 0, image.originalHeight());
        right = clamp(right, 0, image.originalWidth());
        bottom = clamp(bottom, 0, image.originalHeight());
        return new BoundingBox(x, y, Math.max(0, right - x), Math.max(0, bottom - y));
    }

    private List<Candidate> classAwareNms(List<Candidate> candidates) {
        Map<Integer, List<Candidate>> byClass = new HashMap<>();
        for (Candidate candidate : candidates) {
            byClass.computeIfAbsent(candidate.modelClass.classIndex(), ignored -> new ArrayList<>()).add(candidate);
        }
        List<Candidate> kept = new ArrayList<>();
        for (List<Candidate> sameClass : byClass.values()) {
            sameClass.sort(Comparator.comparing(Candidate::confidence).reversed());
            List<Candidate> classKept = new ArrayList<>();
            for (Candidate candidate : sameClass) {
                if (classKept.stream().noneMatch(keptCandidate -> iou(candidate.boundingBox, keptCandidate.boundingBox)
                        >= properties.nmsIouThreshold())) {
                    classKept.add(candidate);
                }
            }
            kept.addAll(classKept);
        }
        return kept.stream().sorted(Comparator.comparing(Candidate::confidence).reversed()).toList();
    }

    private float iou(BoundingBox left, BoundingBox right) {
        float x = Math.max(left.x(), right.x());
        float y = Math.max(left.y(), right.y());
        float r = Math.min(left.x() + left.width(), right.x() + right.width());
        float b = Math.min(left.y() + left.height(), right.y() + right.height());
        float intersection = Math.max(0, r - x) * Math.max(0, b - y);
        float union = left.area() + right.area() - intersection;
        return union <= 0 ? 0 : intersection / union;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Candidate(ModelClassManifest modelClass, float confidence, BoundingBox boundingBox) {
    }
}
