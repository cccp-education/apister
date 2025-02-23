package api.domain;

import static api.domain.SliderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import api.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SliderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Slider.class);
        Slider slider1 = getSliderSample1();
        Slider slider2 = new Slider();
        assertThat(slider1).isNotEqualTo(slider2);

        slider2.setId(slider1.getId());
        assertThat(slider1).isEqualTo(slider2);

        slider2 = getSliderSample2();
        assertThat(slider1).isNotEqualTo(slider2);
    }
}
