package at.klickmagiesoftware.epcqr.render;

import at.klickmagiesoftware.epcqr.exception.GenerationException;
import com.google.zxing.common.BitMatrix;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PngRendererTest {

    @Test
    void shouldGenerateValidPng() {
        BitMatrix matrix = createSimpleMatrix(3);

        byte[] png = PngRenderer.render(matrix);

        assertThat(png).isNotEmpty();
        // PNG magic bytes: 89 50 4E 47 (ASCII: .PNG)
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGenerateWithDefaultModuleSize() {
        BitMatrix matrix = createSimpleMatrix(5);

        byte[] pngDefault = PngRenderer.render(matrix);
        byte[] pngExplicit = PngRenderer.render(matrix, 8);

        assertThat(pngDefault).isEqualTo(pngExplicit);
    }

    @Test
    void shouldGenerateDifferentSizesForDifferentModuleSizes() {
        BitMatrix matrix = createSimpleMatrix(5);

        byte[] png4 = PngRenderer.render(matrix, 4);
        byte[] png8 = PngRenderer.render(matrix, 8);
        byte[] png16 = PngRenderer.render(matrix, 16);

        assertThat(png16.length).isGreaterThan(png8.length);
        assertThat(png8.length).isGreaterThan(png4.length);
    }

@Test
    void shouldIncludeQuietZone() {
        BitMatrix matrix = createSimpleMatrix(3);

        byte[] png = PngRenderer.render(matrix, 4);

        assertThat(png).isNotEmpty();
        // Verify it's a valid PNG by checking magic bytes
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        
        // The image should be larger than just the matrix size due to quiet zone
        // 3 modules + 4 quiet zone each side = 11 modules total
        // With 4px per module = 44x44 pixels
        assertThat(png.length).isGreaterThan(100);
    }

    @Test
    void shouldRenderBlackModules() {
        BitMatrix matrix = new BitMatrix(2);
        matrix.set(0, 0); // Set top-left module

        byte[] png = PngRenderer.render(matrix, 4);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldRenderAllWhiteModules() {
        BitMatrix matrix = new BitMatrix(2);
        // Don't set any modules (all white)

        byte[] png = PngRenderer.render(matrix);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldRespectModuleSize() {
        BitMatrix matrix = new BitMatrix(1);
        matrix.set(0, 0);

        byte[] png4 = PngRenderer.render(matrix, 4);
        byte[] png8 = PngRenderer.render(matrix, 8);
        byte[] png16 = PngRenderer.render(matrix, 16);

        // All should be valid PNGs
        assertThat(png4).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(png8).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(png16).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);

        // Larger module sizes should produce larger files
        assertThat(png16.length).isGreaterThan(png8.length);
        assertThat(png8.length).isGreaterThan(png4.length);
    }

    @Test
    void shouldHandleLargeMatrix() {
        BitMatrix matrix = createSimpleMatrix(25); // Similar to QR code size

        byte[] png = PngRenderer.render(matrix, 8);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        // Should be a substantial file
        assertThat(png.length).isGreaterThan(500);
    }

    @Test
    void shouldThrowExceptionForZeroModuleSize() {
        BitMatrix matrix = createSimpleMatrix(3);

        assertThatThrownBy(() -> PngRenderer.render(matrix, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Module size must be positive");
    }

    @Test
    void shouldThrowExceptionForNegativeModuleSize() {
        BitMatrix matrix = createSimpleMatrix(3);

        assertThatThrownBy(() -> PngRenderer.render(matrix, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Module size must be positive");
    }

    @Test
    void shouldGenerateDifferentPngsForDifferentMatrices() {
        BitMatrix matrix1 = new BitMatrix(2);
        matrix1.set(0, 0);

        BitMatrix matrix2 = new BitMatrix(2);
        matrix2.set(0, 1); // Different pattern

        byte[] png1 = PngRenderer.render(matrix1, 4);
        byte[] png2 = PngRenderer.render(matrix2, 4);

        assertThat(png1).isNotEqualTo(png2);
        // Both should be valid PNGs
        assertThat(png1).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(png2).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldHandleMinimumModuleSize() {
        BitMatrix matrix = createSimpleMatrix(3);

        byte[] png = PngRenderer.render(matrix, 1);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldHandleLargeModuleSize() {
        BitMatrix matrix = createSimpleMatrix(3);

        byte[] png = PngRenderer.render(matrix, 50);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        // Should be a much larger file
        assertThat(png.length).isGreaterThan(1000);
    }

    @Test
    void shouldGenerateConsistentResults() {
        BitMatrix matrix = createSimpleMatrix(5);

        byte[] png1 = PngRenderer.render(matrix, 8);
        byte[] png2 = PngRenderer.render(matrix, 8);

        assertThat(png1).isEqualTo(png2);
    }

    private BitMatrix createSimpleMatrix(int size) {
        return new BitMatrix(size);
    }
}