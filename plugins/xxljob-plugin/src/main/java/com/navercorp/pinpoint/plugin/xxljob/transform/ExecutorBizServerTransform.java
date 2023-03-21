package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.ExecutorBizServerInterceptor;

import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title ExecutorBizServerTransform
 * @Description TODO
 * @date 2022/11/10 17:13
 * @since 1.0.0
 */
public class ExecutorBizServerTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(PluginConstants.XXL_JOB_SCOPE);
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        InstrumentMethod run = target.getDeclaredMethod("run", "com.xxl.job.core.biz.model.TriggerParam");
        run.addScopedInterceptor(ExecutorBizServerInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        return target.toBytecode();
    }
}
