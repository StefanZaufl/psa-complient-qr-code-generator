package at.klickmagiesoftware.epcqr.render;

import at.klickmagiesoftware.epcqr.exception.GenerationException;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Renders a ZXing BitMatrix as PNG bytes.
 *
 * <p>The generated PNG has the following characteristics:
 * <ul>
 *   <li>Each QR module is rendered as a square of pixels</li>
 *   <li>Includes proper quiet zone (4 modules) as required by QR code specification</li>
 *   <li>White background with black modules</li>
 *   <li>RGB color space for maximum compatibility</li>
 * </ul>
 */
public final class PngRenderer {

    private static final int DEFAULT_MODULE_SIZE = 8;
    private static final int QUIET_ZONE_MODULES = 4;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int BLACK_RGB = 0x000000;

    private PngRenderer() {
    }

    /**
     * Renders the BitMatrix as PNG bytes with default module size (8 pixels).
     *
     * @param matrix the QR code BitMatrix from ZXing
     * @return the PNG data as a byte array
     * @throws GenerationException if PNG generation fails
     */
    public static byte[] render(BitMatrix matrix) {
        return render(matrix, DEFAULT_MODULE_SIZE);
    }

    /**
     * Renders the BitMatrix as PNG bytes with the specified module size.
     *
     * @param matrix the QR code BitMatrix from ZXing
     * @param moduleSize the size of each module in pixels
     * @return the PNG data as a byte array
     * @throws GenerationException if PNG generation fails
     */
    public static byte[] render(BitMatrix matrix, int moduleSize) {
        if (moduleSize <= 0) {
            throw new IllegalArgumentException("Module size must be positive");
        }
        
        BufferedImage image = createBufferedImage(matrix, moduleSize);
        return writeAsPng(image);
    }

    private static BufferedImage createBufferedImage(BitMatrix matrix, int moduleSize) {
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();

        int quietZoneSize = QUIET_ZONE_MODULES * moduleSize;
        int imageWidth = (matrixWidth * moduleSize) + (2 * quietZoneSize);
        int imageHeight = (matrixHeight * moduleSize) + (2 * quietZoneSize);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        // Fill with white background
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                image.setRGB(x, y, WHITE_RGB);
            }
        }

        // Draw black modules
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (matrix.get(x, y)) {
                    int rectX = quietZoneSize + (x * moduleSize);
                    int rectY = quietZoneSize + (y * moduleSize);

                    for (int dy = 0; dy < moduleSize; dy++) {
                        for (int dx = 0; dx < moduleSize; dx++) {
                            image.setRGB(rectX + dx, rectY + dy, BLACK_RGB);
                        }
                    }
                }
            }
        }

        return image;
    }

    private static byte[] writeAsPng(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new GenerationException("Failed to write PNG image: " + e.getMessage(), e);
        }
    }
}