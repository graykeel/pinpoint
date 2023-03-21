package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;

/**
 * @author haiman
 * @Title SampleXxlJobInterceptor
 * @Description TODO
 * @date 2022/11/14 10:34
 * @since 1.0.0
 */
public class SampleXxlJobInterceptor  implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    public SampleXxlJobInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        // 3. Record service type
        recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
        // 4. record method signature and arguments
        recorder.recordApi(methodDescriptor);
        // 5. record exception if any.
        recorder.recordException(throwable);
        // 6. Trace doesn't provide a method to record return value. You have to record it as an attribute.
        recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        trace.traceBlockEnd();
    }
}
