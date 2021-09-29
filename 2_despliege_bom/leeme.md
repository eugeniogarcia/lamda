# BOM

En un solo repositorio vamos a generar varios artefactos o módulos. Para ello definimos un pom en la raiz que sera un BOM, _Bill of Materials_. El pom no genera ningun artefacto, es de tipo _pom_:

```xml
  <groupId>my.groupId</groupId>
  <artifactId>chapter5-Data-Pipeline</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>
```

Indicamos los modulos que se van a generar bajo su paraguas:

```xml
  <modules>
    <module>common-code</module>
    <module>bulk-events-stage</module>
    <module>single-event-stage</module>
  </modules>
```

Podemos centralizar en la definición del pom las librerias que seran usadas en los módulos, de modo que garantizamos la coherencia de las versiones que se usen entre ellos.

Cada modulo tendra su propio pom, haciendo referencia al BOM:

```xml
    <parent>
        <groupId>my.groupId</groupId>
        <artifactId>chapter5-Data-Pipeline</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
```

En las dependencias podemos incluir dependencias de las gestionadas por el _dependencymanagement_ del padre, así como a otros modulos incluidos en el BOM. Maven compilara los artefactos en el orden adecuado:

```xml
    <dependencies>
        <dependency>
            <groupId>my.groupId</groupId>
            <artifactId>common-code</artifactId>
            <version>${project.parent.version}</version>
        </dependency>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-events</artifactId>
        </dependency>
```

# Pipeline

A diferencia de lo que hemos hecho en el ejemplo anterior, generaremos diferentes jars para cada uno de los proyectos. En el _template.yaml_ podemos observar como cada lambda hace referencia a un artefacto diferente:

```yaml
  BulkEventsLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: bulk-events-stage/target/lambda.zip
```

Tenemos nuestra sección _Globals_ y los _Resources_. En Globals hemos definido el timeout a usar en todas las lambdas:


```yaml
Globals:
  Function:
    Runtime: java8
    MemorySize: 512
    Timeout: 10
```

Entre los _Resources_ tenemos:

- Bucket de S3. Podemos ver como creamos el nombre del bucket de forma que sea único, y para ello usamos atributos de nuestro contexto. con `!Sub` lo que se indica es que se __Sub__stituyan los valores. El nombre lógico del bucket será _PipelineStartBucket_:

```yaml
Resources:
  PipelineStartBucket:
    Type: AWS::S3::Bucket
    Properties:
      BucketName: !Sub ${AWS::StackName}-${AWS::AccountId}-${AWS::Region}-start
````

- Topico SNS, `AWS::SNS::Topic`, con nombre lógico _FanOutTopic_:

```yamll
  FanOutTopic:
    Type: AWS::SNS::Topicc

```

- Declaramos dos lambdas, `AWS::Serverless::Function` con nombres lógicos _BulkEventsLambda_ y _SingleEventLambda_

```yaml
  BulkEventsLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: bulk-events-stage/target/lambda.zip
      Handler: book.pipeline.bulk.BulkEventsLambda::handler
      Environment:
        Variables:
          FAN_OUT_TOPIC: !Ref FanOutTopic
      Policies:
      - S3ReadPolicy:
          BucketName: !Sub ${AWS::StackName}-${AWS::AccountId}-${AWS::Region}-start
      - SNSPublishMessagePolicy:
          TopicName: !GetAtt FanOutTopic.TopicName
      Events:
        S3Event:
          Type: S3
          Properties:
            Bucket: !Ref PipelineStartBucket
            Events: s3:ObjectCreated:*

  SingleEventLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: single-event-stage/target/lambda.zip
      Handler: book.pipeline.single.SingleEventLambda::handler
      Events:
        SnsEvent:
          Type: SNS
          Properties:
            Topic: !Ref FanOutTopic
```

Podemos destacar de la definición de las lambdas:
- Para acceder al S3, y publicar en SNS, necesitamos unos perfiles concretos que se incluyen en la sección _Policies_
- En la definición de los eventos de entrada a los lambdas, nos referimos con `!Ref` al nombre lógico de un recurso definido en este mismo _template.yaml_
- Podemos acceder a los atributos de los recursos usando `!GetAtt`. Aquí podemos ver como usamos `!GetAtt FanOutTopic.TopicName` para definir una de las Policies que se asocian al lambda
- Definimos una variable de entorno

# Prueba

```ps
mvn package

$Env:CF_BUCKET="egsmartin"

sam deploy --s3-bucket $Env:CF_BUCKET --stack-name mipipeline --capabilities CAPABILITY_IAM
```

Para probar podemos subir el bucket de S3 el json incluido de ejemplo, _sampledata.json_