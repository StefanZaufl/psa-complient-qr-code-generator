package at.klickmagiesoftware.epcqr.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IbanValidatorTest {

    @Test
    void shouldAcceptValidAustrianIban() {
        var result = IbanValidator.validate("AT682011131032423628");
        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldAcceptValidGermanIban() {
        var result = IbanValidator.validate("DE89370400440532013000");
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptIbanWithSpaces() {
        var result = IbanValidator.validate("AT68 2011 1310 3242 3628");
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptLowercaseIban() {
        var result = IbanValidator.validate("at682011131032423628");
        assertThat(result.valid()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldRejectNullOrEmptyIban(String iban) {
        var result = IbanValidator.validate(iban);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("IBAN is required");
    }

    @Test
    void shouldRejectIbanExceedingMaxLength() {
        String longIban = "AT6820111310324236281234567890123456";
        var result = IbanValidator.validate(longIban);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("exceeds maximum length");
    }

    @Test
    void shouldRejectIbanWithInvalidFormat() {
        var result = IbanValidator.validate("12345678901234567890");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("invalid format");
    }

    @Test
    void shouldRejectIbanWithInvalidChecksum() {
        var result = IbanValidator.validate("AT682011131032423629"); // Wrong check digit
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("invalid check digits");
    }

    @Test
    void shouldRejectIbanWithSpecialCharacters() {
        var result = IbanValidator.validate("AT68-2011-1310-3242-3628");
        assertThat(result.valid()).isFalse();
    }
}
