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

    public Object handleRequest(Map<String, Object> event, Context context) {
        try {
            putItemInDynamoDB(event);
            return buildSuccessResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * This method takes the event that the Lambda receives, that represents the
     * data associated with an user, and inserts it, or updates it in the DynamoDB
     * table
     * 
     * @param eventData
     */
    private void putItemInDynamoDB(Map<String, Object> eventData) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(eventData.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> this.buildAttributeValue(e.getValue()))))
                .build();

        ddbClient.putItem(request);
    }

    /**
     * This method checks whether the given entry object is an instance of any of
     * the
     * expected types to receive on the json
     * that represents the event that the lambda handles, and in that case, it
     * returns the equivalent AttributeValue object. In case the object doesn't
     * match any of the expected types, an exception is thrown
     * 
     * @param entry
     * @return
     */
    private AttributeValue buildAttributeValue(Object entry) {
        if (entry instanceof String)
            return AttributeValue.builder()
                    .s(entry.toString())
                    .build();
        else if (entry instanceof String[])
            return AttributeValue.builder()
                    .ss((String[]) entry).build();
        else if (entry instanceof List<?>) {
            List<?> listEntry = (List<?>) entry;
            List<AttributeValue> attributeValues = new ArrayList<>();
            for (Object item : listEntry) {
                AttributeValue value = buildAttributeValue(item);
                if (value != null) {
                    attributeValues.add(value);
                }
            }
            return AttributeValue.builder()
                    .l(attributeValues)
                    .build();
        } else if (entry instanceof Integer)
            return AttributeValue.builder()
                    .n(entry.toString())
                    .build();
        else if (entry instanceof Boolean)
            return AttributeValue.builder()
                    .bool((Boolean) entry)
                    .build();
        else if (entry instanceof Map<?, ?>) {
            Map<?, ?> mapEntry = (Map<?, ?>) entry;
            Map<String, AttributeValue> attributeValueMap = new HashMap<>();
            for (Map.Entry<?, ?> map : mapEntry.entrySet()) {
                AttributeValue value = buildAttributeValue(map.getValue());
                if (value != null) {
                    attributeValueMap.put(map.getKey().toString(), value);
                }
            }
            return AttributeValue.builder()
                    .m(attributeValueMap)
                    .build();
        } else
            throw new IllegalArgumentException("The following value is not supported: " + entry);
    }

    private Map<String, Object> buildSuccessResponse() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("statusCode", 200);
        responseBody.put("body", "Successfully saved or updated data");
        return responseBody;
    }

    private String buildErrorResponse(String errorMessage) {
        return "Error occurred: " + errorMessage;
    }
}
