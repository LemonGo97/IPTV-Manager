package com.lemongo97.iptv.iptvmanager.configuration.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import tools.jackson.databind.ObjectMapper;

import java.sql.*;

public class JsonObjectTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    // 通过构造函数传入具体的类型（例如 List.class）
    public JsonObjectTypeHandler(Class<T> type) {
        if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("序列化 JSON 失败", e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getString(columnIndex));
    }

    private T toObject(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            // 核心：利用 Java 的反序列化机制直接转成对应的具体类型
            return objectMapper.readValue(content, type);
        } catch (Exception e) {
            return null;
        }
    }
}