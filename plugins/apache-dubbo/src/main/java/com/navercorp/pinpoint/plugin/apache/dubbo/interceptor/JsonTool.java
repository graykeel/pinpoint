package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author haiman
 * @Title GsonTool
 * @Description TODO
 * @date 2022/11/16 10:22
 * @since 1.0.0
 */
public class JsonTool {

    private final static PLogger logger = PLoggerFactory.getLogger(JsonTool.class);

    static ObjectMapper objectMapper = null;
    static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static {
        objectMapper = new ObjectMapper();
        // 未知字段报警,
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许null bean
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 不序列化空字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 允许 key 值没有双引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许 单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 使用科学计数法
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        // 设置时间格式
        DateFormat myDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
        objectMapper.setDateFormat(myDateFormat);
    }

    public static ObjectMapper get() {
        return objectMapper;
    }

    public static JsonNode fromNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            logger.error("Json转换成树对象时出错", e);
        }
        return null;
    }

    public static <T> T from(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static <T> T from(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }


    public static <T> T from(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonNode.traverse(), clazz);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static <T> T from(String json, Class<T> fatherClass, Class<?>... otherClass) {
        JavaType javaType = getComplexType(fatherClass, otherClass);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    /**
     * @param collectionClass 父类型
     * @param elementClasses  子类型
     * @return
     */
    public static JavaType getComplexType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static JavaType getComplexType(Class<?> collectionClass, JavaType... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    public static <T> List<T> fromArray(String json, Class<T> clazz) {
        JavaType javaType = getComplexType(ArrayList.class, clazz);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static <T> List<T> fromArray(JsonNode jsonNode, Class<T> clazz) {
        JavaType javaType = getComplexType(ArrayList.class, clazz);
        try {
            return objectMapper.readValue(jsonNode.traverse(), javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }


    public static <K, V> Map<K, V> fromMap(String json, Class<K> keyClass, Class<V> valueClass) {
        JavaType javaType = getComplexType(HashMap.class, keyClass, valueClass);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static <K, V> Map<K, V> fromMap(JsonNode jsonNode, Class<K> keyClass, Class<V> valueClass) {
        JavaType javaType = getComplexType(HashMap.class, keyClass, valueClass);
        try {
            return objectMapper.readValue(jsonNode.traverse(), javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static <K, V> List<Map<K, V>> fromListMap(String json, Class<K> keyClass, Class<V> valueClass) {
        JavaType javaType = getComplexType(HashMap.class, keyClass, valueClass);

        return from(json, ArrayList.class, javaType);
    }

    public static <K, V> Map<K, List<V>> fromMapList(String json, Class<K> keyClass, Class<V> valueClass) {
        JavaType keyType = objectMapper.getTypeFactory().constructSimpleType(keyClass, null);

        JavaType valueType = getComplexType(ArrayList.class, valueClass);

        JavaType mapType = objectMapper.getTypeFactory().constructMapLikeType(HashMap.class, keyType, valueType);

        try {
            return objectMapper.readValue(json, mapType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转换为字符串时出错", e);
        }
        return obj.toString();
    }

    public static String toJsonNoNull(Object obj) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Date) {
            Date date = (Date) obj;
            DateFormat dateFormat = objectMapper.getDateFormat();
            DateFormat localDateFormat = (DateFormat) dateFormat.clone();
            String strDate = localDateFormat.format(date);
            return strDate;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转换为字符串时出错", e);
        }
        return obj.toString();
    }

    public static String toJsonNoNull(TypeReference<?> typeReference, Object obj) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Date) {
            Date date = (Date) obj;
            DateFormat dateFormat = objectMapper.getDateFormat();
            DateFormat localDateFormat = (DateFormat) dateFormat.clone();
            String strDate = localDateFormat.format(date);
            return strDate;
        }
        try {
            return objectMapper.writerFor(typeReference).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转换为字符串时出错", e);
        }
        return obj.toString();
    }

    public static <T> T convertMapToBean(Map map, Class<T> clazz) {
        return from(toJson(map), clazz);
    }

    public static <T> Map<String, Object> beanToMap(T obj) {
        Map<String, Object> params = new HashMap(0);
        try {
            String json = toJson(obj);
            Map<String, Object> map = fromMap(json, String.class, Object.class);
            return map;
        } catch (Exception e) {
            logger.error("实体类转Map失败" + e);
        }
        return params;
    }

    public static <T, V> Map<String, V> beanToMap(T obj, Class<V> clazz) {
        Map<String, V> params = new HashMap(0);
        try {
            String json = toJson(obj);
            Map<String, V> map = fromMap(json, String.class, clazz);
            return map;
        } catch (Exception e) {
            logger.error("实体类转Map失败" + e);
        }
        return params;
    }

    public static <T> T from(String json, Class<T> fatherType, JavaType... otherType) {
        JavaType javaType = getComplexType(fatherType, otherType);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("Json转换成对象时出错", e);
        }
        return null;
    }

    /**
     * 将一个对象序列化成 json 字符串
     *
     * @param obj    *指定要序列化的对象
     * @param format *指定将时间序列化的格式
     * @return 返回序列化后的字符串
     */
    public static String toString(Object obj, String format) {
        if (obj != null) {
            if (format == null) {
                format = "yyyy-MM-dd HH:mm:ss";
            }
            return toJson(obj);
        }

        return null;
    }

    // 将一个 json 数组字符串序列化成一个对象列表
    public static <T> List<T> toList(String strJson, Class<T> clazz) {
        return fromArray(strJson, clazz);
    }

    // 将一个 json 字符串序列化成一个对象
    public static <T> T parse(String strJson, Class<T> clazz) {
        return from(strJson, clazz);
    }


    public static void main(String[] args) {
        System.out.println(JsonTool.toJsonNoNull(new Date()));
    }
}
