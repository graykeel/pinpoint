package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.google.gson.Gson;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.field.accessor.TraceParamFieldAccessor;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.util.GsonTool;

import java.util.HashMap;
import java.util.Map;

import static com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils.SAMPLING_RATE_FALSE;

/**
 * @author haiman
 * @Title ExecutorBizServerInterceptor
 * @Description TODO
 * @date 2022/11/10 17:17
 * @since 1.0.0
 */
public class ExecutorBizServerInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private static final String SCOPE_NAME = "XXL_JOB_SCOPE";
    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    public ExecutorBizServerInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            Trace trace = traceContext.currentTraceObject();
            if (trace == null) {
                return;
            }
            if (!trace.canSampled()) {
                return;
            }
            TriggerParam triggerParam = (TriggerParam)args[0];
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM,GsonTool.toJson(args[0]));
            doInBeforeTrace(recorder, target, args);
        }finally {

        }
    }

    private void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
        recorder.recordApi(methodDescriptor,args);
        final TriggerParam triggerParam = (TriggerParam) args[0];
        final Trace trace = traceContext.currentRawTraceObject();
        final TraceId nextTraceId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextTraceId.getSpanId());
        if (GlueTypeEnum.BEAN == GlueTypeEnum.match(triggerParam.getGlueType())){
            Map<String,String> property = new HashMap<>();
            if (trace.canSampled()) {
                property.put(Header.HTTP_FLAGS.toString(), String.valueOf(nextTraceId.getFlags()));
                property.put(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
                property.put(Header.HTTP_PARENT_APPLICATION_TYPE.toString(),
                        String.valueOf(traceContext.getServerTypeCode()));
                property.put(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextTraceId.getParentSpanId()));
                property.put(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextTraceId.getSpanId()));
                property.put(Header.HTTP_TRACE_ID.toString(), nextTraceId.getTransactionId());
            } else {
                property.put(Header.HTTP_SAMPLED.toString(), SAMPLING_RATE_FALSE);
            }
            property.put("executorHandler",triggerParam.getExecutorHandler());
            property.put("executorParams",triggerParam.getExecutorParams());
            triggerParam.setExecutorParams(GsonTool.toJson(property));
        }


    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
