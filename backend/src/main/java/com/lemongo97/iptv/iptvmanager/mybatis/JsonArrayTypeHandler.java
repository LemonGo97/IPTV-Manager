package com.lemongo97.iptv.iptvmanager.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import tools.jackson.databind.ObjectMapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JsonArrayTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    // 通过构造函数传入具体的类型（例如 List.class）
    public JsonArrayTypeHandler(Class<T> type) {
        if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("序列化 JSON 失败", e);
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toArray(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toArray(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toArray(cs.getString(columnIndex));
    }

    private List<T> toArray(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            // 核心：利用 Java 的反序列化机制直接转成对应的具体类型
            return objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            return null;
        }
    }
}