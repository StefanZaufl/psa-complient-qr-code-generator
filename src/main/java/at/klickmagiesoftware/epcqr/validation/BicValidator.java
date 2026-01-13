package at.klickmagiesoftware.epcqr.validation;

import java.util.regex.Pattern;

/**
 * Validates BIC (Bank Identifier Code) format.
 *
 * <p>A valid BIC has the following structure:
 * <ul>
 *   <li>4 letters: Bank code</li>
 *   <li>2 letters: Country code (ISO 3166-1 alpha-2)</li>
 *   <li>2 alphanumeric: Location code</li>
 *   <li>3 alphanumeric (optional): Branch code</li>
 * </ul>
 *
 * <p>Total length: 8 or 11 characters
 */
public final class BicValidator {

    // BIC format: 4 letters (bank) + 2 letters (country) + 2 alphanumeric (location) + optional 3 alphanumeric (branch)
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$");

    private BicValidator() {
    }

    /**
     * Validates a BIC string.
     *
     * @param bic the BIC to validate
     * @param mandatory whether the BIC is mandatory (true for version 001)
     * @return the validation result
     */
    public static ValidationResult validate(String bic, boolean mandatory) {
        if (bic == null || bic.isBlank()) {
            if (mandatory) {
                return ValidationResult.failure("BIC is mandatory for version 001");
            }
            return ValidationResult.success();
        }

        String normalized = bic.toUpperCase().replaceAll("\\s", "");

        if (normalized.length() != 8 && normalized.length() != 11) {
            return ValidationResult.failure(
                    "BIC must be 8 or 11 characters (was: " + normalized.length() + ")");
        }

        if (!BIC_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.failure("BIC has invalid format. Expected: 4 letters + 2 letters + 2-5 alphanumeric");
        }

        return ValidationResult.success();
    }
}
