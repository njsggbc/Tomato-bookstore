package cn.edu.nju.TomatoMall.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON和Map互转工具类
 * 用于DTO中JSON字符串字段和Map对象之间的转换
 */
@Slf4j
public class JsonMapConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * JSON字符串转Map<String, String>
     *
     * @param json JSON字符串
     * @return Map对象，转换失败返回空Map
     */
    public static Map<String, String> jsonToStringMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>(){});
        } catch (Exception e) {
            log.warn("JSON字符串转Map失败: {}, 错误: {}", json, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * JSON字符串转Map<String, Object>
     *
     * @param json JSON字符串
     * @return Map对象，转换失败返回空Map
     */
    public static Map<String, Object> jsonToObjectMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            log.warn("JSON字符串转ObjectMap失败: {}, 错误: {}", json, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * JSON字符串转List<String>
     *
     * @param json JSON字符串
     * @return List对象，转换失败返回空List
     */
    public static List<String> jsonToStringList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            log.warn("JSON字符串转List失败: {}, 错误: {}", json, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Map转JSON字符串
     *
     * @param map Map对象
     * @return JSON字符串，转换失败返回"{}"
     */
    public static String mapToJson(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Map转JSON字符串失败: {}, 错误: {}", map, e.getMessage());
            return "{}";
        }
    }

    /**
     * List转JSON字符串
     *
     * @param list List对象
     * @return JSON字符串，转换失败返回"[]"
     */
    public static String listToJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("List转JSON字符串失败: {}, 错误: {}", list, e.getMessage());
            return "[]";
        }
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 任意对象
     * @return JSON字符串，转换失败返回null
     */
    public static String objectToJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("对象转JSON字符串失败: {}, 错误: {}", obj, e.getMessage());
            return null;
        }
    }

    /**
     * JSON字符串转指定类型对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("JSON字符串转对象失败: {} -> {}, 错误: {}", json, clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * JSON字符串转指定类型对象（支持泛型）
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.warn("JSON字符串转泛型对象失败: {}, 错误: {}", json, e.getMessage());
            return null;
        }
    }

    /**
     * 验证JSON字符串格式是否正确
     *
     * @param json JSON字符串
     * @return 是否为有效JSON格式
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将Map中的值转换为字符串
     * 适用于前端传递的参数可能是各种类型的情况
     *
     * @param map 原始Map
     * @return 值都转为String的Map
     */
    public static Map<String, String> normalizeToStringMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (value != null) {
                result.put(key, value.toString());
            }
        });

        return result;
    }
}