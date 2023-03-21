package com.navercorp.pinpoint.plugin.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * 构造器方法拦截器
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-17 12:11
 */
public class AsyncConstructorInterceptor implements AroundInterceptor2 {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final InterceptorScope scope;

    public AsyncConstructorInterceptor(InterceptorScope scope) {
        this.scope = scope;
    }

    @IgnoreMethod
    @Override
    public void before(Object target, Object arg0, Object arg1) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object result, Throwable throwable) {
//        AsyncContext asyncContext = (AsyncContext) scope.getCurrentInvocation().getAttachment();
//        ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
    }
}
