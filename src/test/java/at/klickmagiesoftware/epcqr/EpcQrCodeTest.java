package at.klickmagiesoftware.epcqr;

import at.klickmagiesoftware.epcqr.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpcQrCodeTest {

    @Test
    void shouldBuildMinimalValidData() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .build();

        assertThat(data.version()).isEqualTo(EpcVersion.V002);
        assertThat(data.encoding()).isEqualTo(CharacterEncoding.UTF_8);
        assertThat(data.receiverName()).isEqualTo("Max Mustermann");
        assertThat(data.iban()).isEqualTo("AT682011131032423628");
        assertThat(data.hasBic()).isFalse();
        assertThat(data.hasAmount()).isFalse();
    }

    @Test
    void shouldBuildFullData() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V001)
                .encoding(CharacterEncoding.UTF_8)
                .bic("GIBAATWW")
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(new BigDecimal("1456.89"))
                .purpose("SALA")
                .reference("REF123")
                .displayText("Payment note")
                .build();

        assertThat(data.version()).isEqualTo(EpcVersion.V001);
        assertThat(data.bic()).isEqualTo("GIBAATWW");
        assertThat(data.amount()).isEqualTo(new BigDecimal("1456.89"));
        assertThat(data.purpose()).isEqualTo("SALA");
        assertThat(data.reference()).isEqualTo("REF123");
        assertThat(data.displayText()).isEqualTo("Payment note");
    }

    @Test
    void shouldAcceptDoubleAmount() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .amount(99.99)
                .build();

        assertThat(data.amount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void settingReferenceShouldClearText() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .text("Some text")
                .reference("REF123")
                .build();

        assertThat(data.hasReference()).isTrue();
        assertThat(data.hasText()).isFalse();
    }

    @Test
    void settingTextShouldClearReference() {
        var data = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .reference("REF123")
                .text("Some text")
                .build();

        assertThat(data.hasText()).isTrue();
        assertThat(data.hasReference()).isFalse();
    }

    @Test
    void shouldThrowValidationExceptionForMissingVersion() {
        var builder = EpcQrCode.builder()
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Version is required");
    }

    @Test
    void shouldThrowValidationExceptionForMissingEncoding() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("encoding is required");
    }

    @Test
    void shouldThrowValidationExceptionForMissingReceiverName() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Receiver name is required");
    }

    @Test
    void shouldThrowValidationExceptionForMissingIban() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("IBAN is required");
    }

    @Test
    void shouldThrowValidationExceptionForMissingBicInV001() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V001)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("BIC is mandatory for version 001");
    }

    @Test
    void shouldThrowValidationExceptionForInvalidIban() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("INVALID");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("IBAN");
    }

    @Test
    void shouldThrowValidationExceptionWithMultipleErrors() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V001)
                .encoding(CharacterEncoding.UTF_8);

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    var ve = (ValidationException) ex;
                    assertThat(ve.getErrors()).hasSizeGreaterThan(1);
                });
    }

    @Test
    void shouldThrowValidationExceptionForTooLongReceiverName() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("A".repeat(71))
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceeds maximum length of 70");
    }

    @Test
    void shouldThrowValidationExceptionForInvalidPurposeLength() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .purpose("AB"); // Should be exactly 4 characters

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Purpose code must be exactly 4 characters");
    }

    @Test
    void shouldThrowValidationExceptionForReceiverNameWithLinebreak() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max\nMustermann")
                .iban("AT682011131032423628");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Receiver name must not contain linebreaks");
    }

    @Test
    void shouldThrowValidationExceptionForTextWithLinebreak() {
        var builder = EpcQrCode.builder()
                .version(EpcVersion.V002)
                .encoding(CharacterEncoding.UTF_8)
                .receiverName("Max Mustermann")
                .iban("AT682011131032423628")
                .text("Invoice\nPayment");

        assertThatThrownBy(builder::build)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Text must not contain linebreaks");
    }
}
