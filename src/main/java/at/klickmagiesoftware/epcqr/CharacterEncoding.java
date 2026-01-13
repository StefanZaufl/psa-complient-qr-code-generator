package at.klickmagiesoftware.epcqr;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Character encoding options for EPC QR Code payloads.
 *
 * <p>The PSA specification defines 8 possible character encodings, identified by a numeric code (1-8).
 * UTF-8 is recommended for broadest character support.
 */
public enum CharacterEncoding {

    /**
     * UTF-8 encoding (recommended for broadest character support).
     */
    UTF_8(1, StandardCharsets.UTF_8),

    /**
     * ISO 8859-1 (Latin-1) encoding.
     */
    ISO_8859_1(2, StandardCharsets.ISO_8859_1),

    /**
     * ISO 8859-2 (Latin-2, Central European) encoding.
     */
    ISO_8859_2(3, Charset.forName("ISO-8859-2")),

    /**
     * ISO 8859-4 (Latin-4, North European) encoding.
     */
    ISO_8859_4(4, Charset.forName("ISO-8859-4")),

    /**
     * ISO 8859-5 (Cyrillic) encoding.
     */
    ISO_8859_5(5, Charset.forName("ISO-8859-5")),

    /**
     * ISO 8859-7 (Greek) encoding.
     */
    ISO_8859_7(6, Charset.forName("ISO-8859-7")),

    /**
     * ISO 8859-10 (Latin-6, Nordic) encoding.
     */
    ISO_8859_10(7, Charset.forName("ISO-8859-10")),

    /**
     * ISO 8859-15 (Latin-9, Western European with Euro sign) encoding.
     */
    ISO_8859_15(8, Charset.forName("ISO-8859-15"));

    private final int code;
    private final Charset charset;

    CharacterEncoding(int code, Charset charset) {
        this.code = code;
        this.charset = charset;
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
     * @return the Charset instance
     */
    public Charset getCharset() {
        return charset;
    }
}
