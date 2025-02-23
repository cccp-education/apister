package api.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import api.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SliderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SliderDTO.class);
        SliderDTO sliderDTO1 = new SliderDTO();
        sliderDTO1.setId(1L);
        SliderDTO sliderDTO2 = new SliderDTO();
        assertThat(sliderDTO1).isNotEqualTo(sliderDTO2);
        sliderDTO2.setId(sliderDTO1.getId());
        assertThat(sliderDTO1).isEqualTo(sliderDTO2);
        sliderDTO2.setId(2L);
        assertThat(sliderDTO1).isNotEqualTo(sliderDTO2);
        sliderDTO1.setId(null);
        assertThat(sliderDTO1).isNotEqualTo(sliderDTO2);
    }
}
