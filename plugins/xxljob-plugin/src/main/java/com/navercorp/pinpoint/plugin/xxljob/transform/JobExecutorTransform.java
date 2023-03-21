package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.AsyncInitInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * XxlJobExecutor类registJobThread方法注入拦截器
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-14 16:49
 */
public class JobExecutorTransform implements TransformCallback {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                byte[] classFileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(PluginConstants.XXL_JOB_SCOPE);
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
        //获取声明的registJobThread方法
        InstrumentMethod registJobThread = target.getDeclaredMethod("registJobThread", "int","com.xxl.job.core.handler.IJobHandler","java.lang.String");
        if (registJobThread != null) {
            registJobThread.addScopedInterceptor(AsyncInitInterceptor.class, scope);
        }
        return target.toBytecode();
    }


}
