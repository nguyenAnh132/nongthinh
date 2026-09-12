package com.nongthinh.rice_disease_diagnosis_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImagePreprocessorTest {

    private final ImagePreprocessor preprocessor = new ImagePreprocessor();

    @Test
    void convertsRgbImageToNormalizedNchwTensorAndLetterboxesIt() throws Exception {
        BufferedImage source = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, Color.RED.getRGB());
        source.setRGB(1, 0, Color.GREEN.getRGB());

        ImagePreprocessor.PreprocessedImage image = preprocessor.preprocess(png(source), 4, 4, false, 100);

        assertThat(image.originalWidth()).isEqualTo(2);
        assertThat(image.originalHeight()).isEqualTo(1);
        assertThat(image.scale()).isEqualTo(2f);
        assertThat(image.padX()).isZero();
        assertThat(image.padY()).isEqualTo(1);
        assertThat(image.tensorShape()).containsExactly(1, 3, 4, 4);
        assertThat(image.values()[0]).isZero();
        assertThat(image.values()[4]).isEqualTo(1f);
        assertThat(image.values()[16 + 4]).isZero();
        assertThat(image.values()[32 + 4]).isZero();
    }

    @Test
    void normalizesExifOrientationBeforeCalculatingInputGeometry() throws Exception {
        BufferedImage source = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, Color.RED.getRGB());
        source.setRGB(1, 0, Color.GREEN.getRGB());

        ImagePreprocessor.PreprocessedImage image = preprocessor.preprocess(jpegWithOrientation(source, 6), 2, 2, true, 100);

        assertThat(image.originalWidth()).isEqualTo(1);
        assertThat(image.originalHeight()).isEqualTo(2);
        assertThat(image.tensorShape()).containsExactly(1, 2, 2, 3);
    }

    private byte[] png(BufferedImage image) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] jpegWithOrientation(BufferedImage image, int orientation) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", output);
        byte[] jpeg = output.toByteArray();
        byte[] exif = new byte[] {
                (byte) 0xff, (byte) 0xe1, 0, 34,
                'E', 'x', 'i', 'f', 0, 0,
                'M', 'M', 0, 42, 0, 0, 0, 8,
                0, 1,
                1, 18, 0, 3, 0, 0, 0, 1,
                0, (byte) orientation, 0, 0,
                0, 0, 0, 0
        };
        byte[] result = Arrays.copyOf(exif, exif.length + jpeg.length);
        result[0] = jpeg[0];
        result[1] = jpeg[1];
        System.arraycopy(exif, 0, result, 2, exif.length);
        System.arraycopy(jpeg, 2, result, exif.length + 2, jpeg.length - 2);
        return result;
    }
}
