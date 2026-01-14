package at.klickmagiesoftware.epcqr.exception;

import java.util.List;

/**
 * Exception thrown when EPC QR Code data validation fails.
 *
 * <p>Contains a list of all validation errors found, allowing the caller to
 * display all issues at once rather than fixing them one at a time.
 */
public final class ValidationException extends EpcQrCodeException {

    private final List<String> errors;

    /**
     * Creates a new validation exception with the specified errors.
     *
     * @param errors the list of validation error messages
     */
    public ValidationException(List<String> errors) {
        super(formatMessage(errors));
        this.errors = List.copyOf(errors);
    }

    /**
     * Creates a new validation exception with a single error.
     *
     * @param error the validation error message
     */
    public ValidationException(String error) {
        this(List.of(error));
    }

    /**
     * Returns the list of validation errors.
     *
     * @return an immutable list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    private static String formatMessage(List<String> errors) {
        if (errors.size() == 1) {
            return "Validation failed: " + errors.getFirst();
        }
        return "Validation failed with " + errors.size() + " errors:\n- " + String.join("\n- ", errors);
    }
}
