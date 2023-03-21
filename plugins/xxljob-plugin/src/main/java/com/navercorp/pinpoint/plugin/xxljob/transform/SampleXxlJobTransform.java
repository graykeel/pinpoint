package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.SampleXxlJobInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author haiman
 * @Title SampleXxlJobTransform
 * @Description TODO
 * @date 2022/11/13 18:11
 * @since 1.0.0
 */
public class SampleXxlJobTransform implements TransformCallback {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        try {
            Class clazz = classLoader.loadClass(className);
            Method[] methods = clazz.getDeclaredMethods();
            List<Method> methodList = new ArrayList<Method>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(com.xxl.job.core.handler.annotation.XxlJob.class)) {
                    methodList.add(method);
                }
            }
            List<InstrumentMethod> instrumentMethodList = new ArrayList<>();
            Set<String> names = new HashSet<>();
            for (final Method methodO : methodList) {
                if (!names.contains(methodO.getName())) {
                    names.add(methodO.getName());
                    instrumentMethodList.addAll(target.getDeclaredMethods(new MethodFilter() {
                        @Override
                        public boolean accept(InstrumentMethod method) {
                            if (method.getName().equals(methodO.getName())) {
                                return true;
                            }
                            return false;
                        }
                    }));
                } else {
                    continue;
                }
            }
            for (InstrumentMethod instrumentMethod:instrumentMethodList){
                instrumentMethod.addInterceptor(SampleXxlJobInterceptor.class);
            }
        } catch (ClassNotFoundException e) {
            logger.error("class not found exception",e);
        }
        return target.toBytecode();
    }
}
