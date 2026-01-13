package at.klickmagiesoftware.epcqr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterEncodingTest {

    @Test
    void utf8ShouldHaveCode1() {
        assertThat(CharacterEncoding.UTF_8.getCode()).isEqualTo(1);
        assertThat(CharacterEncoding.UTF_8.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void iso88591ShouldHaveCode2() {
        assertThat(CharacterEncoding.ISO_8859_1.getCode()).isEqualTo(2);
        assertThat(CharacterEncoding.ISO_8859_1.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @ParameterizedTest
    @EnumSource(CharacterEncoding.class)
    void allEncodingsShouldHaveValidCodeAndCharset(CharacterEncoding encoding) {
        assertThat(encoding.getCode()).isBetween(1, 8);
        assertThat(encoding.getCharset()).isNotNull();
    }

    @Test
    void shouldHaveEightEncodings() {
        assertThat(CharacterEncoding.values()).hasSize(8);
    }

    @Test
    void encodingCodesShouldBeSequential() {
        assertThat(CharacterEncoding.UTF_8.getCode()).isEqualTo(1);
        assertThat(CharacterEncoding.ISO_8859_1.getCode()).isEqualTo(2);
        assertThat(CharacterEncoding.ISO_8859_2.getCode()).isEqualTo(3);
        assertThat(CharacterEncoding.ISO_8859_4.getCode()).isEqualTo(4);
        assertThat(CharacterEncoding.ISO_8859_5.getCode()).isEqualTo(5);
        assertThat(CharacterEncoding.ISO_8859_7.getCode()).isEqualTo(6);
        assertThat(CharacterEncoding.ISO_8859_10.getCode()).isEqualTo(7);
        assertThat(CharacterEncoding.ISO_8859_15.getCode()).isEqualTo(8);
    }
}
