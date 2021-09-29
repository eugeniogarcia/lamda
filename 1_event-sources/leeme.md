# Antes de Nada

En este proyecto se introduce un plugin cuya misión es la de hacer despliegues reproducibles. ¿Qué quiere decir esto?. Lo que hace el plugin es asegurar que cuando se crea un jar, se eliminen las referencias al timestamp de creación, de modo que si el contenido del jar no cambia, y simplemente se ha regenerado en otro instante de tiempo, ambos jar sean iguales. Para eso eleminamos del jar lo referente al momento de creación.

El plugin que hace este trabajo lo podemos ver en el _pom.xml_:

```xml
      <plugin>
        <groupId>io.github.zlika</groupId>
        <artifactId>reproducible-build-maven-plugin</artifactId>
        <version>0.10</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>strip-jar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```

# Tipos invocaciones

A la hora de invocar a una lambda podemos hacerlo de tres formas:

- De forma sincrona. Los clientes que hacen uso de este patron son servicios de AWS como el _API Gateway_, o la CLI `aws lambda invoke --invocation-type RequestRespons`. 
Los servicios que soportan este patrón son _API Gateway, Amazon CloudFront (Lambda@Edge), Elastic Load Balancing (Application Load Balancer), Cognito, Lex, Alexa, Kinesis Data Firehose_
- De forma asíncrona. Servicios como _S3, SNS, Amazon SES, CloudFormation, CloudWatch Logs, CloudWatch Events, CodeCommit, Config_.
- Stream. Servicios como _Kinesis Data Streams, DynamoDB Streams, Simple Queue Service (SQS)_

## Evento sincrono

Podemos usar _sam_ para ver un evento ejemplo. Con esta instrucción vemos las fuentes de eventos disponibles:

```ps
sam local generate-event 

Commands:
  alexa-skills-kit
  alexa-smart-home
  apigateway
  appsync
  batch
  cloudformation
  cloudfront
  cloudwatch
  codecommit
  codepipeline
  cognito
  config
  connect
  dynamodb
  kinesis
  lex
  rekognition
  s3
  sagemaker
  ses
  sns
  sqs
  stepfunctions
```

Generemos un evento para el _Api Gateway_:

```ps
sam local generate-event apigateway aws-proxy 
```

```json
{
  "body": "eyJ0ZXN0IjoiYm9keSJ9",
  "resource": "/{proxy+}",
  "path": "/path/to/resource",
  "httpMethod": "POST",
  "isBase64Encoded": true,
  "queryStringParameters": {
    "foo": "bar"
  },
  "multiValueQueryStringParameters": {
    "foo": [
      "bar"
    ]
  },
  "pathParameters": {
    "proxy": "/path/to/resource"
  },
  "stageVariables": {
    "baz": "qux"
  },
  "headers": {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Encoding": "gzip, deflate, sdch",
    "Accept-Language": "en-US,en;q=0.8",
    "Cache-Control": "max-age=0",
    "CloudFront-Forwarded-Proto": "https",
    "CloudFront-Is-Desktop-Viewer": "true",
    "CloudFront-Is-Mobile-Viewer": "false",
    "CloudFront-Is-SmartTV-Viewer": "false",
    "CloudFront-Is-Tablet-Viewer": "false",
    "CloudFront-Viewer-Country": "US",
    "Host": "1234567890.execute-api.us-east-1.amazonaws.com",
    "Upgrade-Insecure-Requests": "1",
    "User-Agent": "Custom User Agent String",
    "Via": "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)",
    "X-Amz-Cf-Id": "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==",
    "X-Forwarded-For": "127.0.0.1, 127.0.0.2",
    "X-Forwarded-Port": "443",
    "X-Forwarded-Proto": "https"
  },
  "multiValueHeaders": {
    "Accept": [
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
    ],
    "Accept-Encoding": [
      "gzip, deflate, sdch"
    ],
    "Accept-Language": [
      "en-US,en;q=0.8"
    ],
    "Cache-Control": [
      "max-age=0"
    ],
    "CloudFront-Forwarded-Proto": [
      "https"
    ],
    "CloudFront-Is-Desktop-Viewer": [
      "true"
    ],
    "CloudFront-Is-Mobile-Viewer": [
      "false"
    ],
    "CloudFront-Is-SmartTV-Viewer": [
      "false"
    ],
    "CloudFront-Is-Tablet-Viewer": [
      "false"
    ],
    "CloudFront-Viewer-Country": [
      "US"
    ],
    "Host": [
      "0123456789.execute-api.us-east-1.amazonaws.com"
    ],
    "Upgrade-Insecure-Requests": [
      "1"
    ],
    "User-Agent": [
      "Custom User Agent String"
    ],
    "Via": [
      "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)"
    ],
    "X-Amz-Cf-Id": [
      "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA=="
    ],
    "X-Forwarded-For": [
      "127.0.0.1, 127.0.0.2"
    ],
    "X-Forwarded-Port": [
      "443"
    ],
    "X-Forwarded-Proto": [
      "https"
    ]
  },
  "requestContext": {
    "accountId": "123456789012",
    "resourceId": "123456",
    "stage": "prod",
    "requestId": "c6af9ac6-7b61-11e6-9a41-93e8deadbeef",
    "requestTime": "09/Apr/2015:12:34:56 +0000",
    "requestTimeEpoch": 1428582896000,
    "identity": {
      "cognitoIdentityPoolId": null,
      "accountId": null,
      "cognitoIdentityId": null,
      "caller": null,
      "accessKey": null,
      "sourceIp": "127.0.0.1",
      "cognitoAuthenticationType": null,
      "cognitoAuthenticationProvider": null,
      "userArn": null,
      "userAgent": "Custom User Agent String",
      "user": null
    },
    "path": "/prod/path/to/resource",
    "resourcePath": "/{proxy+}",
    "httpMethod": "POST",
    "apiId": "1234567890",
    "protocol": "HTTP/1.1"
  }
}
```

Cando recibamos un evento de este tipo, el runtime de lambda lo deserializará, o al menos lo intentará, en el argumento de entrada de nuestro handler. Podriamos usar la clase que modela este evento, _APIGatewayProxyRequestEvent_ que estaría disponible si añadimos esta dependencia al pom:

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-events</artifactId>
    <version>2.2.6</version>
  </dependency>
```

Tambien podemos usar un pojo que contenga aquellos campos que nos interesen. Esto es lo que haremos en este ejemplo:

```java
public class APIGatewayEvent {
    public String path;
    public Map<String, String> queryStringParameters;
}
```

En este caso estaríamos disponibilizando en nuestro handler estas dos propiedades. 

Al desplegar el lambda tendremos que indicar que queremos subscribirnos a un evento del Api GAteway. Tendremos que indicar cual es el recurso en el Api Gateway al que nos conectaremos, y que verbos admitiremos. Todo esto se define en el _template.yaml_:

````yaml
AWSTemplateFormatVersion: 2010-09-09
Transform: AWS::Serverless-2016-10-31
Description: chapter5

Globals:
  Api:
    OpenApiVersion: '3.0.1'

Resources:
  HelloAPIWorldLambda:
    Type: AWS::Serverless::Function
    Properties:
      Runtime: java8
      MemorySize: 512
      Handler: book.HelloWorldAPI::handler
      CodeUri: target/lambda.zip
      Events:
        MyApi:
          Type: Api
          Properties:
            Path: /foo
            Method: get
```

Podemos ver como en las propiedades de este lambda estamos definiendo los eventos a los que nos subscribimos, y concretamente estamos diciendo que es el Api Gateway, recurso _/foo_ y que lo haremos con el método _get_. En la sección Globals podemos incluir propiedades comunes a todos los artefactos que definamos en el _template.yaml_.

Probemos el lambda:

```ps
mvn package
```

```ps
$Env:CF_BUCKET="egsmartin"

sam deploy --s3-bucket $Env:CF_BUCKET --stack-name EventoAPIGW --capabilities CAPABILITY_IAM
```

## Pipeline

Vamos a construir dos lambdas que procesan eventos del API Gateway y escriben/leen de Dynamo DB. EN _template.yaml_ tenemos la configuración de los recursos que vamos a desplegar. Primero declaramos las propiedades comunes a todos los recursos:

```yaml
Globals:
  Function:
    Runtime: java8
    MemorySize: 512
    Timeout: 25
    Environment:
      Variables:
        LOCATIONS_TABLE: !Ref LocationsTable
  Api:
    OpenApiVersion: '3.0.1'
```

Destacar como definimos una variable de entorno, _LOCATIONS_TABLE_, con el contenido definido en un recurso del template. Definimos este recurso:

```yaml
Resources:
  LocationsTable:
    Type: AWS::Serverless::SimpleTable
    Properties:
      PrimaryKey:
        Name: locationName
        Type: String
```

Podemos ver que se trata de una tabla, de una tabla de Dynamo `AWS::Serverless::SimpleTable`. Llamamos al recurso `LocationsTable`. Las propiedades que hemos definido para el recurso son el primary key.

La primera de las lambdas:

```yaml
  WeatherEventLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: target/lambda.zip
      Handler: book.api.WeatherEventLambda::handler
      Policies:
        - DynamoDBCrudPolicy:
            TableName: !Ref LocationsTable
      Events:
        ApiEvents:
          Type: Api
          Properties:
            Path: /events
            Method: POST
```

Usa la policy `DynamoDBCrudPolicy` que nos permite hacer operaciones CRUD en la tabla indicada. Esta API esta subscrita a un evento del API Gateway que esta expuesto como un _post_ al recurso _events_. La segunda lambda se configura de forma similar:

```yaml
  WeatherQueryLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: target/lambda.zip
      Handler: book.api.WeatherQueryLambda::handler
      Policies:
        - DynamoDBReadPolicy:
            TableName: !Ref LocationsTable
      Events:
        ApiEvents:
          Type: Api
          Properties:
            Path: /locations
            Method: GET
```

Observese como la policy solo permite consultar la tabla.


Para probar desplegamos el paquete:

```ps
mvn package

$Env:CF_BUCKET="egsmartin"

sam deploy --s3-bucket $Env:CF_BUCKET --stack-name miPipeline --capabilities CAPABILITY_IAM
```

Para probar podemos usar un payload como este:

```json
{
	"locationName":"Oxford, UK", 
	"temperature":64,
	"timestamp":1564428898,
	"latitude": 51.75,
	"longitude": -1.25
}
```
