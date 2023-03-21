package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.IJobHandlerInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title IJobHandlerTransform
 * @Description TODO
 * @date 2022/10/12 23:16
 * @since 1.0.0
 */
public class IJobHandlerTransform implements TransformCallback {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(PluginConstants.XXL_JOB_SCOPE);
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                classfileBuffer);
        target.addField(AsyncContextAccessor.class);
        for (InstrumentMethod instrumentMethod : target.getDeclaredConstructors()) {
            instrumentMethod.addScopedInterceptor(IJobHandlerInterceptor.ConstructInterceptor.class,scope, ExecutionPolicy.ALWAYS);
        }
        final InstrumentMethod executeMethod = target.getDeclaredMethod("execute",
                "java.lang.String");
        if (executeMethod != null) {
            executeMethod.addScopedInterceptor(IJobHandlerInterceptor.ExecuteInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        }
        final InstrumentMethod executeMethod2 = target.getDeclaredMethod("execute");
        if (executeMethod2!=null){
            executeMethod2.addScopedInterceptor(IJobHandlerInterceptor.ExecuteInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        }
        return target.toBytecode();
    }
}
