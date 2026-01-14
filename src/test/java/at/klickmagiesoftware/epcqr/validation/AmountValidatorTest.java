package at.klickmagiesoftware.epcqr.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AmountValidatorTest {

    @Test
    void shouldAcceptNullAmount() {
        var result = AmountValidator.validate(null);
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptMinimumAmount() {
        var result = AmountValidator.validate(new BigDecimal("0.01"));
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptMaximumAmount() {
        var result = AmountValidator.validate(new BigDecimal("999999999.99"));
        assertThat(result.valid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "100", "1.5", "99.99", "1000000"})
    void shouldAcceptValidAmounts(String amount) {
        var result = AmountValidator.validate(new BigDecimal(amount));
        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldRejectAmountBelowMinimum() {
        var result = AmountValidator.validate(new BigDecimal("0.00"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("at least 0.01");
    }

    @Test
    void shouldRejectZeroAmount() {
        var result = AmountValidator.validate(BigDecimal.ZERO);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("at least 0.01");
    }

    @Test
    void shouldRejectNegativeAmount() {
        var result = AmountValidator.validate(new BigDecimal("-10"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("at least 0.01");
    }

    @Test
    void shouldRejectAmountAboveMaximum() {
        var result = AmountValidator.validate(new BigDecimal("1000000000"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("must not exceed");
    }

    @Test
    void shouldRejectAmountWithMoreThan2DecimalPlaces() {
        var result = AmountValidator.validate(new BigDecimal("10.123"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors().getFirst()).contains("at most 2 decimal places");
    }
}
