package com.nongthinh.rice_disease_diagnosis_service.application.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;

@Component
public class ImagePreprocessor {

    public PreprocessedImage preprocess(byte[] bytes, int inputWidth, int inputHeight, boolean nhwc, long maxPixels) {
        BufferedImage decoded = decodeRgb(bytes, maxPixels);
        int originalWidth = decoded.getWidth();
        int originalHeight = decoded.getHeight();
        float scale = Math.min((float) inputWidth / originalWidth, (float) inputHeight / originalHeight);
        int resizedWidth = Math.max(1, Math.round(originalWidth * scale));
        int resizedHeight = Math.max(1, Math.round(originalHeight * scale));
        int padX = (inputWidth - resizedWidth) / 2;
        int padY = (inputHeight - resizedHeight) / 2;

        BufferedImage letterboxed = new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = letterboxed.createGraphics();
        try {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, inputWidth, inputHeight);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(decoded, padX, padY, resizedWidth, resizedHeight, null);
        } finally {
            graphics.dispose();
        }
        return new PreprocessedImage(
                toFloatTensor(letterboxed, nhwc), originalWidth, originalHeight,
                inputWidth, inputHeight, scale, padX, padY, nhwc);
    }

    private BufferedImage decodeRgb(byte[] bytes, long maxPixels) {
        try {
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
            if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0
                    || (long) decoded.getWidth() * decoded.getHeight() > maxPixels) {
                throw new DiagnosisException(ErrorCode.INVALID_REQUEST);
            }
            decoded = normalizeExifOrientation(decoded, readExifOrientation(bytes));
            BufferedImage rgb = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rgb.createGraphics();
            try {
                graphics.drawImage(decoded, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            return rgb;
        } catch (IOException ex) {
            throw new DiagnosisException(ErrorCode.INVALID_REQUEST, ex);
        }
    }

    private BufferedImage normalizeExifOrientation(BufferedImage source, int orientation) {
        if (orientation == 1) return source;
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        boolean swapsDimensions = orientation >= 5 && orientation <= 8;
        BufferedImage normalized = new BufferedImage(
                swapsDimensions ? sourceHeight : sourceWidth,
                swapsDimensions ? sourceWidth : sourceHeight,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                int targetX;
                int targetY;
                switch (orientation) {
                    case 2 -> { targetX = sourceWidth - 1 - x; targetY = y; }
                    case 3 -> { targetX = sourceWidth - 1 - x; targetY = sourceHeight - 1 - y; }
                    case 4 -> { targetX = x; targetY = sourceHeight - 1 - y; }
                    case 5 -> { targetX = y; targetY = x; }
                    case 6 -> { targetX = sourceHeight - 1 - y; targetY = x; }
                    case 7 -> { targetX = sourceHeight - 1 - y; targetY = sourceWidth - 1 - x; }
                    case 8 -> { targetX = y; targetY = sourceWidth - 1 - x; }
                    default -> { targetX = x; targetY = y; }
                }
                normalized.setRGB(targetX, targetY, source.getRGB(x, y));
            }
        }
        return normalized;
    }

    private int readExifOrientation(byte[] bytes) {
        if (bytes == null || bytes.length < 14 || (bytes[0] & 0xff) != 0xff || (bytes[1] & 0xff) != 0xd8) {
            return 1;
        }
        int offset = 2;
        while (offset + 4 <= bytes.length && (bytes[offset] & 0xff) == 0xff) {
            int marker = bytes[offset + 1] & 0xff;
            if (marker == 0xd9 || marker == 0xda) return 1;
            int segmentLength = unsignedShort(bytes, offset + 2, false);
            if (segmentLength < 2 || offset + 2 + segmentLength > bytes.length) return 1;
            if (marker == 0xe1 && segmentLength >= 10
                    && bytes[offset + 4] == 'E' && bytes[offset + 5] == 'x'
                    && bytes[offset + 6] == 'i' && bytes[offset + 7] == 'f'
                    && bytes[offset + 8] == 0 && bytes[offset + 9] == 0) {
                return readTiffOrientation(bytes, offset + 10, offset + 2 + segmentLength);
            }
            offset += segmentLength + 2;
        }
        return 1;
    }

    private int readTiffOrientation(byte[] bytes, int tiffOffset, int endOffset) {
        if (tiffOffset + 8 > endOffset) return 1;
        boolean littleEndian;
        if (bytes[tiffOffset] == 'I' && bytes[tiffOffset + 1] == 'I') {
            littleEndian = true;
        } else if (bytes[tiffOffset] == 'M' && bytes[tiffOffset + 1] == 'M') {
            littleEndian = false;
        } else {
            return 1;
        }
        if (unsignedShort(bytes, tiffOffset + 2, littleEndian) != 42) return 1;
        long ifdOffset = unsignedInt(bytes, tiffOffset + 4, littleEndian);
        long ifdStart = tiffOffset + ifdOffset;
        if (ifdStart < tiffOffset || ifdStart + 2 > endOffset) return 1;
        int entryCount = unsignedShort(bytes, (int) ifdStart, littleEndian);
        for (int index = 0; index < entryCount; index++) {
            long entryOffset = ifdStart + 2L + index * 12L;
            if (entryOffset + 12 > endOffset) return 1;
            int entry = (int) entryOffset;
            if (unsignedShort(bytes, entry, littleEndian) == 0x0112
                    && unsignedShort(bytes, entry + 2, littleEndian) == 3
                    && unsignedInt(bytes, entry + 4, littleEndian) == 1) {
                int orientation = unsignedShort(bytes, entry + 8, littleEndian);
                return orientation >= 1 && orientation <= 8 ? orientation : 1;
            }
        }
        return 1;
    }

    private int unsignedShort(byte[] source, int offset, boolean littleEndian) {
        if (littleEndian) return (source[offset] & 0xff) | ((source[offset + 1] & 0xff) << 8);
        return ((source[offset] & 0xff) << 8) | (source[offset + 1] & 0xff);
    }

    private long unsignedInt(byte[] source, int offset, boolean littleEndian) {
        long first = unsignedShort(source, offset, littleEndian);
        long second = unsignedShort(source, offset + 2, littleEndian);
        return littleEndian ? first | (second << 16) : (first << 16) | second;
    }

    private float[] toFloatTensor(BufferedImage image, boolean nhwc) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] values = new float[width * height * 3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float red = ((rgb >> 16) & 0xff) / 255f;
                float green = ((rgb >> 8) & 0xff) / 255f;
                float blue = (rgb & 0xff) / 255f;
                if (nhwc) {
                    int offset = (y * width + x) * 3;
                    values[offset] = red;
                    values[offset + 1] = green;
                    values[offset + 2] = blue;
                } else {
                    int offset = y * width + x;
                    values[offset] = red;
                    values[width * height + offset] = green;
                    values[2 * width * height + offset] = blue;
                }
            }
        }
        return values;
    }

    public record PreprocessedImage(
            float[] values,
            int originalWidth,
            int originalHeight,
            int inputWidth,
            int inputHeight,
            float scale,
            int padX,
            int padY,
            boolean nhwc
    ) {
        public long[] tensorShape() {
            return nhwc
                    ? new long[] {1, inputHeight, inputWidth, 3}
                    : new long[] {1, 3, inputHeight, inputWidth};
        }
    }
}
