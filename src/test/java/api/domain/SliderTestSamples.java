package api.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SliderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Slider getSliderSample1() {
        return new Slider().id(1L).presentation("presentation1");
    }

    public static Slider getSliderSample2() {
        return new Slider().id(2L).presentation("presentation2");
    }

    public static Slider getSliderRandomSampleGenerator() {
        return new Slider().id(longCount.incrementAndGet()).presentation(UUID.randomUUID().toString());
    }
}
