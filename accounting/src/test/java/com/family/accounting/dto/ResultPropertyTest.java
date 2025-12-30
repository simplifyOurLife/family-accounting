package com.family.accounting.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API响应JSON格式属性测试
 * <p>
 * Feature: family-accounting, Property 16: API响应JSON格式
 * Validates: Requirements 9.2
 * <p>
 * 验证所有API响应都能正确序列化为有效的JSON格式
 */
class ResultPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Property 16: API响应JSON格式
     * For any Result object with any valid data, serializing to JSON should produce
     * valid JSON format.
     */
    @Property(tries = 100)
    void resultShouldSerializeToValidJson(
            @ForAll @IntRange(min = 100, max = 599) int code,
            @ForAll @StringLength(max = 200) String message,
            @ForAll @StringLength(max = 500) String data) throws JsonProcessingException {
        // Given: a Result object with arbitrary values
        Result<String> result = new Result<>(code, message, data);

        // When: serializing to JSON
        String json = objectMapper.writeValueAsString(result);

        // Then: the JSON should be valid and parseable
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Verify it can be parsed back
        Result<?> parsed = objectMapper.readValue(json, Result.class);
        assertNotNull(parsed);
        assertEquals(code, parsed.getCode());
        assertEquals(message, parsed.getMessage());
    }

    /**
     * Property 16: API响应JSON格式 - 成功响应
     * For any success Result, the JSON should contain code 200.
     */
    @Property(tries = 100)
    void successResultShouldHaveCode200(
            @ForAll @StringLength(max = 500) String data) throws JsonProcessingException {
        // Given: a success Result
        Result<String> result = Result.success(data);

        // When: serializing to JSON
        String json = objectMapper.writeValueAsString(result);

        // Then: the JSON should contain code 200
        assertTrue(json.contains("\"code\":200"));

        // And should be parseable
        Result<?> parsed = objectMapper.readValue(json, Result.class);
        assertEquals(200, parsed.getCode());
    }

    /**
     * Property 16: API响应JSON格式 - 错误响应
     * For any error Result with custom code, the JSON should preserve the error
     * code.
     */
    @Property(tries = 100)
    void errorResultShouldPreserveErrorCode(
            @ForAll @IntRange(min = 400, max = 599) int errorCode,
            @ForAll @StringLength(min = 1, max = 200) String errorMessage) throws JsonProcessingException {
        // Given: an error Result
        Result<Void> result = Result.error(errorCode, errorMessage);

        // When: serializing to JSON
        String json = objectMapper.writeValueAsString(result);

        // Then: the JSON should preserve the error code
        Result<?> parsed = objectMapper.readValue(json, Result.class);
        assertEquals(errorCode, parsed.getCode());
        assertEquals(errorMessage, parsed.getMessage());
    }

    /**
     * Property 16: API响应JSON格式 - 中文字符支持
     * For any Result with Chinese characters, the JSON should preserve them
     * correctly.
     */
    @Property(tries = 100)
    void resultShouldPreserveChineseCharacters(
            @ForAll("chineseStrings") String chineseMessage) throws JsonProcessingException {
        // Given: a Result with Chinese message
        Result<String> result = Result.success(chineseMessage, chineseMessage);

        // When: serializing and deserializing
        String json = objectMapper.writeValueAsString(result);
        Result<?> parsed = objectMapper.readValue(json, Result.class);

        // Then: Chinese characters should be preserved
        assertEquals(chineseMessage, parsed.getMessage());
        assertEquals(chineseMessage, parsed.getData());
    }

    @Provide
    Arbitrary<String> chineseStrings() {
        return Arbitraries.strings()
                .withChars('家', '庭', '记', '账', '系', '统', '成', '功', '失', '败',
                        '用', '户', '密', '码', '登', '录', '注', '册', '分', '类')
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    /**
     * Property 16: API响应JSON格式 - null数据处理
     * For any Result with null data, the JSON should be valid.
     */
    @Property(tries = 100)
    void resultWithNullDataShouldSerializeToValidJson(
            @ForAll @IntRange(min = 100, max = 599) int code,
            @ForAll @StringLength(max = 200) String message) throws JsonProcessingException {
        // Given: a Result with null data
        Result<Object> result = new Result<>(code, message, null);

        // When: serializing to JSON
        String json = objectMapper.writeValueAsString(result);

        // Then: the JSON should be valid
        assertNotNull(json);
        Result<?> parsed = objectMapper.readValue(json, Result.class);
        assertNull(parsed.getData());
    }

    /**
     * Property 16: API响应JSON格式 - 复杂对象数据
     * For any Result with complex nested data, the JSON should be valid.
     */
    @Property(tries = 100)
    void resultWithComplexDataShouldSerializeToValidJson(
            @ForAll @StringLength(max = 100) String name,
            @ForAll @IntRange(min = 0, max = 1000000) int amount) throws JsonProcessingException {
        // Given: a Result with complex data
        TestData testData = new TestData(name, amount);
        Result<TestData> result = Result.success(testData);

        // When: serializing to JSON
        String json = objectMapper.writeValueAsString(result);

        // Then: the JSON should be valid and parseable
        assertNotNull(json);
        assertTrue(json.contains("\"code\":200"));
        assertTrue(json.contains("\"data\":{"));
    }

    /**
     * 测试用的复杂数据类
     */
    static class TestData {
        private String name;
        private int amount;

        public TestData() {
        }

        public TestData(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
