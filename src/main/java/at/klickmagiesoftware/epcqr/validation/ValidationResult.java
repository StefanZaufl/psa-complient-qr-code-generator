package at.klickmagiesoftware.epcqr.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a validation operation, containing success status and any error messages.
 *
 * @param valid true if validation passed, false otherwise
 * @param errors list of error messages (empty if valid)
 */
public record ValidationResult(boolean valid, List<String> errors) {

    /**
     * Creates a successful validation result.
     *
     * @return a valid result with no errors
     */
    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    /**
     * Creates a failed validation result with the specified errors.
     *
     * @param errors the validation error messages
     * @return an invalid result with the specified errors
     */
    public static ValidationResult failure(String... errors) {
        return new ValidationResult(false, List.of(errors));
    }

    /**
     * Creates a failed validation result with the specified error list.
     *
     * @param errors the list of validation error messages
     * @return an invalid result with the specified errors
     */
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, List.copyOf(errors));
    }

    /**
     * Merges this result with another validation result.
     *
     * <p>The merged result is valid only if both results are valid.
     * All errors from both results are combined.
     *
     * @param other the other validation result to merge with
     * @return a new merged validation result
     */
    public ValidationResult merge(ValidationResult other) {
        if (this.valid && other.valid) {
            return success();
        }
        var combinedErrors = new ArrayList<>(this.errors);
        combinedErrors.addAll(other.errors);
        return failure(combinedErrors);
    }
}
