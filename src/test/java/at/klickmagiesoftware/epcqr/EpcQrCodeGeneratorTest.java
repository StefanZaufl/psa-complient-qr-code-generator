package at.klickmagiesoftware.epcqr;

import at.klickmagiesoftware.epcqr.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpcQrCodeGeneratorTest {

    private EpcQrCodeGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new EpcQrCodeGenerator();
    }

    @Test
    void shouldGenerateSvgForMinimalData() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\"");
        assertThat(svg).endsWith("</svg>");
        assertThat(svg).contains("viewBox");
        assertThat(svg).contains("fill=\"white\"");
        assertThat(svg).contains("fill=\"black\"");
    }

    @Test
    void shouldGenerateSvgWithCustomModuleSize() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        String svg4 = generator.generateSvg(data, 4);
        String svg8 = generator.generateSvg(data, 8);

        // Larger module size should result in larger SVG dimensions
        assertThat(extractSvgWidth(svg8)).isGreaterThan(extractSvgWidth(svg4));
    }

    @Test
    void shouldFormatPayload() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(100.00)
                .build();

        String payload = generator.formatPayload(data);

        assertThat(payload).startsWith("BCD\n002\n1\nSCT\n");
        assertThat(payload).contains("Max Mustermann");
        assertThat(payload).contains("AT682011131032423628");
        assertThat(payload).contains("EUR100");
    }

    @Test
    void shouldGenerateSpecificationExample1() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V001)
                .encoding(CharacterEncoding.UTF_8)
                .bic("GIBAATWW")
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).isNotEmpty();
        assertThat(svg).contains("<svg");
    }

    @Test
    void shouldGenerateSpecificationExample2() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .bic("GIBAATWW")
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).isNotEmpty();
    }

    @Test
    void shouldGenerateSpecificationExample3WithoutBic() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = CharacterEncoding.class, names = "ISO_8859_10", mode = EnumSource.Mode.EXCLUDE)
    void shouldGenerateSvgWithSupportedEncodings(CharacterEncoding encoding) {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(encoding)
                .receiverName("Test Receiver")
                .iban("AT682011131032423628")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).isNotEmpty();
        assertThat(svg).startsWith("<svg");
    }

    @Test
    void shouldGenerateSvgWithSpecialCharacters() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Müller & Söhne GmbH")
                .iban("AT682011131032423628")
                .text("Rechnung für März 2024 - Büromöbel")
                .build();

        String svg = generator.generateSvg(data);

        assertThat(svg).isNotEmpty();
    }

    @Test
    void shouldRejectPayloadExceedingMaxSize() {
        // Create data that exceeds 331 bytes using UTF-8 multi-byte characters
        // German umlauts like "ü" take 2 bytes each in UTF-8
        // So 70 "ü" chars = 140 bytes, 140 "ü" chars = 280 bytes
        // Total: 140 + 280 + 140 = 560 bytes (well over 331)
        String umlautChar = "ü";

        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName(umlautChar.repeat(70))  // 70 chars = 140 bytes in UTF-8
                .iban("AT682011131032423628")
                .text(umlautChar.repeat(140))         // 140 chars = 280 bytes in UTF-8
                .displayText(umlautChar.repeat(70))   // 70 chars = 140 bytes in UTF-8
                .build();

        assertThatThrownBy(() -> generator.generateSvg(data))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceeds maximum of 331 bytes");
    }

    @Test
    void shouldGenerateDifferentSvgsForDifferentData() {
        var data1 = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Receiver One")
                .iban("AT682011131032423628")
                .amount(100.00)
                .build();

        var data2 = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Receiver Two")
                .iban("AT682011131032423628")
                .amount(200.00)
                .build();

        String svg1 = generator.generateSvg(data1);
        String svg2 = generator.generateSvg(data2);

        // Different data should produce different QR codes
        assertThat(svg1).isNotEqualTo(svg2);
    }

    @Test
    void svgShouldBeWellFormedXml() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        String svg = generator.generateSvg(data);

        // Basic XML structure checks
        assertThat(countOccurrences(svg, "<svg")).isEqualTo(1);
        assertThat(countOccurrences(svg, "</svg>")).isEqualTo(1);
        assertThat(svg.indexOf("<svg")).isLessThan(svg.indexOf("</svg>"));
    }

    private int extractSvgWidth(String svg) {
        // Extract width from: width="123"
        int widthStart = svg.indexOf("width=\"") + 7;
        int widthEnd = svg.indexOf("\"", widthStart);
        return Integer.parseInt(svg.substring(widthStart, widthEnd));
    }

    @Test
    void shouldGeneratePngForMinimalData() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        // PNG magic bytes: 89 50 4E 47 (ASCII: .PNG)
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGeneratePngWithDefaultModuleSize() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        byte[] pngDefault = generator.generatePng(data);
        byte[] pngExplicit = generator.generatePng(data, 8);

        assertThat(pngDefault).isEqualTo(pngExplicit);
    }

    @Test
    void shouldGeneratePngWithCustomModuleSize() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        byte[] png4 = generator.generatePng(data, 4);
        byte[] png8 = generator.generatePng(data, 8);
        byte[] png16 = generator.generatePng(data, 16);

        // Larger module sizes should produce larger files
        assertThat(png16.length).isGreaterThan(png8.length);
        assertThat(png8.length).isGreaterThan(png4.length);
    }

    @Test
    void shouldGeneratePngFile() throws IOException {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(100.00)
                .build();

        Path pngPath = tempDir.resolve("test-qrcode.png");

        generator.generatePngFile(data, 8, pngPath);

        assertThat(Files.exists(pngPath)).isTrue();
        assertThat(Files.size(pngPath)).isGreaterThan(1000);
        
        byte[] fileContent = Files.readAllBytes(pngPath);
        assertThat(fileContent).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldCreateParentDirectoriesForPngFile() throws IOException {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        Path nestedPath = tempDir.resolve("nested").resolve("dir").resolve("qrcode.png");

        generator.generatePngFile(data, 8, nestedPath);

        assertThat(Files.exists(nestedPath)).isTrue();
        assertThat(Files.isDirectory(nestedPath.getParent())).isTrue();
        
        byte[] fileContent = Files.readAllBytes(nestedPath);
        assertThat(fileContent).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGenerateSpecificationExample1AsPng() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V001)
                .encoding(CharacterEncoding.UTF_8)
                .bic("GIBAATWW")
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGenerateSpecificationExample2AsPng() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .bic("GIBAATWW")
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGenerateSpecificationExample3WithoutBicAsPng() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .reference("457845789452")
                .text("Diverse Autoteile, Re 789452 KN 457845")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @ParameterizedTest
    @EnumSource(value = CharacterEncoding.class, names = "ISO_8859_10", mode = EnumSource.Mode.EXCLUDE)
    void shouldGeneratePngWithSupportedEncodings(CharacterEncoding encoding) {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(encoding)
                .receiverName("Test Receiver")
                .iban("AT682011131032423628")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldGeneratePngWithSpecialCharacters() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Müller & Söhne GmbH")
                .iban("AT682011131032423628")
                .text("Rechnung für März 2024 - Büromöbel")
                .build();

        byte[] png = generator.generatePng(data);

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
    }

    @Test
    void shouldRejectPngPayloadExceedingMaxSize() {
        String umlautChar = "ü";

        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName(umlautChar.repeat(70))
                .iban("AT682011131032423628")
                .text(umlautChar.repeat(140))
                .displayText(umlautChar.repeat(70))
                .build();

        assertThatThrownBy(() -> generator.generatePng(data))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceeds maximum of 331 bytes");
    }

    @Test
    void shouldGenerateDifferentPngsForDifferentData() {
        var data1 = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Receiver One")
                .iban("AT682011131032423628")
                .amount(100.00)
                .build();

        var data2 = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Receiver Two")
                .iban("AT682011131032423628")
                .amount(200.00)
                .build();

        byte[] png1 = generator.generatePng(data1);
        byte[] png2 = generator.generatePng(data2);

        // Different data should produce different QR codes
        assertThat(png1).isNotEqualTo(png2);
    }

    @Test
    void shouldGenerateConsistentPngResults() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Test Receiver")
                .iban("AT682011131032423628")
                .amount(50.00)
                .build();

        byte[] png1 = generator.generatePng(data, 8);
        byte[] png2 = generator.generatePng(data, 8);

        assertThat(png1).isEqualTo(png2);
    }

    @Test
    void shouldGeneratePngAndSvgWithSameData() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(100.00)
                .build();

        String svg = generator.generateSvg(data, 8);
        byte[] png = generator.generatePng(data, 8);

        // Both should be non-empty and valid
        assertThat(svg).isNotEmpty();
        assertThat(svg).startsWith("<svg");
        assertThat(svg).endsWith("</svg>");

        assertThat(png).isNotEmpty();
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
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
