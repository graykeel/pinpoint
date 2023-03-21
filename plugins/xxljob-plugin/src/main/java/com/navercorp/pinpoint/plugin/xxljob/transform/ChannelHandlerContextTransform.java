package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;

import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title ChannelHandlerContextTransform
 * @Description TODO
 * @date 2022/11/11 16:07
 * @since 1.0.0
 */
public class ChannelHandlerContextTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(AsyncContextAccessor.class);
        return target.toBytecode();
    }
}
