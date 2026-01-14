package at.klickmagiesoftware.epcqr.validation;

import java.math.BigDecimal;

/**
 * Validates payment amount according to EPC QR Code specification.
 *
 * <p>Amount rules:
 * <ul>
 *   <li>Optional (may be null)</li>
 *   <li>If present, must be between 0.01 and 999999999.99 EUR</li>
 *   <li>Maximum 2 decimal places</li>
 * </ul>
 */
public final class AmountValidator {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");
    private static final int MAX_SCALE = 2;

    private AmountValidator() {
    }

    /**
     * Validates a payment amount.
     *
     * @param amount the amount to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validate(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.success();
        }

        if (amount.scale() > MAX_SCALE) {
            return ValidationResult.failure(
                    "Amount must have at most 2 decimal places (was: " + amount.scale() + ")");
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            return ValidationResult.failure(
                    "Amount must be at least " + MIN_AMOUNT + " EUR (was: " + amount + ")");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            return ValidationResult.failure(
                    "Amount must not exceed " + MAX_AMOUNT + " EUR (was: " + amount + ")");
        }

        return ValidationResult.success();
    }
}
