package at.klickmagiesoftware.epcqr.validation;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Validates IBAN (International Bank Account Number) format.
 *
 * <p>Validates:
 * <ul>
 *   <li>Length: 1-34 characters (per EPC specification)</li>
 *   <li>Format: 2-letter country code + 2 check digits + BBAN</li>
 *   <li>Check digits using MOD-97 algorithm (ISO 7064)</li>
 * </ul>
 */
public final class IbanValidator {

    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$");
    private static final int MAX_LENGTH = 34;
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);

    private IbanValidator() {
    }

    /**
     * Validates an IBAN string.
     *
     * @param iban the IBAN to validate
     * @return the validation result
     */
    public static ValidationResult validate(String iban) {
        if (iban == null || iban.isBlank()) {
            return ValidationResult.failure("IBAN is required");
        }

        String normalized = iban.toUpperCase().replaceAll("\\s", "");

        if (normalized.length() > MAX_LENGTH) {
            return ValidationResult.failure(
                    "IBAN exceeds maximum length of " + MAX_LENGTH + " characters (was: " + normalized.length() + ")");
        }

        if (!IBAN_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.failure("IBAN has invalid format. Expected: 2 letters + 2 digits + up to 30 alphanumeric characters");
        }

        if (!isValidChecksum(normalized)) {
            return ValidationResult.failure("IBAN has invalid check digits");
        }

        return ValidationResult.success();
    }

    /**
     * Validates IBAN checksum using MOD-97 algorithm.
     */
    private static boolean isValidChecksum(String iban) {
        // Move first 4 characters to the end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.getNumericValue(c));
            } else {
                numericIban.append(c);
            }
        }

        // Perform MOD-97 check
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        return ibanNumber.mod(MOD_97).equals(BigInteger.ONE);
    }
}
