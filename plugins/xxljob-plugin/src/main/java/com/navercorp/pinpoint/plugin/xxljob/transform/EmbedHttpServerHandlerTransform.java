package com.navercorp.pinpoint.plugin.xxljob.transform;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.xxljob.PluginConstants;
import com.navercorp.pinpoint.plugin.xxljob.interceptor.EmbedHttpServerHandlerInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * @author haiman
 * @Title EmbedHttpServerHandlerTransform
 * @Description TODO
 * @date 2022/10/28 14:50
 * @since 1.0.0
 */
public class EmbedHttpServerHandlerTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(PluginConstants.XXL_JOB_SCOPE);
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        InstrumentMethod channelRead0 = target.getDeclaredMethod("channelRead0", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.FullHttpRequest");
        channelRead0.addScopedInterceptor(EmbedHttpServerHandlerInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        InstrumentMethod writeResponse = target.getDeclaredMethod("writeResponse", "io.netty.channel.ChannelHandlerContext", "boolean", "java.lang.String");
        writeResponse.addScopedInterceptor(EmbedHttpServerHandlerInterceptor.EmbedServerwriteResponseInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        InstrumentMethod process = target.getDeclaredMethod("process", "io.netty.handler.codec.http.HttpMethod", "java.lang.String", "java.lang.String", "java.lang.String");
        process.addScopedInterceptor(EmbedHttpServerHandlerInterceptor.EmbedServerwriteResponseInterceptor.class, scope, ExecutionPolicy.ALWAYS);
        return target.toBytecode();
    }
}
