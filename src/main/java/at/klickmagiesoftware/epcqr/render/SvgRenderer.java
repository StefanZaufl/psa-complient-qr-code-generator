package at.klickmagiesoftware.epcqr.render;

import com.google.zxing.common.BitMatrix;

/**
 * Renders a ZXing BitMatrix as an SVG string.
 *
 * <p>The generated SVG has the following characteristics:
 * <ul>
 *   <li>Each QR module is rendered as a {@code <rect>} element</li>
 *   <li>Includes proper quiet zone (4 modules) as required by QR code specification</li>
 *   <li>Uses viewBox for scalability</li>
 *   <li>White background with black modules</li>
 * </ul>
 */
public final class SvgRenderer {

    private static final int DEFAULT_MODULE_SIZE = 4;
    private static final int QUIET_ZONE_MODULES = 4;

    private SvgRenderer() {
    }

    /**
     * Renders the BitMatrix as an SVG string with default module size (4 pixels).
     *
     * @param matrix the QR code BitMatrix from ZXing
     * @return the SVG markup as a string
     */
    public static String render(BitMatrix matrix) {
        return render(matrix, DEFAULT_MODULE_SIZE);
    }

    /**
     * Renders the BitMatrix as an SVG string with the specified module size.
     *
     * @param matrix the QR code BitMatrix from ZXing
     * @param moduleSize the size of each module in pixels
     * @return the SVG markup as a string
     */
    public static String render(BitMatrix matrix, int moduleSize) {
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();

        int quietZoneSize = QUIET_ZONE_MODULES * moduleSize;
        int svgWidth = (matrixWidth * moduleSize) + (2 * quietZoneSize);
        int svgHeight = (matrixHeight * moduleSize) + (2 * quietZoneSize);

        StringBuilder svg = new StringBuilder();

        // SVG header with viewBox for scalability
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
                .append("viewBox=\"0 0 ").append(svgWidth).append(" ").append(svgHeight).append("\" ")
                .append("width=\"").append(svgWidth).append("\" ")
                .append("height=\"").append(svgHeight).append("\">\n");

        // White background
        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n");

        // Render black modules
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (matrix.get(x, y)) {
                    int rectX = quietZoneSize + (x * moduleSize);
                    int rectY = quietZoneSize + (y * moduleSize);
                    svg.append("  <rect x=\"").append(rectX)
                            .append("\" y=\"").append(rectY)
                            .append("\" width=\"").append(moduleSize)
                            .append("\" height=\"").append(moduleSize)
                            .append("\" fill=\"black\"/>\n");
                }
            }
        }

        svg.append("</svg>");

        return svg.toString();
    }
}
