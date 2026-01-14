package at.klickmagiesoftware.epcqr;

/**
 * EPC QR Code version as defined in the PSA specification.
 *
 * <p>The version determines whether BIC is mandatory or optional:
 * <ul>
 *   <li>{@link #V001} - BIC is mandatory</li>
 *   <li>{@link #V002} - BIC is optional (may be empty)</li>
 * </ul>
 */
public enum EpcVersion {

    /**
     * Version 001 - BIC is mandatory.
     */
    V001("001", true),

    /**
     * Version 002 - BIC is optional.
     */
    V002("002", false);

    private final String code;
    private final boolean bicMandatory;

    EpcVersion(String code, boolean bicMandatory) {
        this.code = code;
        this.bicMandatory = bicMandatory;
    }

    /**
     * Returns the version code as it appears in the EPC payload (e.g., "001" or "002").
     *
     * @return the version code string
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns whether the BIC field is mandatory for this version.
     *
     * @return true if BIC is mandatory, false if optional
     */
    public boolean isBicMandatory() {
        return bicMandatory;
    }
}
