/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.strace.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import wangjubao.base.dubbo.context.TraceContextUtil;


/**
 * TraceContext method interceptor
 *
 * @author haiman
 */
public class GenerateTraceIdInterceptor implements AroundInterceptor0 {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public GenerateTraceIdInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, null);
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        TraceContextUtil.set(trace.getTraceId().getTransactionId());
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, null, result, throwable);
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        TraceContextUtil.remove();
    }

}
