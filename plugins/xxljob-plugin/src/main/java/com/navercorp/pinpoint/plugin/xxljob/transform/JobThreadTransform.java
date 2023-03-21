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
import com.navercorp.pinpoint.plugin.xxljob.interceptor.AsyncConstructorInterceptor;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.AsyncRunInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * JobThread类run方法注入拦截器
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-16 16:25
 */
public class JobThreadTransform implements TransformCallback {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(PluginConstants.XXL_JOB_SCOPE);
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(AsyncContextAccessor.class);
        //获取构造方法注入拦截器，注意参数个数类型要对应
        InstrumentMethod  constructor = target.getConstructor("int","com.xxl.job.core.handler.IJobHandler");
//        if(constructor != null){
//            constructor.addScopedInterceptor(AsyncConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);
//        }
        //获取声明的run方法，注入拦截器
        InstrumentMethod run = target.getDeclaredMethod("run");
        if(run != null){
            run.addScopedInterceptor(AsyncRunInterceptor.class, scope, ExecutionPolicy.INTERNAL);
        }
        return target.toBytecode();
    }
}