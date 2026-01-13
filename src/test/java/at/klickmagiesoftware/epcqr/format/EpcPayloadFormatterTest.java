package at.klickmagiesoftware.epcqr.format;

import at.klickmagiesoftware.epcqr.CharacterEncoding;
import at.klickmagiesoftware.epcqr.EpcQrCodeData;
import at.klickmagiesoftware.epcqr.EpcVersion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EpcPayloadFormatterTest {

    @Test
    void shouldFormatMinimalPayload() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                null, null, null, null, null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).isEqualTo("""
                BCD
                002
                1
                SCT

                Max Mustermann
                AT682011131032423628


                """);
    }

    @Test
    void shouldFormatPayloadWithBic() {
        var data = new EpcQrCodeData(
                EpcVersion.V001,
                CharacterEncoding.UTF_8,
                "GIBAATWW",
                "Max Mustermann",
                "AT682011131032423628",
                null, null, null, null, null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).contains("GIBAATWW\n");
    }

    @Test
    void shouldFormatPayloadWithAmount() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                new BigDecimal("1456.89"),
                null, null, null, null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).contains("EUR1456.89\n");
    }

    @Test
    void shouldFormatAmountWithoutTrailingZeros() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                new BigDecimal("100.00"),
                null, null, null, null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).contains("EUR100\n");
        assertThat(payload).doesNotContain("EUR100.00");
    }

    @Test
    void shouldFormatPayloadWithReference() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                null, null,
                "REF123456",
                null, null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).contains("REF123456\n");
    }

    @Test
    void shouldFormatPayloadWithText() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                null, null, null,
                "Invoice payment",
                null);

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).endsWith("Invoice payment");
    }

    @Test
    void shouldFormatPayloadWithDisplayText() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                null, null, null, null,
                "Display note");

        String payload = EpcPayloadFormatter.format(data);

        assertThat(payload).endsWith("Display note");
    }

    @Test
    void shouldFormatFullPayloadFromSpecExample1() {
        var data = new EpcQrCodeData(
                EpcVersion.V001,
                CharacterEncoding.UTF_8,
                "GIBAATWW",
                "Max Mustermann",
                "AT682011131032423628",
                new BigDecimal("1456.89"),
                null,
                "457845789452",
                "Diverse Autoteile, Re 789452 KN 457845",
                null);

        String payload = EpcPayloadFormatter.format(data);

        String expected = """
                BCD
                001
                1
                SCT
                GIBAATWW
                Max Mustermann
                AT682011131032423628
                EUR1456.89

                457845789452
                Diverse Autoteile, Re 789452 KN 457845""";

        assertThat(payload).isEqualTo(expected);
    }

    @Test
    void shouldCalculatePayloadBytes() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                "AT682011131032423628",
                null, null, null, null, null);

        int bytes = EpcPayloadFormatter.calculatePayloadBytes(data);

        assertThat(bytes).isPositive();
        assertThat(bytes).isLessThanOrEqualTo(331);
    }

    @Test
    void shouldHandleUmlautsInUtf8() {
        var data = new EpcQrCodeData(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Müller Österreich",
                "AT682011131032423628",
                null, null, null, null, null);

        int bytes = EpcPayloadFormatter.calculatePayloadBytes(data);

        // UTF-8 umlauts use 2 bytes each
        assertThat(bytes).isGreaterThan(data.receiverName().length());
    }
}
