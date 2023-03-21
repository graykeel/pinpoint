package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.util.GsonTool;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author haiman
 * @Title IJobHandlerInterceptor
 * @Description TODO
 * @date 2022/10/12 23:27
 * @since 1.0.0
 */
public final class IJobHandlerInterceptor {
    public static class ExecuteInterceptor implements AroundInterceptor {
        private final PLogger logger = PLoggerFactory.getLogger(getClass());
        private final boolean isDebug = logger.isDebugEnabled();
        private TraceContext traceContext;
        private MethodDescriptor methodDescriptor;
        public ExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
            this.traceContext = traceContext;
            this.methodDescriptor = methodDescriptor;
        }

        @Override
        public void before(Object target, Object[] args) {
            if (isDebug) {
                logger.beforeInterceptor(target, args);
            }
            try {
                traceContext.removeTraceObject();
                Trace trace = createTrace(target, args);
                if (trace == null) {
                    return;
                }
                if (!trace.canSampled()) {
                    return;
                }
                final SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordApi(methodDescriptor);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
                }
            }
        }

        protected Trace createTrace(Object target, Object[] args) {
            String executorParams = XxlJobContext.getXxlJobContext().getJobParam();
            Map<String,String> property = GsonTool.fromJson(executorParams, Map.class);
            try {
                XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
                Field field = xxlJobContext.getClass().getDeclaredField("jobParam");
                field.setAccessible(true);
                field.set(xxlJobContext,property.get("executorParams"));
                if (property.isEmpty()) {
                    return null;
                }
            } catch (NoSuchFieldException e) {
                logger.error("file not found exception",e);
            } catch (IllegalAccessException e) {
                logger.error("file not found exception",e);
            }
            Trace trace = createTrace(target, property);
            final SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(PluginConstants.SERVICE_TYPE);
            spanRecorder.recordApi(methodDescriptor);
            spanRecorder.recordRemoteAddress("LocalHost");
            spanRecorder.recordEndPoint("LocalHost");
            spanRecorder.recordAcceptorHost("LocalHost");
            spanRecorder.recordRpcName(property.get("executorHandler"));
            return trace;
        }

        private Trace createTrace(Object target, Map<String,String> msgs) {
            final TraceId traceId = populateTraceIdFromHeaders(traceContext, msgs);
            if (traceId != null) {
                return createContinueTrace(target, traceContext, msgs, traceId);
            } else {
                return createTrace0(target, traceContext, msgs);
            }
        }

        private TraceId populateTraceIdFromHeaders(TraceContext traceContext, Map<String,String> msgs) {
            final String transactionId = msgs.get(Header.HTTP_TRACE_ID.toString());
            final String spanID = msgs.get(Header.HTTP_SPAN_ID.toString());
            final String parentSpanID = msgs.get(Header.HTTP_PARENT_SPAN_ID.toString());
            final String flags = msgs.get(Header.HTTP_FLAGS.toString());

            if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
                return null;
            }

            return traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID),
                    Long.parseLong(spanID), Short.parseShort(flags));
        }

        private Trace createContinueTrace(Object target, TraceContext traceContext, Map<String,String> msgs,
                                          TraceId traceId) {
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. traceId:{}", traceId);
            }
            final Trace trace = traceContext.continueTraceObject(traceId);
            return trace;
        }

        Trace createTrace0(Object target, TraceContext traceContext, Map<String,String> msgs) {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace.");
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace.");
                }
            }
            return trace;
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
                recorder.recordApi(methodDescriptor);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("AFTER. Caused:{}", th.getMessage(), th);
                }
            } finally {
                trace.traceBlockEnd();
                traceContext.removeTraceObject();
                trace.close();
                AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
                if (asyncContext!=null){
                    asyncContext.close();
                }
            }
        }
    }

    public static class ConstructInterceptor implements AroundInterceptor {
        private final InterceptorScope scope;

        public ConstructInterceptor(InterceptorScope scope) {
            this.scope = scope;
        }

        @Override
        public void before(Object target, Object[] args) {

        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
            AsyncContext asyncContext = (AsyncContext) scope.getCurrentInvocation().getAttachment();
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

}


