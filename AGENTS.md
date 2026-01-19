# AGENTS.md

This file provides essential guidelines and commands for agentic coding agents working in this EPC QR Code library repository.

## Build Commands

### Core Maven Commands
```bash
# Compile the project
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=EpcQrCodeTest

# Run a specific test method
mvn test -Dtest=EpcQrCodeTest#shouldBuildMinimalValidData

# Run tests with coverage
mvn test jacoco:report

# Package the JAR
mvn package

# Install to local repository
mvn install

# Clean build artifacts
mvn clean

# Full clean build and test
mvn clean compile test
```

### Test Execution Patterns
- Test classes follow `*Test.java` naming pattern
- All tests use JUnit 5 and AssertJ
- Run tests in specific packages: `mvn test -Dtest=at.klickmagiesoftware.epcqr.validation.*`
- Run all validation tests: `mvn test -Dtest="**/validation/*Test"`

## Code Style Guidelines

### Project Structure
- **Package**: `at.klickmagiesoftware.epcqr`
- **Main source**: `src/main/java/at/klickmagiesoftware/epcqr/`
- **Test source**: `src/test/java/at/klickmagiesoftware/epcqr/`
- **Java version**: 25 (uses modern features)

### Naming Conventions
- **Classes**: PascalCase (e.g., `EpcQrCode`, `ValidationResult`)
- **Methods**: camelCase (e.g., `generateSvg()`, `hasBic()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_MODULE_SIZE`, `SERVICE_TAG`)
- **Packages**: lowercase, no underscores (e.g., `validation`, `format`, `render`)
- **Test methods**: `should` prefix, descriptive (e.g., `shouldThrowValidationExceptionForMissingBic`)

### Import Organization
1. Java standard library imports (`java.*`)
2. Third-party library imports (`org.*`, `com.*`)
3. Project imports (`at.klickmagiesoftware.epcqr.*`)
4. Static imports last
5. No wildcard imports (except in test files for AssertJ)

### Type Guidelines
- **Records**: Use for immutable data carriers (`EpcQrCodeData`, `ValidationResult`)
- **Enums**: Use for fixed sets of values (`EpcVersion`, `CharacterEncoding`)
- **BigDecimal**: Always use for financial amounts, never double/float
- **String**: Use for text fields, validate for null/empty
- **Optional**: Use sparingly, prefer null checks or record methods like `hasBic()`

### Error Handling
- **Exception hierarchy**: 
  - `EpcQrCodeException` (base)
  - `ValidationException` (contains list of errors)
  - `GenerationException` (wraps ZXing exceptions)
- **Validation**: Fail-fast, collect all errors before throwing
- **Messages**: Clear, actionable, include field names and expected values
- **Unchecked exceptions**: All exceptions extend RuntimeException

### Formatting Rules
- **Indentation**: 4 spaces, no tabs
- **Line length**: 120 characters max
- **Braces**: K&R style, opening brace on same line
- **Spacing**: Around operators, after commas, before opening brace
- **Empty lines**: Between methods, logical sections

### API Design Patterns
- **Builder Pattern**: Used for `EpcQrCode` with fluent API
- **Immutable Objects**: All data objects are immutable
- **Factory Methods**: `EpcQrCode.builder()`, `ValidationResult.success()`
- **Convenience Methods**: Provide both `BigDecimal` and `double` amount setters
- **Mutual Exclusion**: Setting `reference()` clears `text()` and vice versa

### Documentation Standards
- **Javadoc**: All public APIs must have Javadoc
- **Examples**: Include code examples in main class Javadoc
- **Param tags**: Use `@param` for all method parameters
- **Return tags**: Use `@return` for non-void methods
- **Throws tags**: Document all checked exceptions and significant unchecked exceptions

### Testing Guidelines
- **Test Structure**: Given-When-Then pattern
- **Assertions**: Use AssertJ's fluent assertions
- **Test Coverage**: Aim for high coverage on validation logic
- **Edge Cases**: Test null values, empty strings, boundary conditions
- **Error Messages**: Verify specific error messages in validation tests

### Package Organization
```
at.klickmagiesoftware.epcqr/
├── Main classes (EpcQrCode, EpcQrCodeData, etc.)
├── validation/    # Input validation logic
├── format/        # Payload formatting
├── render/        # SVG rendering
└── exception/     # Exception classes
```

### Dependencies
- **ZXing Core**: QR code generation (`com.google.zxing:core`)
- **JUnit 5**: Testing framework (`org.junit.jupiter:junit-jupiter`)
- **AssertJ**: Fluent assertions (`org.assertj:assertj-core`)
- **No other external dependencies**

### Performance Considerations
- **Immutable Objects**: Records provide efficient immutability
- **Lazy Validation**: Only validate when `build()` is called
- **SVG Generation**: Efficient string building, no DOM libraries
- **Memory**: Minimal object creation during generation

### Security Notes
- **No logging of sensitive data**: Never log IBANs or full payment data
- **Input validation**: Validate all user inputs, especially IBAN format
- **Encoding**: Properly handle character encodings per specification
- **No reflection**: Avoid reflection-based APIs for security

## Common Tasks

### Adding New Validation
1. Create validator class in `validation/` package
2. Add method to `EpcQrCodeValidator`
3. Write comprehensive tests
4. Update error messages list

### Modifying Payload Format
1. Update `EpcPayloadFormatter`
2. Ensure byte count calculation is accurate
3. Add integration tests for new format
4. Verify against PSA specification

### Adding New Output Format
1. Create renderer class in `render/` package
2. Add method to `EpcQrCodeGenerator`
3. Write tests for new format
4. Update documentation

### Version Updates
1. Update version in `pom.xml`
2. Check ZXing compatibility
3. Run full test suite
4. Update README if needed

## Git Workflow
- **Main branch**: `main`
- **Feature branches**: Use descriptive names
- **Commits**: Atomic, well-described commit messages
- **PRs**: Require tests to pass before merge