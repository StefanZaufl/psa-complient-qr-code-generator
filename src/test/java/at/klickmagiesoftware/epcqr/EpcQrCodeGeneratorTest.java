package at.klickmagiesoftware.epcqr;

import at.klickmagiesoftware.epcqr.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpcQrCodeGeneratorTest {

    private EpcQrCodeGenerator generator;

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
