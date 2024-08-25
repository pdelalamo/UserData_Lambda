# UserDataLambda

## Overview

`UserDataLambda` is an AWS Lambda function implemented in Java that handles user data for the FitMyMacros application. This Lambda function processes incoming events (representing user data) and stores or updates the data in a DynamoDB table named `FitMyMacros`.

The function uses the AWS SDK for Java to interact with DynamoDB and supports various data types, including strings, numbers, booleans, lists, and maps.

## Features

- **AWS Lambda Handler**: Implements the `RequestHandler` interface from AWS Lambda, enabling it to handle incoming requests.
- **DynamoDB Integration**: Inserts or updates user data in a DynamoDB table.
- **Custom Data Handling**: Supports multiple data types such as Strings, Numbers, Booleans, Lists, and Maps.
- **Error Handling**: Provides basic error handling and returns success or error responses based on the outcome of the operation.

## AWS Services Used

- **Lambda**: The core service that runs this function in response to events.
- **DynamoDB**: A NoSQL database service used to store and manage user data.

## Installation

### Prerequisites

- Java 8 or higher
- Maven (for building the project)
- AWS SDK for Java (included in the project dependencies)

### Build

To build the project, run the following Maven command:

mvn clean install

Deployment
Create the DynamoDB Table:

Ensure that a DynamoDB table named FitMyMacros exists in your AWS account.
Define appropriate partition keys and indexes as per your application needs.
Deploy the Lambda:

Package the Lambda code into a .zip file or using AWS SAM/Serverless Framework.
Deploy the Lambda function via the AWS Management Console, AWS CLI, or an automated deployment tool.

Code Structure
handleRequest: The main entry point of the Lambda function that processes the incoming event and routes the data to DynamoDB.
putItemInDynamoDB: Converts the event data into a format suitable for DynamoDB and inserts or updates the item in the table.
buildAttributeValue: A helper method to convert various data types into AttributeValue objects, which are used by DynamoDB.
buildSuccessResponse: Generates a successful response with a status code of 200.
buildErrorResponse: Generates an error response with an appropriate message.
