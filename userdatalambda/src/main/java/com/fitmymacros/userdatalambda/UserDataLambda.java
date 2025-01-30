package com.fitmymacros.userdatalambda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class UserDataLambda implements RequestHandler<Map<String, Object>, Object> {

    private static final String TABLE_NAME = "FitMyMacros";
    private final DynamoDbClient ddbClient = DynamoDbClient.create();

    @Override
    public Object handleRequest(Map<String, Object> event, Context context) {
        try {
            insertOrUpdateItem(event);
            return createSuccessResponse();
        } catch (Exception exception) {
            exception.printStackTrace();
            return createErrorResponse(exception.getMessage());
        }
    }

    /**
     * Inserts or updates the user-related data in the DynamoDB table.
     *
     * @param eventData the data representing user information to be stored
     */
    private void insertOrUpdateItem(Map<String, Object> eventData) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(buildDynamoDBItem(eventData))
                .build();

        ddbClient.putItem(request);
    }

    /**
     * Builds a DynamoDB item from the event data.
     *
     * @param eventData the given event data map
     * @return the constructed item map for DynamoDB
     */
    private Map<String, AttributeValue> buildDynamoDBItem(Map<String, Object> eventData) {
        return eventData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> convertToAttributeValue(entry.getValue())
                ));
    }

    /**
     * Converts a given Java object to a DynamoDB AttributeValue object according to its type.
     *
     * @param entry the object to be converted
     * @return the equivalent AttributeValue object
     */
    private AttributeValue convertToAttributeValue(Object entry) {
        if (entry instanceof String) {
            return AttributeValue.builder().s((String) entry).build();
        } else if (entry instanceof String[]) {
            return AttributeValue.builder().ss((String[]) entry).build();
        } else if (entry instanceof List<?>) {
            return convertListToAttributeValue((List<?>) entry);
        } else if (entry instanceof Integer) {
            return AttributeValue.builder().n(entry.toString()).build();
        } else if (entry instanceof Boolean) {
            return AttributeValue.builder().bool((Boolean) entry).build();
        } else if (entry instanceof Map<?, ?>) {
            return convertMapToAttributeValue((Map<?, ?>) entry);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + entry);
        }
    }

    /**
     * Converts a List<?> to an AttributeValue containing a list of AttributeValues.
     *
     * @param list the list to convert
     * @return the AttributeValue representing the list
     */
    private AttributeValue convertListToAttributeValue(List<?> list) {
        List<AttributeValue> attributeValues = list.stream()
                .map(this::convertToAttributeValue)
                .collect(Collectors.toList());
        return AttributeValue.builder().l(attributeValues).build();
    }

    /**
     * Converts a Map<?, ?> to an AttributeValue containing a map of AttributeValues.
     *
     * @param map the map to convert
     * @return the AttributeValue representing the map
     */
    private AttributeValue convertMapToAttributeValue(Map<?, ?> map) {
        Map<String, AttributeValue> attributeValueMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> convertToAttributeValue(entry.getValue())
                ));
        return AttributeValue.builder().m(attributeValueMap).build();
    }

    /**
     * Builds a response map indicating a successful operation.
     *
     * @return the response map containing status and message
     */
    private Map<String, Object> createSuccessResponse() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("statusCode", 200);
        responseBody.put("body", "Successfully saved or updated data");
        return responseBody;
    }

    /**
     * Builds an error message indicating that an error occurred.
     *
     * @param errorMessage the error message to be displayed
     * @return the formatted error message string
     */
    private String createErrorResponse(String errorMessage) {
        return "Error occurred: " + errorMessage;
    }
}
