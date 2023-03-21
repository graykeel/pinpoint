package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseAdaptor;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.Collection;

/**
 * @author haiman
 * @Title FullHttpResponseAdaptor
 * @Description TODO
 * @date 2022/11/9 14:31
 * @since 1.0.0
 */
public class FullHttpResponseAdaptor implements ResponseAdaptor<FullHttpResponse> {
    @Override
    public boolean containsHeader(FullHttpResponse response, String name) {
        return response.headers().contains(name);
    }

    @Override
    public void setHeader(FullHttpResponse response, String name, String value) {
        response.headers().set(name,value);
    }

    @Override
    public void addHeader(FullHttpResponse response, String name, String value) {
        response.headers().add(name,value);
    }

    @Override
    public String getHeader(FullHttpResponse response, String name) {
        return response.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(FullHttpResponse response, String name) {
        return response.headers().getAll(name);
    }

    @Override
    public Collection<String> getHeaderNames(FullHttpResponse response) {
        return response.headers().names();
    }
}
