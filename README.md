# EPC QR Code Library

A Java library for generating EPC (European Payments Council) QR Codes for SEPA Credit Transfers, compliant with the PSA (Payment Services Austria) specification version 3.0.

## Features

- **PSA Specification Compliant**: Fully implements the Austrian banking standard for payment QR codes
- **SVG Output**: Generates scalable vector graphics for resolution-independent QR codes
- **Comprehensive Validation**: Validates IBAN (with checksum), BIC, amounts, and all field constraints
- **Fluent Builder API**: Easy-to-use builder pattern for constructing payment data
- **Java 25**: Leverages modern Java features (records, pattern matching, string templates)
- **Minimal Dependencies**: Only requires ZXing core library

## Requirements

- Java 25 or later
- Maven 3.8+

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>at.klickmagiesoftware</groupId>
    <artifactId>epcqr</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Minimal Example

```java
import at.klickmagiesoftware.epcqr.*;

// Build payment data
EpcQrCodeData data = EpcQrCode.builder()
    .version(EpcVersion.V002)
    .encoding(CharacterEncoding.UTF_8)
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .build();

// Generate SVG
EpcQrCodeGenerator generator = new EpcQrCodeGenerator();
String svg = generator.generateSvg(data);
```

### Full Example with All Fields

```java
EpcQrCodeData data = EpcQrCode.builder()
    .version(EpcVersion.V001)
    .encoding(CharacterEncoding.UTF_8)
    .bic("GIBAATWW")
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .amount(new BigDecimal("1456.89"))
    .reference("INV-2024-001")
    .displayText("Invoice payment")
    .build();

EpcQrCodeGenerator generator = new EpcQrCodeGenerator();

// Generate with custom module size (6 pixels per module)
String svg = generator.generateSvg(data, 6);

// Save to file
Files.writeString(Path.of("payment-qr.svg"), svg);
```

### Using Text Instead of Reference

```java
EpcQrCodeData data = EpcQrCode.builder()
    .version(EpcVersion.V002)
    .encoding(CharacterEncoding.UTF_8)
    .receiverName("Max Mustermann")
    .iban("AT682011131032423628")
    .amount(99.99)
    .text("Invoice #12345 - Consulting Services")
    .build();

String svg = new EpcQrCodeGenerator().generateSvg(data);
```

## API Reference

### EpcQrCode.Builder

| Method | Description |
|--------|-------------|
| `version(EpcVersion)` | Set EPC version (V001 requires BIC, V002 makes BIC optional) |
| `encoding(CharacterEncoding)` | Set character encoding (UTF-8 recommended) |
| `bic(String)` | Set Bank Identifier Code (8 or 11 characters) |
| `receiverName(String)` | Set receiver name (max 70 characters, required) |
| `iban(String)` | Set IBAN (required) |
| `amount(BigDecimal)` | Set payment amount (0.01 to 999999999.99 EUR) |
| `amount(double)` | Set payment amount (convenience method) |
| `purpose(String)` | Set 4-character purpose code |
| `reference(String)` | Set reconciliation reference (max 35 chars) |
| `text(String)` | Set reconciliation text (max 140 chars) |
| `displayText(String)` | Set display text (max 70 chars, not transmitted) |
| `build()` | Build and validate the data |

### EpcQrCodeGenerator

| Method | Description |
|--------|-------------|
| `generateSvg(EpcQrCodeData)` | Generate SVG with default module size (4 pixels) |
| `generateSvg(EpcQrCodeData, int)` | Generate SVG with custom module size |
| `formatPayload(EpcQrCodeData)` | Get the raw EPC payload string |

### Enums

**EpcVersion:**
- `V001` - BIC is mandatory
- `V002` - BIC is optional

**CharacterEncoding:**
- `UTF_8` (code 1) - Recommended
- `ISO_8859_1` (code 2)
- `ISO_8859_2` (code 3)
- `ISO_8859_4` (code 4)
- `ISO_8859_5` (code 5)
- `ISO_8859_7` (code 6)
- `ISO_8859_10` (code 7)
- `ISO_8859_15` (code 8)

## Field Constraints

| Field | Constraint |
|-------|------------|
| Receiver Name | 1-70 characters, required |
| IBAN | Valid IBAN format, required |
| BIC | 8 or 11 characters (required for V001) |
| Amount | 0.01 to 999999999.99 EUR, max 2 decimal places |
| Purpose | Exactly 4 characters if present |
| Reference | Max 35 characters |
| Text | Max 140 characters |
| Display Text | Max 70 characters |
| Total Payload | Max 331 bytes |

**Note:** Reference and Text are mutually exclusive. Setting one clears the other.

## SVG Output

The generated SVG includes:
- Proper quiet zone (4 modules) as required by QR code specification
- ViewBox-based sizing for scalability
- Error correction level M (~15% redundancy) per PSA spec
- Maximum QR version 13 (69x69 modules)

Example SVG structure:
```xml
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 292 292" width="292" height="292">
  <rect width="100%" height="100%" fill="white"/>
  <rect x="16" y="16" width="4" height="4" fill="black"/>
  <!-- ... more modules ... -->
</svg>
```

## Error Handling

The library throws specific exceptions for different error conditions:

```java
try {
    EpcQrCodeData data = EpcQrCode.builder()
        .version(EpcVersion.V001)
        .receiverName("Test")
        .iban("INVALID")
        .build();
} catch (ValidationException e) {
    // Contains list of validation errors
    System.out.println(e.getErrors());
}
```

Exception hierarchy:
- `EpcQrCodeException` - Base exception
  - `ValidationException` - Validation errors (contains error list)
  - `GenerationException` - QR code generation failures

## Building

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package
```

## Project Structure

```
src/main/java/at/klickmagiesoftware/epcqr/
├── EpcQrCode.java              # Builder entry point
├── EpcQrCodeData.java          # Immutable data record
├── EpcQrCodeGenerator.java     # SVG generation
├── EpcVersion.java             # Version enum
├── CharacterEncoding.java      # Encoding enum
├── validation/
│   ├── EpcQrCodeValidator.java # Main validator
│   ├── IbanValidator.java      # IBAN validation
│   ├── BicValidator.java       # BIC validation
│   ├── AmountValidator.java    # Amount validation
│   └── ValidationResult.java   # Validation result
├── format/
│   └── EpcPayloadFormatter.java # Payload formatting
├── render/
│   └── SvgRenderer.java        # SVG rendering
└── exception/
    ├── EpcQrCodeException.java # Base exception
    ├── ValidationException.java
    └── GenerationException.java
```

## Specification Reference

This library implements the PSA specification:
- **Document**: "Application of QR-Code for Initiating of Credit Transfers"
- **Version**: 3.0 (01.05.2021)
- **Standard**: ISO 18004 (QR Code bar code symbology specification)

## License

See [LICENSE](LICENSE) file for details.
