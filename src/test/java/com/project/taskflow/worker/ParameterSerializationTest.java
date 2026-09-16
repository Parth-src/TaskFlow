package com.project.taskflow.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testA_NoParameters() throws JsonProcessingException {
        Map<String, Object> params = Map.of();
        String json = mapper.writeValueAsString(params);
        assertEquals("{}", json);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.isObject());
        assertEquals(0, node.size());
    }

    @Test
    void testB_Strings() throws JsonProcessingException {
        Map<String, Object> params = Map.of("email", "test@example.com");
        String json = mapper.writeValueAsString(params);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.has("email"));
        assertTrue(node.get("email").isTextual());
        assertEquals("test@example.com", node.get("email").asText());
    }

    @Test
    void testC_Numbers() throws JsonProcessingException {
        Map<String, Object> params = Map.of(
                "userId", 123,
                "amount", 5000.50
        );
        String json = mapper.writeValueAsString(params);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.has("userId"));
        assertTrue(node.get("userId").isInt());
        assertEquals(123, node.get("userId").asInt());

        assertTrue(node.has("amount"));
        assertTrue(node.get("amount").isDouble() || node.get("amount").isFloatingPointNumber());
        assertEquals(5000.50, node.get("amount").asDouble(), 0.001);
    }

    @Test
    void testD_Boolean() throws JsonProcessingException {
        Map<String, Object> params = Map.of("sendReceipt", true);
        String json = mapper.writeValueAsString(params);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.has("sendReceipt"));
        assertTrue(node.get("sendReceipt").isBoolean());
        assertTrue(node.get("sendReceipt").asBoolean());
    }

    @Test
    void testE_NestedObject() throws JsonProcessingException {
        Map<String, Object> params = Map.of(
                "customer", Map.of(
                        "id", 123,
                        "name", "Parth"
                )
        );
        String json = mapper.writeValueAsString(params);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.has("customer"));
        assertTrue(node.get("customer").isObject());
        assertEquals(123, node.get("customer").get("id").asInt());
        assertEquals("Parth", node.get("customer").get("name").asText());
    }

    @Test
    void testF_Array() throws JsonProcessingException {
        Map<String, Object> params = Map.of(
                "items", List.of("item1", "item2")
        );
        String json = mapper.writeValueAsString(params);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.has("items"));
        assertTrue(node.get("items").isArray());
        assertEquals(2, node.get("items").size());
        assertEquals("item1", node.get("items").get(0).asText());
        assertEquals("item2", node.get("items").get(1).asText());
    }
}
