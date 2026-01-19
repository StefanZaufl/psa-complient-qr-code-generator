package at.klickmagiesoftware.epcqr;

import at.klickmagiesoftware.epcqr.exception.GenerationException;
import at.klickmagiesoftware.epcqr.exception.ValidationException;
import at.klickmagiesoftware.epcqr.format.EpcPayloadFormatter;
import at.klickmagiesoftware.epcqr.render.PngRenderer;
import at.klickmagiesoftware.epcqr.render.SvgRenderer;
import at.klickmagiesoftware.epcqr.validation.EpcQrCodeValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Generates EPC QR Code SVG and PNG images from validated data.
 *
 * <p>This generator produces both SVG and PNG output:
 * <ul>
 *   <li>SVG: Scalable, resolution-independent QR codes ideal for web and print</li>
 *   <li>PNG: Bitmap images suitable for direct file output and legacy systems</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * EpcQrCodeData data = EpcQrCode.builder()
 *     .version(EpcVersion.V002)
 *     .encoding(CharacterEncoding.UTF_8)
 *     .receiverName("Max Mustermann")
 *     .iban("AT682011131032423628")
 *     .amount(100.00)
 *     .build();
 *
 * EpcQrCodeGenerator generator = new EpcQrCodeGenerator();
 * 
 * // SVG generation
 * String svg = generator.generateSvg(data);
 * 
 * // PNG generation
 * byte[] png = generator.generatePng(data);
 * 
 * // Direct PNG file output
 * generator.generatePngFile(data, 8, Paths.get("payment.png"));
 * }</pre>
 */
public final class EpcQrCodeGenerator {

    /**
     * Error correction level M (~15% redundancy) as required by PSA specification.
     */
    private static final ErrorCorrectionLevel ERROR_CORRECTION = ErrorCorrectionLevel.M;

    /**
     * Maximum QR Code version (13 = 69x69 modules) as specified by PSA.
     */
    private static final int MAX_QR_VERSION = 13;

    /**
     * Default module size in pixels for SVG generation.
     */
    private static final int DEFAULT_MODULE_SIZE = 4;

    /**
     * Default module size in pixels for PNG generation.
     */
    private static final int DEFAULT_PNG_MODULE_SIZE = 8;

    private final QRCodeWriter qrCodeWriter;

    /**
     * Creates a new EPC QR Code generator.
     */
    public EpcQrCodeGenerator() {
        this.qrCodeWriter = new QRCodeWriter();
    }

    /**
     * Generates an SVG QR code with the default module size (4 pixels per module).
     *
     * @param data the validated EPC QR Code data
     * @return the SVG markup as a string
     * @throws GenerationException if QR code generation fails
     * @throws ValidationException if payload exceeds maximum size
     */
    public String generateSvg(EpcQrCodeData data) {
        return generateSvg(data, DEFAULT_MODULE_SIZE);
    }

    /**
     * Generates an SVG QR code with the specified module size.
     *
     * @param data the validated EPC QR Code data
     * @param moduleSize the size of each module in pixels
     * @return the SVG markup as a string
     * @throws GenerationException if QR code generation fails
     * @throws ValidationException if payload exceeds maximum size
     */
    public String generateSvg(EpcQrCodeData data, int moduleSize) {
        String payload = formatPayload(data);
        validatePayloadSize(data, payload);

        BitMatrix matrix = encodeQrCode(payload, data);
        return SvgRenderer.render(matrix, moduleSize);
    }

    /**
     * Generates a PNG QR code with the default module size (8 pixels per module).
     *
     * @param data the validated EPC QR Code data
     * @return the PNG data as a byte array
     * @throws GenerationException if QR code generation fails
     * @throws ValidationException if payload exceeds maximum size
     */
    public byte[] generatePng(EpcQrCodeData data) {
        return generatePng(data, DEFAULT_PNG_MODULE_SIZE);
    }

    /**
     * Generates a PNG QR code with the specified module size.
     *
     * @param data the validated EPC QR Code data
     * @param moduleSize the size of each module in pixels
     * @return the PNG data as a byte array
     * @throws GenerationException if QR code generation fails
     * @throws ValidationException if payload exceeds maximum size
     */
    public byte[] generatePng(EpcQrCodeData data, int moduleSize) {
        String payload = formatPayload(data);
        validatePayloadSize(data, payload);

        BitMatrix matrix = encodeQrCode(payload, data);
        return PngRenderer.render(matrix, moduleSize);
    }

    /**
     * Generates a PNG QR code and saves it directly to the specified file path.
     *
     * @param data the validated EPC QR Code data
     * @param moduleSize the size of each module in pixels
     * @param pngPath the file path where the PNG will be saved
     * @throws GenerationException if QR code generation fails
     * @throws ValidationException if payload exceeds maximum size
     * @throws IOException if file cannot be written
     */
    public void generatePngFile(EpcQrCodeData data, int moduleSize, Path pngPath) throws IOException {
        byte[] png = generatePng(data, moduleSize);
        
        // Create parent directories if they don't exist
        if (pngPath.getParent() != null) {
            Files.createDirectories(pngPath.getParent());
        }
        
        Files.write(pngPath, png);
    }

    /**
     * Formats the EPC QR Code data into the payload string without generating the QR code.
     *
     * <p>This is useful for debugging or when you need the raw payload for custom processing.
     *
     * @param data the EPC QR Code data
     * @return the formatted payload string
     */
    public String formatPayload(EpcQrCodeData data) {
        return EpcPayloadFormatter.format(data);
    }

    /**
     * Validates that the payload does not exceed the maximum size of 331 bytes.
     */
    private void validatePayloadSize(EpcQrCodeData data, String payload) {
        int payloadBytes = payload.getBytes(data.encoding().getCharset()).length;
        var result = EpcQrCodeValidator.validatePayloadSize(payloadBytes);
        if (!result.valid()) {
            throw new ValidationException(result.errors());
        }
    }

    /**
     * Encodes the payload as a QR code BitMatrix using ZXing.
     */
    private BitMatrix encodeQrCode(String payload, EpcQrCodeData data) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ERROR_CORRECTION);
        hints.put(EncodeHintType.CHARACTER_SET, data.encoding().getCharset().name());
        hints.put(EncodeHintType.QR_VERSION, MAX_QR_VERSION);
        hints.put(EncodeHintType.MARGIN, 0); // We handle quiet zone in SVG renderer

        try {
            // Using MAX_QR_VERSION dimensions; ZXing will use smaller if possible
            int size = (MAX_QR_VERSION * 4) + 17; // QR version formula: (version * 4) + 17
            return qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (WriterException e) {
            throw new GenerationException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
}
