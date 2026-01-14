package at.klickmagiesoftware.epcqr.render;

import com.google.zxing.common.BitMatrix;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SvgRendererTest {

    @Test
    void shouldRenderValidSvg() {
        BitMatrix matrix = createSimpleMatrix(3);

        String svg = SvgRenderer.render(matrix);

        assertThat(svg).startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\"");
        assertThat(svg).endsWith("</svg>");
    }

    @Test
    void shouldIncludeViewBox() {
        BitMatrix matrix = createSimpleMatrix(5);

        String svg = SvgRenderer.render(matrix, 4);

        // 5 modules + 4 quiet zone on each side = 13 modules total
        // 13 * 4 pixels = 52 pixels
        assertThat(svg).contains("viewBox=\"0 0 52 52\"");
    }

    @Test
    void shouldIncludeWhiteBackground() {
        BitMatrix matrix = createSimpleMatrix(3);

        String svg = SvgRenderer.render(matrix);

        assertThat(svg).contains("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>");
    }

    @Test
    void shouldRenderBlackModules() {
        BitMatrix matrix = new BitMatrix(2);
        matrix.set(0, 0); // Set top-left module

        String svg = SvgRenderer.render(matrix, 4);

        // Quiet zone is 4 modules * 4 pixels = 16 pixels
        assertThat(svg).contains("x=\"16\" y=\"16\" width=\"4\" height=\"4\" fill=\"black\"");
    }

    @Test
    void shouldNotRenderWhiteModules() {
        BitMatrix matrix = new BitMatrix(2);
        // Don't set any modules

        String svg = SvgRenderer.render(matrix);

        // Should only have the white background rect, no black modules
        assertThat(svg).contains("fill=\"white\"");
        assertThat(svg).doesNotContain("fill=\"black\"");
    }

    @Test
    void shouldRespectModuleSize() {
        BitMatrix matrix = new BitMatrix(1);
        matrix.set(0, 0);

        String svg = SvgRenderer.render(matrix, 10);

        assertThat(svg).contains("width=\"10\" height=\"10\" fill=\"black\"");
    }

    @Test
    void shouldIncludeQuietZone() {
        BitMatrix matrix = createSimpleMatrix(5);

        String svg = SvgRenderer.render(matrix, 4);

        // With 4-module quiet zone and 4px per module, quiet zone = 16px
        // Total size: (5 + 8) * 4 = 52 pixels (5 modules + 4 quiet zone each side)
        assertThat(svg).contains("width=\"52\"");
        assertThat(svg).contains("height=\"52\"");
    }

    @Test
    void shouldRenderWithDefaultModuleSize() {
        BitMatrix matrix = createSimpleMatrix(5);

        String svgDefault = SvgRenderer.render(matrix);
        String svgExplicit = SvgRenderer.render(matrix, 4);

        assertThat(svgDefault).isEqualTo(svgExplicit);
    }

    @Test
    void shouldProduceWellFormedXml() {
        BitMatrix matrix = createSimpleMatrix(10);
        matrix.set(0, 0);
        matrix.set(5, 5);
        matrix.set(9, 9);

        String svg = SvgRenderer.render(matrix);

        // Count opening and closing tags
        int svgOpenCount = countOccurrences(svg, "<svg");
        int svgCloseCount = countOccurrences(svg, "</svg>");
        int rectOpenCount = countOccurrences(svg, "<rect");

        assertThat(svgOpenCount).isEqualTo(1);
        assertThat(svgCloseCount).isEqualTo(1);
        // At least one background rect plus 3 black modules
        assertThat(rectOpenCount).isGreaterThanOrEqualTo(4);
    }

    private BitMatrix createSimpleMatrix(int size) {
        return new BitMatrix(size);
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
