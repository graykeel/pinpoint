package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;

/**
 * 线程初始化拦截器
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-14 14:45
 */
public class AsyncInitInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final MethodDescriptor descriptor;

    private final TraceContext traceContext;

    private final InterceptorScope scope;

    public AsyncInitInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
        recorder.recordApi(descriptor, new Object[]{args});
        AsyncContext asyncContext = recorder.recordNextAsyncContext();
        scope.getCurrentInvocation().setAttachment(asyncContext);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            if (throwable != null) {
                SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
