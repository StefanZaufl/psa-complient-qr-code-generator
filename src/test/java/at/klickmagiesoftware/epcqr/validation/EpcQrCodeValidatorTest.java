package at.klickmagiesoftware.epcqr.validation;

import at.klickmagiesoftware.epcqr.CharacterEncoding;
import at.klickmagiesoftware.epcqr.EpcVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class EpcQrCodeValidatorTest {

    private static final String VALID_IBAN = "AT682011131032423628";

    @Test
    void shouldRejectReceiverNameWithNewline() {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max\nMustermann",
                VALID_IBAN,
                null, null, null, null, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Receiver name must not contain linebreaks");
    }

    @Test
    void shouldRejectReceiverNameWithCarriageReturn() {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max\rMustermann",
                VALID_IBAN,
                null, null, null, null, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Receiver name must not contain linebreaks");
    }

    @Test
    void shouldRejectReceiverNameWithCRLF() {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max\r\nMustermann",
                VALID_IBAN,
                null, null, null, null, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Receiver name must not contain linebreaks");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUR\nE", "PU\rPE", "P\r\nRP"})
    void shouldRejectPurposeWithLinebreaks(String purpose) {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                VALID_IBAN,
                null, purpose, null, null, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Purpose must not contain linebreaks");
    }

    @ParameterizedTest
    @ValueSource(strings = {"REF\n123", "REF\r123", "REF\r\n123"})
    void shouldRejectReferenceWithLinebreaks(String reference) {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                VALID_IBAN,
                null, null, reference, null, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Reference must not contain linebreaks");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invoice\nPayment", "Invoice\rPayment", "Invoice\r\nPayment"})
    void shouldRejectTextWithLinebreaks(String text) {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                VALID_IBAN,
                null, null, null, text, null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Text must not contain linebreaks");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Display\nNote", "Display\rNote", "Display\r\nNote"})
    void shouldRejectDisplayTextWithLinebreaks(String displayText) {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                VALID_IBAN,
                null, null, null, null, displayText);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Display text must not contain linebreaks");
    }

    @Test
    void shouldAcceptValidDataWithoutLinebreaks() {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max Mustermann",
                VALID_IBAN,
                null, "SALA", "REF123", null, "Display note");

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldReportMultipleLinebreakErrors() {
        var result = EpcQrCodeValidator.validate(
                EpcVersion.V002,
                CharacterEncoding.UTF_8,
                null,
                "Max\nMustermann",
                VALID_IBAN,
                null, null, "REF\n123", null, "Display\nnote");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains(
                "Receiver name must not contain linebreaks",
                "Reference must not contain linebreaks",
                "Display text must not contain linebreaks");
    }
}
