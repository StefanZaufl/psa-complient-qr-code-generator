package at.klickmagiesoftware.epcqr;

import at.klickmagiesoftware.epcqr.exception.ValidationException;
import at.klickmagiesoftware.epcqr.validation.EpcQrCodeValidator;

import java.math.BigDecimal;

/**
 * Entry point for building EPC QR Code data.
 *
 * <p>Use the fluent builder API to construct validated {@link EpcQrCodeData} instances:
 *
 * <pre>{@code
 * EpcQrCodeData data = EpcQrCode.builder()
 *     .version(EpcVersion.V002)
 *     .encoding(CharacterEncoding.UTF_8)
 *     .receiverName("Max Mustermann")
 *     .iban("AT682011131032423628")
 *     .amount(100.00)
 *     .build();
 * }</pre>
 */
public final class EpcQrCode {

    private EpcQrCode() {
    }

    /**
     * Creates a new builder for EPC QR Code data.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for constructing {@link EpcQrCodeData} instances.
     *
     * <p>The builder validates all data when {@link #build()} is called.
     * Required fields are:
     * <ul>
     *   <li>version</li>
     *   <li>encoding</li>
     *   <li>receiverName</li>
     *   <li>iban</li>
     *   <li>bic (only for version V001)</li>
     * </ul>
     */
    public static final class Builder {

        private EpcVersion version;
        private CharacterEncoding encoding;
        private String bic;
        private String receiverName;
        private String iban;
        private BigDecimal amount;
        private String purpose;
        private String reference;
        private String text;
        private String displayText;

        private Builder() {
        }

        /**
         * Sets the EPC version.
         *
         * @param version the version (V001 requires BIC, V002 makes BIC optional)
         * @return this builder
         */
        public Builder version(EpcVersion version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the character encoding.
         *
         * @param encoding the encoding for text fields
         * @return this builder
         */
        public Builder encoding(CharacterEncoding encoding) {
            this.encoding = encoding;
            return this;
        }

        /**
         * Sets the BIC (Bank Identifier Code).
         *
         * @param bic the BIC (8 or 11 characters)
         * @return this builder
         */
        public Builder bic(String bic) {
            this.bic = bic;
            return this;
        }

        /**
         * Sets the receiver name.
         *
         * @param receiverName the name of the payment receiver (max 70 characters)
         * @return this builder
         */
        public Builder receiverName(String receiverName) {
            this.receiverName = receiverName;
            return this;
        }

        /**
         * Sets the IBAN.
         *
         * @param iban the receiver's IBAN
         * @return this builder
         */
        public Builder iban(String iban) {
            this.iban = iban;
            return this;
        }

        /**
         * Sets the payment amount using BigDecimal for precision.
         *
         * @param amount the payment amount in EUR (0.01 to 999999999.99)
         * @return this builder
         */
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Sets the payment amount using a double value.
         *
         * <p>Note: For precise financial calculations, prefer {@link #amount(BigDecimal)}.
         *
         * @param amount the payment amount in EUR
         * @return this builder
         */
        public Builder amount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }

        /**
         * Sets the purpose code.
         *
         * @param purpose the 4-character purpose code
         * @return this builder
         */
        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        /**
         * Sets the reconciliation reference.
         *
         * <p>Note: Reference and text are mutually exclusive. Setting reference clears any previous text.
         *
         * @param reference the reference (max 35 characters)
         * @return this builder
         */
        public Builder reference(String reference) {
            this.reference = reference;
            this.text = null;
            return this;
        }

        /**
         * Sets the reconciliation text.
         *
         * <p>Note: Reference and text are mutually exclusive. Setting text clears any previous reference.
         *
         * @param text the text (max 140 characters)
         * @return this builder
         */
        public Builder text(String text) {
            this.text = text;
            this.reference = null;
            return this;
        }

        /**
         * Sets the display text.
         *
         * <p>This text is shown to the user but is NOT transmitted to the payment system.
         *
         * @param displayText the display text (max 70 characters)
         * @return this builder
         */
        public Builder displayText(String displayText) {
            this.displayText = displayText;
            return this;
        }

        /**
         * Builds and validates the EPC QR Code data.
         *
         * @return the validated EpcQrCodeData instance
         * @throws ValidationException if validation fails
         */
        public EpcQrCodeData build() {
            var result = EpcQrCodeValidator.validate(
                    version, encoding, bic, receiverName, iban,
                    amount, purpose, reference, text, displayText);

            if (!result.valid()) {
                throw new ValidationException(result.errors());
            }

            return new EpcQrCodeData(
                    version, encoding, bic, receiverName, iban,
                    amount, purpose, reference, text, displayText);
        }
    }
}
