package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

/**
 * @author haiman
 * @Title FullHttpRequestAdapter
 * @Description TODO
 * @date 2022/11/2 17:11
 * @since 1.0.0
 */
public class FullHttpRequestAdaptor implements RequestAdaptor<FullHttpRequest> {
    @Override
    public String getHeader(FullHttpRequest request, String name) {
        return request.headers().get(name);
    }

    @Override
    public Collection<String> getHeaderNames(FullHttpRequest request) {
        final Enumeration<String> headerNames = Collections.enumeration(request.headers().names());
        if (headerNames == null) {
            return Collections.emptySet();
        }
        return Collections.list(headerNames);
    }

    @Override
    public String getRpcName(FullHttpRequest request) {
        return request.uri();
    }

    @Override
    public String getEndPoint(FullHttpRequest request) {
        return request.headers().get(HOST);
    }

    @Override
    public String getRemoteAddress(FullHttpRequest request) {
//        request.
        return null;
    }

    @Override
    public String getAcceptorHost(FullHttpRequest request) {
        String  url = request.uri();
        final String acceptorHost = url != null ? NetworkUtils.getHostFromURL(url.toString()) : null;
        return acceptorHost;
    }
}
