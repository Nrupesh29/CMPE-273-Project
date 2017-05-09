import boto3
from boto3.dynamodb.conditions import Key, Attr

def lambda_handler(event, context):
    # TODO implement

    # Get the service resource.
    dynamodb = boto3.resource('dynamodb')
    
    # Instantiate a table resource object without actually
    # creating a DynamoDB table. Note that the attributes of this table
    # are lazy-loaded: a request is not made nor are the attribute
    # values populated until the attributes
    # on the table resource are accessed or its load() method is called.
    table = dynamodb.Table('IoT')
    
    # Print out some data about the table.
    # This will cause a request to be made to DynamoDB and its attribute
    # values will be set based on the response.
    
    
    response1 = table.query(
        KeyConditionExpression=Key('deviceId').eq('000001'),
        Limit=1,
        ScanIndexForward=False
    )
    
    response2 = table.query(
        KeyConditionExpression=Key('deviceId').eq('000002'),
        Limit=1,
        ScanIndexForward=False
    )
    
    response3 = table.query(
        KeyConditionExpression=Key('deviceId').eq('000003'),
        Limit=1,
        ScanIndexForward=False
    )
    
    response1['Items'].append(response2['Items'][0])
    response1['Items'].append(response3['Items'][0])
    
    response1['Count'] += 2
    response1['ScannedCount'] += 2
    response1.pop('ResponseMetadata', None)
    
    return response1
