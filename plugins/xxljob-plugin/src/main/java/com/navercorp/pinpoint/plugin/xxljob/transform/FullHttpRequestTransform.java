package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.field.accessor.AsyncStartFlagFieldAccessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title FullHttpRequestTransform
 * @Description TODO
 * @date 2022/10/28 18:36
 * @since 1.0.0
 */
public class FullHttpRequestTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(AsyncContextAccessor.class);
        target.addField(AsyncStartFlagFieldAccessor.class);
        return target.toBytecode();
    }
}
