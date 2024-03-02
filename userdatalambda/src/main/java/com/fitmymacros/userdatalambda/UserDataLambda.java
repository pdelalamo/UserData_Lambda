package com.fitmymacros.userdatalambda;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public class UserDataLambda implements RequestHandler<Map<String, String>, Object> {

    private static final String TABLE_NAME = "FitMyMacros";
    private final DynamoDbClient ddbClient = DynamoDbClient.create();

    public Object handleRequest(Map<String, String> event, Context context) {
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
    private void putItemInDynamoDB(Map<String, String> eventData) {
        Map<String, AttributeValue> itemAttributes = eventData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> AttributeValue.builder()
                                .s(e.getValue())
                                .build()));

        Map<String, AttributeValue> primaryKey = new HashMap<>();
        primaryKey.put("userId", AttributeValue.builder().s(eventData.get("userId").toString()).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(primaryKey)
                .attributeUpdates(itemAttributes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> AttributeValueUpdate.builder()
                                        .value(e.getValue())
                                        .action(AttributeAction.PUT)
                                        .build())))
                .conditionExpression("attribute_not_exists(userId)")
                .build();

        ddbClient.updateItem(request);
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
