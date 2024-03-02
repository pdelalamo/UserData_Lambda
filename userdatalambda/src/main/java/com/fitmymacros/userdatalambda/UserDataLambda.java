package com.fitmymacros.userdatalambda;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

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
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(eventData.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> AttributeValue.builder()
                                        .s(e.getValue())
                                        .build())))
                .build();

        ddbClient.putItem(request);
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
