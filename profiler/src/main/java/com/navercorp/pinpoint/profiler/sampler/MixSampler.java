package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.config.ConfigOnChange;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.profiler.sampler.CountingSamplerFactory.LEGACY_SAMPLING_RATE_NAME;
import static com.navercorp.pinpoint.profiler.sampler.CountingSamplerFactory.SAMPLING_RATE_NAME;

/**
 * @author haiman
 * @Title MixSampler
 * @Description TODO
 * @date 2023/1/5 16:41
 * @since 1.0.0
 */
public class MixSampler implements Sampler, ConfigOnChange {

    private final AtomicInteger counter = new AtomicInteger(0);

    private ProfilerConfig profilerConfig;

    private int samplingRate = 1;

    public MixSampler(ProfilerConfig profilerConfig){
        this.profilerConfig = profilerConfig;
        this.profilerConfig.setConfigOnChange(this);
    }

    @Override
    public boolean isSampling() {
        if (samplingRate <= 0) {
            return FalseSampler.INSTANCE.isSampling();
        }
        if (samplingRate == 1) {
            return TrueSampler.INSTANCE.isSampling();
        }else{
            int samplingCount = counter.getAndIncrement();
            int isSampling = MathUtils.floorMod(samplingCount, samplingRate);
            return isSampling == 0;
        }
    }

    @Override
    public boolean onChange() {
        int legacy = profilerConfig.readInt(LEGACY_SAMPLING_RATE_NAME, -1);
        if (legacy != -1) {
            samplingRate = legacy;
        } else {
            samplingRate = profilerConfig.readInt(SAMPLING_RATE_NAME, 1);
        }
        return true;
    }
}
