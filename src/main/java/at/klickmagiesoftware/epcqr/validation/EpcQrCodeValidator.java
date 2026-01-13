package at.klickmagiesoftware.epcqr.validation;

import at.klickmagiesoftware.epcqr.CharacterEncoding;
import at.klickmagiesoftware.epcqr.EpcVersion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Main validation orchestrator for EPC QR Code data.
 *
 * <p>Validates all fields according to the PSA specification:
 * <ul>
 *   <li>Version and encoding are mandatory</li>
 *   <li>BIC: mandatory for V001, optional for V002</li>
 *   <li>Receiver name: 1-70 characters, mandatory</li>
 *   <li>IBAN: valid format, mandatory</li>
 *   <li>Amount: 0.01 to 999999999.99, optional</li>
 *   <li>Purpose: 0 or 4 characters, optional</li>
 *   <li>Reference: 0-35 characters (mutually exclusive with text)</li>
 *   <li>Text: 0-140 characters (mutually exclusive with reference)</li>
 *   <li>Display text: 0-70 characters, optional</li>
 *   <li>Total payload: max 331 bytes</li>
 * </ul>
 */
public final class EpcQrCodeValidator {

    private static final int MAX_RECEIVER_NAME_LENGTH = 70;
    private static final int PURPOSE_LENGTH = 4;
    private static final int MAX_REFERENCE_LENGTH = 35;
    private static final int MAX_TEXT_LENGTH = 140;
    private static final int MAX_DISPLAY_TEXT_LENGTH = 70;
    private static final int MAX_PAYLOAD_BYTES = 331;

    private EpcQrCodeValidator() {
    }

    /**
     * Validates all EPC QR Code fields.
     *
     * @param version the EPC version
     * @param encoding the character encoding
     * @param bic the BIC (may be null for V002)
     * @param receiverName the receiver name
     * @param iban the IBAN
     * @param amount the payment amount (may be null)
     * @param purpose the purpose code (may be null)
     * @param reference the reference (may be null)
     * @param text the text (may be null)
     * @param displayText the display text (may be null)
     * @return the validation result with all errors found
     */
    public static ValidationResult validate(
            EpcVersion version,
            CharacterEncoding encoding,
            String bic,
            String receiverName,
            String iban,
            BigDecimal amount,
            String purpose,
            String reference,
            String text,
            String displayText) {

        List<String> errors = new ArrayList<>();

        // Mandatory fields
        if (version == null) {
            errors.add("Version is required");
        }
        if (encoding == null) {
            errors.add("Character encoding is required");
        }

        // BIC validation
        var bicResult = BicValidator.validate(bic, version != null && version.isBicMandatory());
        if (!bicResult.valid()) {
            errors.addAll(bicResult.errors());
        }

        // Receiver name validation
        if (receiverName == null || receiverName.isBlank()) {
            errors.add("Receiver name is required");
        } else if (receiverName.length() > MAX_RECEIVER_NAME_LENGTH) {
            errors.add("Receiver name exceeds maximum length of " + MAX_RECEIVER_NAME_LENGTH +
                    " characters (was: " + receiverName.length() + ")");
        }

        // IBAN validation
        var ibanResult = IbanValidator.validate(iban);
        if (!ibanResult.valid()) {
            errors.addAll(ibanResult.errors());
        }

        // Amount validation
        var amountResult = AmountValidator.validate(amount);
        if (!amountResult.valid()) {
            errors.addAll(amountResult.errors());
        }

        // Purpose validation (must be exactly 4 characters if present)
        if (purpose != null && !purpose.isBlank() && purpose.length() != PURPOSE_LENGTH) {
            errors.add("Purpose code must be exactly " + PURPOSE_LENGTH + " characters (was: " + purpose.length() + ")");
        }

        // Reference and text mutual exclusivity
        boolean hasReference = reference != null && !reference.isBlank();
        boolean hasText = text != null && !text.isBlank();

        if (hasReference && hasText) {
            errors.add("Reference and text are mutually exclusive; only one may have content");
        }

        if (hasReference && reference.length() > MAX_REFERENCE_LENGTH) {
            errors.add("Reference exceeds maximum length of " + MAX_REFERENCE_LENGTH +
                    " characters (was: " + reference.length() + ")");
        }

        if (hasText && text.length() > MAX_TEXT_LENGTH) {
            errors.add("Text exceeds maximum length of " + MAX_TEXT_LENGTH +
                    " characters (was: " + text.length() + ")");
        }

        // Display text validation
        if (displayText != null && displayText.length() > MAX_DISPLAY_TEXT_LENGTH) {
            errors.add("Display text exceeds maximum length of " + MAX_DISPLAY_TEXT_LENGTH +
                    " characters (was: " + displayText.length() + ")");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Validates that the payload size does not exceed the maximum.
     *
     * @param payloadBytes the payload size in bytes
     * @return the validation result
     */
    public static ValidationResult validatePayloadSize(int payloadBytes) {
        if (payloadBytes > MAX_PAYLOAD_BYTES) {
            return ValidationResult.failure(
                    "Total payload exceeds maximum of " + MAX_PAYLOAD_BYTES + " bytes (was: " + payloadBytes + ")");
        }
        return ValidationResult.success();
    }
}
