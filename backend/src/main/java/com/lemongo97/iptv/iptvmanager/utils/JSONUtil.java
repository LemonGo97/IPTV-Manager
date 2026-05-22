package com.lemongo97.iptv.iptvmanager.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JSON 工具类
 * 基于 Jackson 3.x 封装的静态 JSON 操作工具
 */
public class JSONUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ==================== 序列化方法 ====================

    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public static String toPrettyJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON pretty serialization failed", e);
        }
    }

    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON bytes serialization failed", e);
        }
    }

    // ==================== 反序列化方法 ====================

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    public static <T> T fromJsonString(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    public static <T> T fromJsonBytes(byte[] json, Class<T> clazz) {
        if (json == null || json.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON bytes deserialization failed", e);
        }
    }

    public static <T> T fromJsonBytes(byte[] json, TypeReference<T> typeReference) {
        if (json == null || json.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("JSON bytes deserialization failed", e);
        }
    }

    // ==================== JSON 节点操作 ====================

    public static JsonNode parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    public static Optional<JsonNode> getPathValue(String json, String path) {
        JsonNode root = parseJson(json);
        if (root == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(root.at(path));
    }

    public static Optional<String> getStringValue(String json, String path) {
        return getPathValue(json, path)
                .map(node -> node.isObject() || node.isArray() ? node.toString() : node.asText());
    }

    public static Optional<Integer> getIntValue(String json, String path) {
        return getPathValue(json, path)
                .filter(JsonNode::isInt)
                .map(JsonNode::asInt);
    }

    public static Optional<Long> getLongValue(String json, String path) {
        return getPathValue(json, path)
                .filter(JsonNode::isLong)
                .map(JsonNode::asLong);
    }

    public static Optional<Boolean> getBooleanValue(String json, String path) {
        return getPathValue(json, path)
                .filter(JsonNode::isBoolean)
                .map(JsonNode::asBoolean);
    }

    // ==================== 文件操作 ====================

    public static <T> T fromJsonFile(File file, Class<T> clazz) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON file reading failed", e);
        }
    }

    public static <T> T fromJsonFile(File file, TypeReference<T> typeReference) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(file, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("JSON file reading failed", e);
        }
    }

    public static <T> T fromJsonFile(Path path, Class<T> clazz) {
        if (path == null || !Files.exists(path)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(path.toFile(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON file reading failed", e);
        }
    }

    public static <T> T fromJsonStream(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON stream reading failed", e);
        }
    }

    public static void toJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            return;
        }
        try {
            OBJECT_MAPPER.writeValue(file, obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON file writing failed", e);
        }
    }

    public static void toPrettyJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            return;
        }
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON file writing failed", e);
        }
    }

    // ==================== 类型转换方法 ====================

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> toList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        return OBJECT_MAPPER.convertValue(obj, List.class);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JacksonException e) {
            throw new RuntimeException("JSON List deserialization failed", e);
        }
    }

    // ==================== 实用方法 ====================

    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    public static String mergeJson(String baseJson, String overrideJson) {
        try {
            JsonNode baseNode = OBJECT_MAPPER.readTree(baseJson);
            JsonNode overrideNode = OBJECT_MAPPER.readTree(overrideJson);

            if (baseNode.isObject() && overrideNode.isObject()) {
                ObjectNode merged = ((ObjectNode) baseNode).setAll((ObjectNode) overrideNode);
                return OBJECT_MAPPER.writeValueAsString(merged);
            }

            return baseJson;
        } catch (JacksonException e) {
            throw new RuntimeException("JSON merge failed", e);
        }
    }
}
