# EPC QR Code Library - Architecture Document

## 1. Executive Summary

This document describes the architecture for a Java library that generates EPC (European Payments Council) QR Codes for SEPA Credit Transfers, compliant with the PSA (Payment Services Austria) specification version 3.0.

## 2. QR Code Library Selection

### Candidates Evaluated

| Library | Latest Version | License | Maven Central | Status |
|---------|---------------|---------|---------------|--------|
| **ZXing** | 3.5.4 (Nov 2025) | Apache 2.0 | Yes | Maintenance mode |
| **Nayuki QR** | N/A | MIT | No (source copy) | Active |
| **QRGen** | 3.0.1 (Mar 2025) | Apache 2.0 | No (JitPack) | Active |

### Recommendation: ZXing (com.google.zxing)

**Selected for the following reasons:**

1. **Maven Central availability** - No additional repositories required; clean dependency management
2. **Proven stability** - Battle-tested in countless production systems
3. **Sufficient feature set** - Provides all required capabilities:
   - Error correction level M (required by spec)
   - QR Version control (max Version 13)
   - Multiple character encoding support
   - BitMatrix output for flexible rendering
4. **Apache 2.0 License** - Permissive, compatible with commercial use
5. **Continued updates** - Despite "maintenance mode", still receives updates (last release Nov 2025)

### SVG Generation Approach

For SVG output, we generate the SVG directly from ZXing's `BitMatrix` without additional dependencies. This approach:
- Keeps the library lightweight (no SVG framework dependency)
- Provides clean, minimal SVG output
- Allows full control over SVG structure and attributes

The SVG generation simply iterates over the BitMatrix and creates `<rect>` elements for each black module.

**Dependencies to add:**
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.4</version>
</dependency>
```

> **Note:** Only the `core` module is required. The `javase` module is not needed since we generate SVG directly from the BitMatrix.

## 3. Overall Architecture

### 3.1 Architecture Style

The library follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        PUBLIC API LAYER                         │
│  EpcQrCode (Builder) │ EpcQrCodeGenerator │ EpcQrCodeData      │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DOMAIN/CORE LAYER                          │
│  CharacterEncoding │ EpcVersion │ Validators │ PayloadFormatter│
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       RENDER LAYER                              │
│                   SvgRenderer (BitMatrix → SVG)                 │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE LAYER                        │
│                    ZXing QR Code Library                        │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Design Principles

1. **Immutability** - All data objects are immutable (using Java records)
2. **Builder Pattern** - Fluent API for constructing EPC QR data
3. **Fail-Fast Validation** - Validate all inputs before QR generation
4. **Separation of Concerns** - Data model, validation, formatting, and generation are separate
5. **Minimal Dependencies** - Only ZXing as external dependency

## 4. Package Structure

```
at.klickmagiesoftware.epcqr
├── EpcQrCode.java                    # Builder entry point
├── EpcQrCodeData.java                # Immutable data record
├── EpcQrCodeGenerator.java           # QR image generation
├── EpcVersion.java                   # Version enum (V001, V002)
├── CharacterEncoding.java            # Encoding enum (1-8)
│
├── validation/
│   ├── EpcQrCodeValidator.java       # Main validation orchestrator
│   ├── ValidationResult.java         # Validation result record
│   ├── IbanValidator.java            # IBAN format validation
│   ├── BicValidator.java             # BIC format validation
│   └── AmountValidator.java          # Amount range validation
│
├── format/
│   └── EpcPayloadFormatter.java      # Formats data to EPC string
│
├── render/
│   └── SvgRenderer.java              # Converts BitMatrix to SVG string
│
└── exception/
    ├── EpcQrCodeException.java       # Base exception
    ├── ValidationException.java      # Validation failures
    └── GenerationException.java      # QR generation failures
```

## 5. Component Breakdown

### 5.1 EpcQrCodeData (Record)

Immutable data container holding all EPC QR code fields.

```java
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
    // Compact constructor for validation
}
```

**Design Decision:** Using `BigDecimal` for amount to avoid floating-point precision issues in financial calculations.

### 5.2 EpcQrCode (Builder)

Fluent builder providing the main entry point for users.

```java
public final class EpcQrCode {

    public static Builder builder() { ... }

    public static final class Builder {
        public Builder version(EpcVersion version) { ... }
        public Builder encoding(CharacterEncoding encoding) { ... }
        public Builder bic(String bic) { ... }
        public Builder receiverName(String name) { ... }
        public Builder iban(String iban) { ... }
        public Builder amount(BigDecimal amount) { ... }
        public Builder amount(double amount) { ... }  // Convenience
        public Builder purpose(String purpose) { ... }
        public Builder reference(String reference) { ... }
        public Builder text(String text) { ... }
        public Builder displayText(String displayText) { ... }

        public EpcQrCodeData build() { ... }  // Validates and builds
    }
}
```

### 5.3 EpcQrCodeGenerator

Responsible for generating QR code images from validated data. The generator produces SVG output, which is ideal for:
- Scalable, resolution-independent QR codes
- Web and print applications
- Easy integration with HTML/CSS

```java
public final class EpcQrCodeGenerator {

    // Default settings per PSA specification
    private static final ErrorCorrectionLevel ERROR_CORRECTION = ErrorCorrectionLevel.M;
    private static final int MAX_QR_VERSION = 13;
    private static final int DEFAULT_MODULE_SIZE = 4;  // pixels per module

    // SVG generation
    public String generateSvg(EpcQrCodeData data) { ... }
    public String generateSvg(EpcQrCodeData data, int moduleSize) { ... }

    // Advanced: get the payload string without generating QR
    public String formatPayload(EpcQrCodeData data) { ... }
}
```

**SVG Output Details:**

The generated SVG has the following characteristics:
- Each QR module is rendered as a `<rect>` element
- Configurable module size (default: 4 pixels per module)
- Includes proper quiet zone (4 modules) as required by QR spec
- Clean, minimal SVG structure for easy embedding
- Viewbox-based sizing for scalability

Example SVG structure:
```xml
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 292 292" width="292" height="292">
  <rect width="100%" height="100%" fill="white"/>
  <rect x="16" y="16" width="4" height="4" fill="black"/>
  <!-- ... more modules ... -->
</svg>
```

### 5.4 EpcVersion (Enum)

```java
public enum EpcVersion {
    V001("001", true),   // BIC mandatory
    V002("002", false);  // BIC optional

    private final String code;
    private final boolean bicMandatory;

    public String getCode() { ... }
    public boolean isBicMandatory() { ... }
}
```

### 5.5 CharacterEncoding (Enum)

```java
public enum CharacterEncoding {
    UTF_8(1, StandardCharsets.UTF_8),
    ISO_8859_1(2, StandardCharsets.ISO_8859_1),
    ISO_8859_2(3, Charset.forName("ISO-8859-2")),
    ISO_8859_4(4, Charset.forName("ISO-8859-4")),
    ISO_8859_5(5, Charset.forName("ISO-8859-5")),
    ISO_8859_7(7, Charset.forName("ISO-8859-7")),
    ISO_8859_10(7, Charset.forName("ISO-8859-10")),
    ISO_8859_15(8, Charset.forName("ISO-8859-15"));

    private final int code;
    private final Charset charset;

    public int getCode() { ... }
    public Charset getCharset() { ... }
}
```

### 5.6 Validation Components

#### ValidationResult

```java
public record ValidationResult(
    boolean valid,
    List<String> errors
) {
    public static ValidationResult success() { ... }
    public static ValidationResult failure(String... errors) { ... }
    public static ValidationResult failure(List<String> errors) { ... }

    public ValidationResult merge(ValidationResult other) { ... }
}
```

#### EpcQrCodeValidator

Orchestrates all validation rules:

- Service tag is always "BCD"
- Version is 001 or 002
- Encoding code is 1-8
- Function is always "SCT"
- BIC: 8 or 11 characters, mandatory for V001
- Receiver name: 1-70 characters, mandatory
- IBAN: 1-34 characters, mandatory, valid format
- Amount: 0.01 to 999999999.99, EUR only
- Purpose: 0 or 4 characters
- Reference: 0-35 characters (mutually exclusive with text)
- Text: 0-140 characters (mutually exclusive with reference)
- Display text: 0-70 characters
- **Total payload: max 331 bytes**

### 5.7 EpcPayloadFormatter

Formats the data into the EPC payload string per specification:

```java
public final class EpcPayloadFormatter {

    private static final String SERVICE_TAG = "BCD";
    private static final String FUNCTION = "SCT";

    public String format(EpcQrCodeData data) {
        // Returns properly formatted string with line separators
    }

    public int calculatePayloadBytes(EpcQrCodeData data) {
        // Returns byte count for the formatted payload
    }
}
```

**Line Separator Handling:** The specification allows either LF or CRLF, but requires consistency. This library uses LF (`\n`) for minimal payload size.

### 5.8 SvgRenderer

Converts ZXing's `BitMatrix` to SVG markup:

```java
public final class SvgRenderer {

    private static final int DEFAULT_MODULE_SIZE = 4;
    private static final int QUIET_ZONE_MODULES = 4;  // QR spec requirement

    public String render(BitMatrix matrix) {
        return render(matrix, DEFAULT_MODULE_SIZE);
    }

    public String render(BitMatrix matrix, int moduleSize) {
        // Generates SVG with proper quiet zone and viewBox
    }
}
```

**Implementation Notes:**
- Uses a single path element with multiple rectangles for efficient SVG (smaller file size)
- Quiet zone (4 modules of white space) is included per QR code specification
- ViewBox-based sizing ensures the SVG scales properly at any size

## 6. Data Flow

### 6.1 QR Code Generation Flow

```
┌──────────────┐     ┌─────────────┐     ┌───────────────┐     ┌──────────────┐
│ User calls   │────▶│ Builder     │────▶│ Validation    │────▶│ EpcQrCodeData│
│ Builder API  │     │ collects    │     │ validates all │     │ (immutable)  │
│              │     │ parameters  │     │ fields        │     │              │
└──────────────┘     └─────────────┘     └───────────────┘     └──────────────┘
                                                                       │
                                                                       ▼
┌──────────────┐     ┌─────────────┐     ┌───────────────┐     ┌──────────────┐
│ SVG String   │◀────│ SVG         │◀────│ ZXing         │◀────│ Generator    │
│ (ready to    │     │ Renderer    │     │ BitMatrix     │     │ receives     │
│ embed/save)  │     │             │     │               │     │ data         │
└──────────────┘     └─────────────┘     └───────────────┘     └──────────────┘
```

### 6.2 Payload Format Structure

```
BCD                          ← Service Tag (fixed)
002                          ← Version
1                            ← Encoding (1=UTF-8)
SCT                          ← Function (fixed)
GIBAATWW                     ← BIC (optional for V002)
Max Mustermann               ← Receiver Name
AT682011131032423628         ← IBAN
EUR1456.89                   ← Amount with currency
                             ← Purpose (empty)
457845789452                 ← Reference
Diverse Autoteile            ← Text (only if no reference)
                             ← Display Text (optional)
```

## 7. API Design Decisions

### 7.1 Why Builder Pattern?

1. **Many optional fields** - 12 fields, only 4 mandatory
2. **Validation at build time** - Ensures only valid objects exist
3. **Fluent API** - Natural and readable code
4. **Immutable result** - Thread-safe, no defensive copying needed

### 7.2 Why Separate Generator?

1. **Stateless generation** - Generator can be reused
2. **Configurable output** - Different sizes, formats
3. **Testability** - Easy to mock in unit tests
4. **Single Responsibility** - Data vs. generation concerns separated

### 7.3 Amount Handling

- **Input:** Accept both `double` and `BigDecimal`
- **Internal:** Always use `BigDecimal` for precision
- **Output formatting:** Follow spec exactly (no trailing zeros, no leading zeros except for 0.xx)

### 7.4 Reference vs Text Mutual Exclusivity

The specification states that Reference and Text are mutually exclusive. The builder enforces this:
- Setting `reference()` clears any previous `text()`
- Setting `text()` clears any previous `reference()`
- Both can be empty

## 8. Error Handling Strategy

### 8.1 Exception Hierarchy

```
EpcQrCodeException (RuntimeException)
├── ValidationException
│   └── Contains list of validation errors
└── GenerationException
    └── Wraps ZXing exceptions
```

**Design Decision:** Using unchecked exceptions (RuntimeException) as validation errors represent programming errors that should be fixed, not handled at runtime.

### 8.2 Validation Error Messages

Clear, actionable error messages:
- "BIC is mandatory for version 001"
- "Receiver name exceeds maximum length of 70 characters (was: 85)"
- "Amount must be between 0.01 and 999999999.99 (was: 0.001)"
- "Reference and text are mutually exclusive; only one may have content"
- "Total payload exceeds maximum of 331 bytes (was: 345)"

## 9. Java 25 Features Utilized

1. **Records** - For immutable data classes (`EpcQrCodeData`, `ValidationResult`)
2. **Pattern Matching for switch** - In validation logic
3. **Sealed classes** - For exception hierarchy (optional)
4. **String templates** - For error message formatting (if stable)
5. **Enhanced enums** - Rich enum implementations

## 10. Testing Strategy

### 10.1 Unit Tests

| Component | Test Focus |
|-----------|------------|
| `EpcQrCodeData` | Construction, immutability |
| `EpcQrCode.Builder` | Fluent API, validation triggers |
| `EpcQrCodeValidator` | All validation rules, edge cases |
| `IbanValidator` | IBAN format, country codes |
| `BicValidator` | BIC format (8 and 11 chars) |
| `AmountValidator` | Range, precision, formatting |
| `EpcPayloadFormatter` | Exact output format, encoding |
| `SvgRenderer` | SVG structure, dimensions, module positioning |

### 10.2 Integration Tests

| Test Scenario | Description |
|---------------|-------------|
| End-to-end generation | Generate SVG → Parse SVG → Decode QR → Verify content |
| All encoding types | Test each of the 8 character encodings |
| Version variants | V001 with BIC, V002 with/without BIC |
| Boundary tests | Maximum payload (331 bytes) |
| Example verification | All examples from spec must work |
| SVG validity | Verify generated SVG is well-formed XML |
| SVG dimensions | Verify correct viewBox and quiet zone |

### 10.3 Test Data from Specification

The specification provides several examples that serve as test cases:

```java
@Test
void shouldGenerateExample1FromSpec() {
    var data = EpcQrCode.builder()
        .version(EpcVersion.V001)
        .encoding(CharacterEncoding.UTF_8)
        .bic("GIBAATWW")
        .receiverName("Max Mustermann")
        .iban("AT682011131032423628")
        .amount(1456.89)
        .reference("457845789452")
        .text("Diverse Autoteile, Re 789452 KN 457845")
        .build();

    var generator = new EpcQrCodeGenerator();
    var payload = generator.formatPayload(data);

    // Verify exact payload format
    assertThat(payload).startsWith("BCD\n001\n1\nSCT\n");
    // ... more assertions
}
```

## 11. Class Diagram

```
┌───────────────────────────────────────┐
│            <<record>>                 │
│          EpcQrCodeData                │
├───────────────────────────────────────┤
│ + version: EpcVersion                 │
│ + encoding: CharacterEncoding         │
│ + bic: String                         │
│ + receiverName: String                │
│ + iban: String                        │
│ + amount: BigDecimal                  │
│ + purpose: String                     │
│ + reference: String                   │
│ + text: String                        │
│ + displayText: String                 │
└───────────────────────────────────────┘
                    ▲
                    │ creates
┌───────────────────┴───────────────────┐
│              EpcQrCode                │
├───────────────────────────────────────┤
│ + builder(): Builder                  │
├───────────────────────────────────────┤
│        <<inner class>>                │
│            Builder                    │
│ + version(EpcVersion): Builder        │
│ + encoding(CharacterEncoding): Builder│
│ + bic(String): Builder                │
│ + receiverName(String): Builder       │
│ + iban(String): Builder               │
│ + amount(BigDecimal): Builder         │
│ + amount(double): Builder             │
│ + purpose(String): Builder            │
│ + reference(String): Builder          │
│ + text(String): Builder               │
│ + displayText(String): Builder        │
│ + build(): EpcQrCodeData              │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│         EpcQrCodeGenerator            │
├───────────────────────────────────────┤
│ + generateSvg(EpcQrCodeData): String  │
│ + generateSvg(data, moduleSize): Str  │
│ + formatPayload(data): String         │
└───────────────────────────────────────┘
         │
         │ uses
         ▼
┌───────────────────────────────────────┐
│        EpcPayloadFormatter            │
├───────────────────────────────────────┤
│ + format(EpcQrCodeData): String       │
│ + calculatePayloadBytes(data): int    │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│           SvgRenderer                 │
├───────────────────────────────────────┤
│ + render(BitMatrix): String           │
│ + render(BitMatrix, moduleSize): Str  │
└───────────────────────────────────────┘

┌───────────────────┐    ┌───────────────────┐
│   <<enum>>        │    │    <<enum>>       │
│   EpcVersion      │    │ CharacterEncoding │
├───────────────────┤    ├───────────────────┤
│ V001              │    │ UTF_8             │
│ V002              │    │ ISO_8859_1        │
├───────────────────┤    │ ISO_8859_2        │
│ + getCode()       │    │ ISO_8859_4        │
│ + isBicMandatory()│    │ ISO_8859_5        │
└───────────────────┘    │ ISO_8859_7        │
                         │ ISO_8859_10       │
                         │ ISO_8859_15       │
                         ├───────────────────┤
                         │ + getCode()       │
                         │ + getCharset()    │
                         └───────────────────┘
```

## 12. Usage Examples

### 12.1 Minimal Example (Version 002, no amount)

```java
var data = EpcQrCode.builder()
    .version(EpcVersion.V002)
    .encoding(CharacterEncoding.UTF_8)
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .build();

var generator = new EpcQrCodeGenerator();
String svg = generator.generateSvg(data);
// svg contains the complete SVG markup ready for embedding
```

### 12.2 Full Example with All Fields

```java
var data = EpcQrCode.builder()
    .version(EpcVersion.V001)
    .encoding(CharacterEncoding.UTF_8)
    .bic("GIBAATWW")
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .amount(new BigDecimal("1456.89"))
    .reference("457845789452")
    .displayText("Payment for car parts")
    .build();

var generator = new EpcQrCodeGenerator();
// Generate with custom module size (6 pixels per module for larger output)
String svg = generator.generateSvg(data, 6);

// Save to file
Files.writeString(Path.of("payment-qr.svg"), svg);
```

### 12.3 Using Text Instead of Reference

```java
var data = EpcQrCode.builder()
    .version(EpcVersion.V002)
    .encoding(CharacterEncoding.UTF_8)
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .amount(99.99)
    .text("Invoice #12345 - Consulting Services")
    .build();

String svg = new EpcQrCodeGenerator().generateSvg(data);

// Embed in HTML
String html = STR."""
    <html>
        <body>
            <h1>Payment QR Code</h1>
            \{svg}
        </body>
    </html>
    """;
```

### 12.4 Getting Just the Payload

```java
var data = EpcQrCode.builder()
    .version(EpcVersion.V002)
    .encoding(CharacterEncoding.UTF_8)
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .amount(100.00)
    .build();

// Get the raw EPC payload string (useful for debugging or custom QR generation)
String payload = new EpcQrCodeGenerator().formatPayload(data);
System.out.println(payload);
// Output:
// BCD
// 002
// 1
// SCT
//
// Max Mustermann
// AT682011131032423628
// EUR100
```

## 13. Future Considerations

1. **PNG/Raster Output** - Add raster image output for systems that don't support SVG
2. **Custom Styling** - Logo embedding, color customization (within spec limits)
3. **Batch Generation** - Multiple QR codes efficiently
4. **Payload Optimization** - Automatic encoding selection for minimal byte usage
5. **Visual Frame** - Add optional "Zahlen mit Code" frame per PSA specification

## 14. References

- [PSA Specification v3.0](https://www.psa.at/) - "Application of QR-Code for Initiating of Credit Transfers"
- [ISO 18004](https://www.iso.org/standard/62021.html) - QR Code bar code symbology specification
- [ZXing GitHub](https://github.com/zxing/zxing) - ZXing ("Zebra Crossing") barcode library
- [EPC Guidelines](https://www.europeanpaymentscouncil.eu/) - European Payments Council standards
