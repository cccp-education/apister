package api.service.mapper;

import static api.domain.SliderAsserts.*;
import static api.domain.SliderTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SliderMapperTest {

    private SliderMapper sliderMapper;

    @BeforeEach
    void setUp() {
        sliderMapper = new SliderMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSliderSample1();
        var actual = sliderMapper.toEntity(sliderMapper.toDto(expected));
        assertSliderAllPropertiesEquals(expected, actual);
    }
}
