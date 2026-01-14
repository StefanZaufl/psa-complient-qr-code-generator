package at.klickmagiesoftware.epcqr;

import java.math.BigDecimal;

/**
 * Immutable data container for EPC QR Code content.
 *
 * <p>This record holds all the fields defined in the PSA specification for SEPA Credit Transfer QR codes.
 * Instances should be created using {@link EpcQrCode#builder()}.
 *
 * @param version the EPC version (V001 or V002)
 * @param encoding the character encoding for text fields
 * @param bic the Bank Identifier Code (mandatory for V001, optional for V002)
 * @param receiverName the name of the payment receiver (mandatory, max 70 chars)
 * @param iban the receiver's IBAN (mandatory, max 34 chars)
 * @param amount the payment amount in EUR (optional, 0.01 to 999999999.99)
 * @param purpose the purpose code (optional, exactly 4 chars if present)
 * @param reference the reconciliation reference (optional, max 35 chars, mutually exclusive with text)
 * @param text the reconciliation text (optional, max 140 chars, mutually exclusive with reference)
 * @param displayText the display text for user (optional, max 70 chars, not transmitted to payment system)
 */
public record EpcQrCodeData(
        EpcVersion version,
        CharacterEncoding encoding,
        String bic,
        String receiverName,
        String iban,
        BigDecimal amount,
        String purpose,
        String reference,
        String text,
        String displayText
) {

    /**
     * Returns true if this data has a non-empty BIC.
     *
     * @return true if BIC is present and not blank
     */
    public boolean hasBic() {
        return bic != null && !bic.isBlank();
    }

    /**
     * Returns true if this data has an amount.
     *
     * @return true if amount is present
     */
    public boolean hasAmount() {
        return amount != null;
    }

    /**
     * Returns true if this data has a purpose code.
     *
     * @return true if purpose is present and not blank
     */
    public boolean hasPurpose() {
        return purpose != null && !purpose.isBlank();
    }

    /**
     * Returns true if this data has a reference.
     *
     * @return true if reference is present and not blank
     */
    public boolean hasReference() {
        return reference != null && !reference.isBlank();
    }

    /**
     * Returns true if this data has text.
     *
     * @return true if text is present and not blank
     */
    public boolean hasText() {
        return text != null && !text.isBlank();
    }

    /**
     * Returns true if this data has display text.
     *
     * @return true if display text is present and not blank
     */
    public boolean hasDisplayText() {
        return displayText != null && !displayText.isBlank();
    }
}
