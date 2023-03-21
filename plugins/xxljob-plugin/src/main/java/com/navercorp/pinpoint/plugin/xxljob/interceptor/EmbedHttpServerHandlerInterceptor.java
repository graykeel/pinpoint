package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.*;
import com.navercorp.pinpoint.bootstrap.plugin.response.DefaultServerResponseHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServerResponseHeaderRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.field.accessor.AsyncStartFlagFieldAccessor;
import com.xxl.job.core.util.GsonTool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

/**
 * @author haiman
 * @Title EmbedHttpServerHandlerInterceptor
 * @Description TODO
 * @date 2022/10/28 15:44
 * @since 1.0.0
 */
public class EmbedHttpServerHandlerInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;

    private final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;
    private RequestTraceReader<FullHttpRequest> requestTraceReader;
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<HttpMessage> requestTraceWriter;
    private final InterceptorScope scope;

    public EmbedHttpServerHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor,InterceptorScope scope) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(true, clientRequestAdaptor);
        requestTraceReader = new RequestTraceReader<>(traceContext, new FullHttpRequestAdaptor(), true);
        ClientHeaderAdaptor<HttpMessage> clientHeaderAdaptor = new HttpMessageClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(args)) {
            return;
        }

        FullHttpRequest request = (FullHttpRequest) args[1];
        final Trace trace = createTrace(request);
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }
        final SpanRecorder spanRecorder = trace.getSpanRecorder();
        // record root span
        spanRecorder.recordServiceType(PluginConstants.SERVICE_TYPE);
        spanRecorder.recordApi(methodDescriptor);
        final ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        NettyClientRequestWrapper clientRequestWrapper = new NettyClientRequestWrapper(request, channelHandlerContext);
        spanRecorder.recordRemoteAddress(channelHandlerContext.channel().remoteAddress().toString());
        spanRecorder.recordEndPoint(clientRequestWrapper.getDestinationId());
        spanRecorder.recordAcceptorHost(request.headers().get(HOST));
        spanRecorder.recordRpcName(request.uri());
        spanRecorder.recordAttribute(AnnotationKey.HTTP_PARAM, request.content().toString(CharsetUtil.UTF_8));
        final SpanEventRecorder recorder = trace.traceBlockBegin();//开始跟踪
        ((AsyncStartFlagFieldAccessor) args[1])._$PINPOINT$_setAsyncStartFlag(true);
        AsyncContextAccessorUtils.setAsyncContext(recorder.recordNextAsyncContext(), args[1]);
        recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
        recorder.recordApi(methodDescriptor);

        doInBeforeTrace(recorder, trace, target, args);
        AsyncContext asyncContext = recorder.recordNextAsyncContext();
        ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
        ((AsyncContextAccessor) request.method())._$PINPOINT$_setAsyncContext(recorder.recordNextAsyncContext());
        scope.getCurrentInvocation().setAttachment(asyncContext);

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validate(args)) {
            return;
        }
        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            trace.close();
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
            after0(target, args, result, throwable);
        }finally {
            trace.traceBlockEnd();
            this.traceContext.removeTraceObject();
            trace.close();
        }
    }

    private Trace createTrace(FullHttpRequest request) {
        final Trace trace = this.requestTraceReader.read(request);
        return trace;
    }

    private void after0(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        doInAfterTrace(recorder, target, args, result, throwable);
    }

    private void afterAsync(Object target, Object[] args, Object result, Throwable throwable) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 1);
        if (asyncContext == null) {
            logger.debug("AsyncContext not found");
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope.
        if (!leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (isAsyncTraceDestination(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        final ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        final HttpMessage httpMessage = (HttpMessage) args[1];
        this.clientRequestRecorder.record(recorder, new NettyClientRequestWrapper(httpMessage, channelHandlerContext), throwable);
    }

    private void deleteAsyncContext(final Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }

        trace.close();
        asyncContext.close();
    }

    private boolean leaveAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isAsyncTraceDestination(final Trace trace) {
        if (!trace.isAsync()) {
            return false;
        }

        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        return scope != null && !scope.isActive();
    }

    private boolean validate(Object[] args) {
        if (ArrayUtils.getLength(args) != 2) {
            return false;
        }

        if (!(args[0] instanceof ChannelHandlerContext)) {
            return false;
        }
        ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        Channel channel = channelHandlerContext.channel();
        if (channel == null) {
            return false;
        }

        if (!(args[1] instanceof HttpMessage)) {
            return false;
        }
        HttpMessage httpMessage = (HttpMessage) args[1];
        if (httpMessage.headers() == null) {
            return false;
        }
        if (!(args[1] instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }

    protected void doInBeforeTrace(SpanEventRecorder recorder, Trace trace, Object target, Object[] args) {
        // generate next trace id.
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(PluginConstants.SERVICE_TYPE);

        final ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        final HttpMessage httpMessage = (HttpMessage) args[1];
        final String host = getHost(channelHandlerContext);
        this.requestTraceWriter.write(httpMessage, nextId, host);
    }

    private String getHost(ChannelHandlerContext channelHandlerContext) {
        if (channelHandlerContext != null) {
            final Channel channel = channelHandlerContext.channel();
            if (channel != null) {
                return NettyUtils.getEndPoint(channel.remoteAddress());
            }
        }
        return null;
    }

    private void entryAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            scope.tryEnter();
        }
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to continue async trace. 'result is null'");
            }
            return null;
        }
        if (isDebug) {
            logger.debug("getAsyncTrace() trace {}, asyncContext={}", trace, asyncContext);
        }

        return trace;
    }

    private void before0(Trace trace, Object target, Object[] args) {
        if (!trace.canSampled()) {
            final HttpMessage httpMessage = (HttpMessage) args[1];
            this.requestTraceWriter.write(httpMessage);
            return;
        }
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        doInBeforeTrace(recorder, trace, target, args);
    }

    class HttpMessageClientHeaderAdaptor implements ClientHeaderAdaptor<HttpMessage> {
        private final PLogger logger = PLoggerFactory.getLogger(getClass());
        private final boolean isDebug = logger.isDebugEnabled();

        @Override
        public void setHeader(HttpMessage httpMessage, String name, String value) {
            final HttpHeaders headers = httpMessage.headers();
            if (headers != null && !headers.contains(name)) {
                headers.set(name, value);
                if (isDebug) {
                    logger.debug("Set header {}={}", name, value);
                }
            }
        }
    }

    class NettyClientRequestWrapper implements ClientRequestWrapper {
        private final HttpMessage httpMessage;
        private final ChannelHandlerContext channelHandlerContext;

        public NettyClientRequestWrapper(final HttpMessage httpMessage, final ChannelHandlerContext channelHandlerContext) {
            this.httpMessage = Objects.requireNonNull(httpMessage, "httpMessage");
            this.channelHandlerContext = channelHandlerContext;
        }


        @Override
        public String getDestinationId() {
            if (this.channelHandlerContext != null) {
                final Channel channel = this.channelHandlerContext.channel();
                if (channel != null) {
                    return NettyUtils.getEndPoint(channel.remoteAddress());
                }
            }
            return "Unknown";
        }

        @Override
        public String getUrl() {
            if (this.httpMessage instanceof HttpRequest) {
                return ((HttpRequest) httpMessage).uri();
            }
            return null;
        }

    }

   public static class EmbedServerwriteResponseInterceptor implements AroundInterceptor {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
        private final boolean isDebug = logger.isDebugEnabled();

        private final TraceContext traceContext;
        private final ServerResponseHeaderRecorder<FullHttpResponse> serverResponseHeaderRecorder;
        private final HttpStatusCodeRecorder httpStatusCodeRecorder;
        protected final MethodDescriptor methodDescriptor;
       private final InterceptorScope scope;

        public EmbedServerwriteResponseInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor,InterceptorScope scope) {
            this.traceContext = traceContext;
            this.methodDescriptor = methodDescriptor;
            this.serverResponseHeaderRecorder = new DefaultServerResponseHeaderRecorder<>(new FullHttpResponseAdaptor(),new ArrayList<String>());
            HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors(Collections.<String>emptyList());
            this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(httpStatusCodeErrors);
            this.scope = scope;
        }

        @Override
        public void before(Object target, Object[] args) {
            if (isDebug) {
                logger.beforeInterceptor(target, args);
            }
            AsyncContext asyncContext = ((AsyncContextAccessor)args[0])._$PINPOINT$_getAsyncContext();
            if (asyncContext != null) {
                Trace trace = this.getAsyncTrace(asyncContext);
                if (trace != null) {
                    this.entryAsyncTraceScope(trace);
                    try {
                        SpanEventRecorder recorder = trace.traceBlockBegin();
                        recorder.recordApi(methodDescriptor, new Object[]{args});
                    } catch (Throwable var6) {
                        logger.warn("BEFORE. Caused:{}", var6.getMessage(), var6);
                    }
                }
            }
        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
            if (this.isDebug) {
                this.logger.afterInterceptor(target, args, result, throwable);
            }
            AsyncContext asyncContext = ((AsyncContextAccessor)args[0])._$PINPOINT$_getAsyncContext();
            if (asyncContext != null) {
                Trace trace = asyncContext.currentAsyncTraceObject();
                if (trace != null) {
                    if (!this.leaveAsyncTraceScope(trace)) {
                        this.logger.warn("Failed to leave scope of async trace {}.", trace);
                        this.deleteAsyncContext(trace, asyncContext);
                    } else {
                        try {
                            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                            recorder.recordServiceType(PluginConstants.SERVICE_TYPE);
                            recorder.recordApi(methodDescriptor);
                            recorder.recordException(throwable);
                        } catch (Throwable var11) {
                            this.logger.warn("AFTER error. Caused:{}", var11.getMessage(), var11);
                        } finally {
                            trace.traceBlockEnd();
                            if (this.isAsyncTraceDestination(trace)) {
                                this.deleteAsyncContext(trace, asyncContext);
                            }
                        }

                    }
                }
            }
        }

       private Trace getAsyncTrace(AsyncContext asyncContext) {
           Trace trace = asyncContext.continueAsyncTraceObject();
           if (trace == null) {
               this.logger.warn("Failed to continue async trace. 'result is null'");
               return null;
           }
           final SpanRecorder spanRecorder = trace.getSpanRecorder();
           // record root span
           spanRecorder.recordServiceType(PluginConstants.SERVICE_TYPE);
           spanRecorder.recordApi(methodDescriptor);
           spanRecorder.recordRemoteAddress("UnKnow");
           spanRecorder.recordEndPoint("UnKnow");
           spanRecorder.recordAcceptorHost("UnKnow");
           spanRecorder.recordRpcName("UnKnow");
           return trace;
       }

       private void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
           if (this.isDebug) {
               this.logger.debug("Delete async trace {}.", trace);
           }
           trace.close();
           asyncContext.close();
       }

       private void entryAsyncTraceScope(Trace trace) {
           TraceScope scope = trace.getScope("##ASYNC_TRACE_SCOPE");
           if (scope != null) {
               scope.tryEnter();
           }

       }

       private boolean leaveAsyncTraceScope(Trace trace) {
           TraceScope scope = trace.getScope("##ASYNC_TRACE_SCOPE");
           if (scope != null) {
               if (!scope.canLeave()) {
                   return false;
               }
               scope.leave();
           }
           return true;
       }

       private boolean isAsyncTraceDestination(Trace trace) {
           if (!trace.isAsync()) {
               return false;
           } else {
               TraceScope scope = trace.getScope("##ASYNC_TRACE_SCOPE");
               return scope != null && !scope.isActive();
           }
       }
    }

}