package com.navercorp.pinpoint.plugin.xxljob;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AnnotationInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.xxljob.transform.*;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

/**
 * 任何Pinpoint Profiler插件都必须实现ProfilerPlugin接口
 * 然后实现里面的setup方法
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-17 17:53
 */
public class XxlJobPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final Matcher ssuperMacher = Matchers.newPackageBasedMatcher(new ArrayList<String>() {{
                                                                         add("com.xxl");
                                                                     }},
                new SuperClassInternalNameMatcherOperand(
                        "io.netty.channel.SimpleChannelInboundHandler",
                        true));
        transformTemplate.transform(ssuperMacher, EmbedHttpServerHandlerTransform.class);
        final Matcher fsuperMacher = Matchers.newPackageBasedMatcher(new ArrayList<String>() {{
                                                                         add("io.netty");
                                                                     }},
                new InterfaceInternalNameMatcherOperand(
                        "io.netty.handler.codec.http.FullHttpRequest",
                        true));
        transformTemplate.transform(fsuperMacher, FullHttpRequestTransform.class);
        final Matcher csuperMacher = Matchers.newPackageBasedMatcher(new ArrayList<String>() {{
                                                                         add("io.netty");
                                                                     }},
                new InterfaceInternalNameMatcherOperand(
                        "io.netty.channel.ChannelHandlerContext",
                        true));
        transformTemplate.transform(csuperMacher, ChannelHandlerContextTransform.class);
        transformTemplate.transform("io.netty.handler.codec.http.HttpMethod", HttpMethodTransform.class);
        transformTemplate.transform("com.xxl.job.core.biz.model.TriggerParam",TriggerParamTransform.class);
        transformTemplate.transform("com.xxl.job.core.context.XxlJobContext",XxlJobContextTransform.class);
        transformTemplate.transform("com.xxl.job.core.biz.impl.ExecutorBizImpl", ExecutorBizServerTransform.class);
        //异步线程JobThread，监听里面的run
        transformTemplate.transform("com.xxl.job.core.thread.JobThread", JobThreadTransform.class);
        //JobThread线程调用了FutureTask，监听get方法
        transformTemplate.transform("java.util.concurrent.FutureTask", FutureTransform.class);
        final Matcher superMacher = Matchers.newPackageBasedMatcher(new ArrayList<String>() {{
                                                                        add("com.xxl");
                                                                    }},
                new SuperClassInternalNameMatcherOperand(
                        "com.xxl.job.core.handler.IJobHandler",
                        true));
        transformTemplate.transform(superMacher, IJobHandlerTransform.class);
//        transformTemplate.transform("com.xxl.job.executor.service.jobhandler.SampleXxlJob", SampleXxlJobTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate matchableTransformTemplate) {
        this.transformTemplate = matchableTransformTemplate;
    }
}
