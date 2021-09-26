Necesitamos usar dos CLI, la CLI de AWS y la de SAM

Vamos a crear la Lambda en Java, asi que necesitaremos un jdk de Java y maven.

```ps

java -version

mvn -v

aws --version

sam --version
```

Configuramos AWS en la computadora usando:

```ps
aws configure
```

Creamos una plantilla de proyecto usando `sam`:

```ps
sam init --location gh:symphoniacloud/sam-init-HelloWorldLambdaJava
```

`gh:symphoniacloud/sam-init-HelloWorldLambdaJava` hace referencia a una plantilla que se usara para crear el proyecto. `gh` hace referencia a _github_. He clonado en este directorio la plantilla.

En la plantilla que se crea destacar lo siguiente:

- El proyecto java, con el handler
- El pom con la configuración maven
- el archivo de configuración que usa `SAM` para crear la infraestructura: `template.yaml`

Para crear el paquete a desplegar hacemos:

```ps
mvn package

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.663 s
[INFO] Finished at: 2021-09-26T11:32:58+02:00
[INFO] ------------------------------------------------------------------------
```

Para hacer el despligue necesitaremos un bucket de s3. Creamos uno que se llama `egsmartin`. Para hacer el despliegue con SAM:

```ps
$Env:CF_BUCKET="egsmartin"

sam deploy --s3-bucket $Env:CF_BUCKET --stack-name HelloWorldLambdaJava --capabilities CAPABILITY_IAM
```

Crea todos los recursos definidos en _template.yaml_. Podemos ver el stack que se ha creado en cloudformation:

```ps
aws cloudformation delete-stack --stack-name HelloWorldLambdaJava
```

Podemos llamar de forma sincrona a la lambda:

```ps
aws lambda invoke --invocation-type RequestResponse --function-name HelloWorldLambdaJava-HelloWorldLambda-juIBr5YCm06W --cli-binary-format raw-in-base64-out --payload '{\"name\":\"Eugenio\"}' outputfile.txt
```

O llamarla de forma asincrona, indicando que el tipo de invocación es `Event`:

```ps
aws lambda invoke --invocation-type Event --function-name HelloWorldLambdaJava-HelloWorldLambda-juIBr5YCm06W --cli-binary-format raw-in-base64-out --payload '{\"name\":\"Eugenio\"}' outputfile.txt
```
