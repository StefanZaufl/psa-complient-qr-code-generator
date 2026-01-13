package at.klickmagiesoftware.epcqr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpcVersionTest {

    @Test
    void v001ShouldHaveCorrectCode() {
        assertThat(EpcVersion.V001.getCode()).isEqualTo("001");
    }

    @Test
    void v002ShouldHaveCorrectCode() {
        assertThat(EpcVersion.V002.getCode()).isEqualTo("002");
    }

    @Test
    void v001ShouldRequireBic() {
        assertThat(EpcVersion.V001.isBicMandatory()).isTrue();
    }

    @Test
    void v002ShouldNotRequireBic() {
        assertThat(EpcVersion.V002.isBicMandatory()).isFalse();
    }
}
