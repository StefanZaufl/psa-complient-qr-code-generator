package at.klickmagiesoftware.epcqr.format;

import at.klickmagiesoftware.epcqr.EpcQrCodeData;

import java.math.BigDecimal;

/**
 * Formats EPC QR Code data into the payload string per PSA specification.
 *
 * <p>The payload format consists of fields separated by line endings (LF),
 * with the structure:
 * <pre>
 * BCD
 * [version]
 * [encoding]
 * SCT
 * [bic]
 * [receiver name]
 * [iban]
 * [amount]
 * [purpose]
 * [reference]
 * [text]
 * [display text]
 * </pre>
 *
 * <p>Empty trailing fields are omitted.
 */
public final class EpcPayloadFormatter {

    private static final String SERVICE_TAG = "BCD";
    private static final String FUNCTION = "SCT";
    private static final String LINE_SEPARATOR = "\n";
    private static final String CURRENCY = "EUR";

    private EpcPayloadFormatter() {
    }

    /**
     * Formats the EPC QR Code data into the payload string.
     *
     * @param data the EPC QR Code data
     * @return the formatted payload string
     */
    public static String format(EpcQrCodeData data) {
        StringBuilder sb = new StringBuilder();

        // Service tag (mandatory, fixed)
        sb.append(SERVICE_TAG).append(LINE_SEPARATOR);

        // Version (mandatory)
        sb.append(data.version().getCode()).append(LINE_SEPARATOR);

        // Encoding (mandatory)
        sb.append(data.encoding().getCode()).append(LINE_SEPARATOR);

        // Function (mandatory, fixed)
        sb.append(FUNCTION).append(LINE_SEPARATOR);

        // BIC (may be empty for V002)
        sb.append(data.hasBic() ? data.bic() : "").append(LINE_SEPARATOR);

        // Receiver name (mandatory)
        sb.append(data.receiverName()).append(LINE_SEPARATOR);

        // IBAN (mandatory)
        sb.append(data.iban()).append(LINE_SEPARATOR);

        // Amount (optional)
        sb.append(formatAmount(data.amount())).append(LINE_SEPARATOR);

        // Purpose (optional)
        sb.append(data.hasPurpose() ? data.purpose() : "").append(LINE_SEPARATOR);

        // Reference (optional, mutually exclusive with text)
        // Only add line separator if text or displayText follows
        sb.append(data.hasReference() ? data.reference() : "");
        if (data.hasText() || data.hasDisplayText()) {
            sb.append(LINE_SEPARATOR);
        }

        // Text (optional, mutually exclusive with reference)
        sb.append(data.hasText() ? data.text() : "");

        // Display text (optional) - only add if present
        if (data.hasDisplayText()) {
            sb.append(LINE_SEPARATOR).append(data.displayText());
        }

        return sb.toString();
    }

    /**
     * Calculates the byte count for the formatted payload.
     *
     * @param data the EPC QR Code data
     * @return the payload size in bytes
     */
    public static int calculatePayloadBytes(EpcQrCodeData data) {
        String payload = format(data);
        return payload.getBytes(data.encoding().getCharset()).length;
    }

    /**
     * Formats the amount according to EPC specification.
     *
     * <p>Format rules:
     * <ul>
     *   <li>Prefix: "EUR"</li>
     *   <li>Decimal separator: "." (dot)</li>
     *   <li>No trailing zeros after decimal point</li>
     *   <li>No leading zeros (except for amounts less than 1)</li>
     * </ul>
     *
     * @param amount the amount (may be null)
     * @return the formatted amount string, or empty string if null
     */
    static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        // Strip trailing zeros and convert to plain string (no scientific notation)
        String amountStr = amount.stripTrailingZeros().toPlainString();

        return CURRENCY + amountStr;
    }
}
