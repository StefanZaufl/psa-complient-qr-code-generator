package at.klickmagiesoftware.epcqr.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BicValidatorTest {

    @Test
    void shouldAcceptValid8CharacterBic() {
        var result = BicValidator.validate("GIBAATWW", false);
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptValid11CharacterBic() {
        var result = BicValidator.validate("GIBAATWWXXX", false);
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptLowercaseBic() {
        var result = BicValidator.validate("gibaatww", false);
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptBicWithSpaces() {
        var result = BicValidator.validate("GIBA ATWW", false);
        assertThat(result.valid()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldAcceptEmptyBicWhenOptional(String bic) {
        var result = BicValidator.validate(bic, false);
        assertThat(result.valid()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldRejectEmptyBicWhenMandatory(String bic) {
        var result = BicValidator.validate(bic, true);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("BIC is mandatory for version 001");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GIBA", "GIBAAT", "GIBAATW", "GIBAATWWXXXY"})
    void shouldRejectBicWithWrongLength(String bic) {
        var result = BicValidator.validate(bic, false);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("8 or 11 characters");
    }

    @Test
    void shouldRejectBicWithInvalidFormat() {
        var result = BicValidator.validate("12BAATWW", false);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("invalid format");
    }

    @Test
    void shouldAcceptBicWithNumericLocationCode() {
        var result = BicValidator.validate("GIBAAT2W", false);
        assertThat(result.valid()).isTrue();
    }
}
