package com.navercorp.pinpoint.plugin.xxljob;

import com.navercorp.pinpoint.common.trace.*;

/**
 * 插件常量
 *
 * @author YY
 * @version 1.0
 * @date 2019-05-17 17:44
 */
public class PluginConstants {
    /**
     * 插件的SCOPE
     */
    public static final String XXL_JOB_SCOPE = "XXL_JOB_ASYNC";

    /**
     * 插件的唯一身份，用了1998这个id
     */
    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(1998, XXL_JOB_SCOPE);
    /**
     * pinpoint追踪信息中显示的属性的定义，用了998这个id
     */
    public static final AnnotationKey ANNOTATION_KEY = AnnotationKeyFactory.of(995, "async.job", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
}
