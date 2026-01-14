package at.klickmagiesoftware.epcqr;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Character encoding options for EPC QR Code payloads.
 *
 * <p>The PSA specification defines 8 possible character encodings, identified by a numeric code (1-8).
 * UTF-8 is recommended for broadest character support.
 *
 * <p><strong>Note:</strong> ISO-8859-10 (code 7) is defined in the PSA specification but is not
 * available in standard Java JDK. Attempting to use this encoding will throw an
 * {@link UnsupportedCharsetException}. Consider using ISO-8859-4 or ISO-8859-15 as alternatives
 * for Nordic language support.
 */
public enum CharacterEncoding {

    /**
     * UTF-8 encoding (recommended for broadest character support).
     */
    UTF_8(1, "UTF-8"),

    /**
     * ISO 8859-1 (Latin-1) encoding.
     */
    ISO_8859_1(2, "ISO-8859-1"),

    /**
     * ISO 8859-2 (Latin-2, Central European) encoding.
     */
    ISO_8859_2(3, "ISO-8859-2"),

    /**
     * ISO 8859-4 (Latin-4, North European) encoding.
     */
    ISO_8859_4(4, "ISO-8859-4"),

    /**
     * ISO 8859-5 (Cyrillic) encoding.
     */
    ISO_8859_5(5, "ISO-8859-5"),

    /**
     * ISO 8859-7 (Greek) encoding.
     */
    ISO_8859_7(6, "ISO-8859-7"),

    /**
     * ISO 8859-10 (Latin-6, Nordic) encoding.
     *
     * <p><strong>Warning:</strong> This charset is NOT available in standard Java JDK.
     * Attempting to use this encoding will throw {@link UnsupportedCharsetException}.
     * Consider using {@link #ISO_8859_4} or {@link #ISO_8859_15} as alternatives.
     */
    ISO_8859_10(7, "ISO-8859-10"),

    /**
     * ISO 8859-15 (Latin-9, Western European with Euro sign) encoding.
     */
    ISO_8859_15(8, "ISO-8859-15");

    private final int code;
    private final String charsetName;
    private volatile Charset charset;

    CharacterEncoding(int code, String charsetName) {
        this.code = code;
        this.charsetName = charsetName;
    }

    /**
     * Returns the numeric code for this encoding as used in the EPC payload.
     *
     * @return the encoding code (1-8)
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the Java Charset corresponding to this encoding.
     *
     * <p>The charset is resolved lazily on first access. If the charset is not supported
     * by the current JVM, an {@link UnsupportedCharsetException} is thrown.
     *
     * @return the Charset instance
     * @throws UnsupportedCharsetException if the charset is not available in this JVM
     *         (notably ISO-8859-10 is not available in standard Java JDK)
     */
    public Charset getCharset() {
        Charset result = charset;
        if (result == null) {
            synchronized (this) {
                result = charset;
                if (result == null) {
                    if (!Charset.isSupported(charsetName)) {
                        throw new UnsupportedCharsetException(charsetName +
                                " (this charset is specified in PSA spec but not available in standard Java JDK; " +
                                "consider using UTF-8 or ISO-8859-15 instead)");
                    }
                    charset = result = Charset.forName(charsetName);
                }
            }
        }
        return result;
    }

    /**
     * Checks if this encoding's charset is supported by the current JVM.
     *
     * @return true if the charset is available, false otherwise
     */
    public boolean isSupported() {
        return Charset.isSupported(charsetName);
    }
}
