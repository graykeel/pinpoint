package com.navercorp.pinpoint.plugin.xxljob;

import com.navercorp.pinpoint.common.trace.*;

/**
 * 元信息类
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-14 14:45
 */
public class JobTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(PluginConstants.SERVICE_TYPE);
        context.addAnnotationKey(PluginConstants.ANNOTATION_KEY);
    }
}
