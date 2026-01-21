# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java 25 library for generating EPC (European Payments Council) QR Codes for SEPA Credit Transfers, compliant with the PSA (Payment Services Austria) specification version 3.0.

## Build Commands

```bash
mvn compile              # Compile the project
mvn test                 # Run all tests
mvn test -Dtest=EpcQrCodeTest                    # Run single test class
mvn test -Dtest=EpcQrCodeTest#shouldBuildMinimalValidData  # Run specific test method
mvn package              # Create JAR package
```

## Architecture

**Layered architecture with clear separation of concerns:**

```
PUBLIC API LAYER
  EpcQrCode (Builder) → EpcQrCodeGenerator → EpcQrCodeData (immutable record)
                              ↓
DOMAIN/CORE LAYER
  CharacterEncoding, EpcVersion, Validators, EpcPayloadFormatter
                              ↓
RENDER LAYER
  SvgRenderer (BitMatrix → SVG)
                              ↓
INFRASTRUCTURE LAYER
  ZXing QR Code Library
```

**Key data flow:** User Input → Builder → Validation → EpcQrCodeData → EpcPayloadFormatter → ZXing BitMatrix → SvgRenderer → SVG String

**Entry points:**
- `EpcQrCode.java` - Fluent builder pattern entry point
- `EpcQrCodeGenerator.java` - SVG generation logic
- `EpcQrCodeValidator.java` - Validation orchestrator

## Key Design Patterns

- **Immutability**: All data objects use Java records (`EpcQrCodeData`, `ValidationResult`)
- **Builder Pattern**: Fluent API with fail-fast validation at build time
- **Unchecked Exceptions**: `ValidationException` and `GenerationException` extend `RuntimeException` (validation errors = programming errors)
- **Mutual Exclusivity**: `reference` and `text` fields are mutually exclusive; setting one clears the other

## Validation Rules (PSA Spec)

- BIC: 8 or 11 chars (mandatory for V001, optional for V002)
- Receiver name: 1-70 chars (required)
- IBAN: Valid format with checksum (required)
- Amount: 0.01 to 999999999.99 EUR, max 2 decimal places
- Purpose: Exactly 4 chars if present
- Reference: Max 35 chars (mutually exclusive with text)
- Text: Max 140 chars (mutually exclusive with reference)
- Total payload: Max 331 bytes

## Dependencies

- `com.google.zxing:core:3.5.4` - QR code generation (only external dependency)
- JUnit 5 + AssertJ for testing

## Notes

- ISO-8859-10 encoding is defined in PSA spec but unavailable in standard JDK
- SVG output includes proper 4-module quiet zone per QR spec
- Error correction level M (~15% redundancy) per PSA spec
- Maximum QR version 13 (69x69 modules)
