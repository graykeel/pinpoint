package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.plugin.xxljob.field.accessor.TraceParamFieldAccessor;
import com.navercorp.pinpoint.plugin.xxljob.field.accessor.TrigerHandlerFieldAccessor;

import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title XxlJobContextTransform
 * @Description TODO
 * @date 2022/11/14 16:26
 * @since 1.0.0
 */
public class XxlJobContextTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(TrigerHandlerFieldAccessor.class);
        return target.toBytecode();
    }
}