Para facilitar la prueba unitaria refactorizamos el código de modo que se eviten side-effects en los métodos, y que haya constructores que tomen como argumento los objetos que de otra forma se toman del contexto. Empezando por esta última parte, tenemos un constructor como el siguiente:

```java
    public BulkEventsLambda() {
        this(AmazonSNSClientBuilder.defaultClient(), AmazonS3ClientBuilder.defaultClient());
    }
```

Vamos a crear un constructor que usaremos en los tests, y que nos permita pasar los clientes SNS y S3. Esto nos permitirá crear un mock de los clientes durante el test

```java
    public BulkEventsLambda(AmazonSNS sns, AmazonS3 s3) {
        this.sns = sns;
        this.s3 = s3;
        this.snsTopic = System.getenv(FAN_OUT_TOPIC_ENV);
        if (this.snsTopic == null) {
            throw new RuntimeException(String.format("%s must be set", FAN_OUT_TOPIC_ENV));
        }
    }
```

Procuraremos que los métodos no tengan side-effects, es decir, que no modifiquen el estado de la aplicación, ni que incluyan integraciones con terceros.

# JUnit

## Reglas

Las reglas nos permiten simplificar la definición de los casos de prueba. Definir una regla consta de dos partes:

- __Necesitamos implementar un interface__, `org.junit.rules.TestRule`. Este interface incluye un solo método con firma `public Statement apply(Statement base, Description description)`. `Statement` representa el caso de prueba. Veamos un ejemplo de una regla:


```java
public class TestMethodNameLogger implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(TestMethodNameLogger.class);

    @Override
    public Statement apply(Statement base, Description description) {
        logInfo("Before test", description);
        try {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                }
            };
        } finally {
            logInfo("After test", description);
        }
    }

    private void logInfo(String msg, Description description) {
        LOG.info(msg + description.getMethodName());
    }
}
```

Lo que hara el framwork es llamar al método `apply`. En el método apply la llamada `base.evaluate();` es la ejecución del caso de prueba - el método anotado con `@Test`. Lo que podemos hacer por lo tanto es incluir lógica antes y después de la ejecución del caso de prueba. Esto es, conseguimos algo que podríamos haber conseguid con `@before`, `@after`, `@beforeclass`y `@afterclass`, __pero de una manera más encapsulada, y sobre todo, facíl de reutilizar__.

Podemos crear una regla custom como hemos visto en el ejemplo anterior, o utilizar una de las ya creadas por defecto. [En este artículo](https://www.baeldung.com/junit-4-rules) se explica su utilización. En nuestro código usamos una regla para fijar variables de entorno, capturar logs, y otra para capturar y verificar excepciones:

```yaml
    @Rule
    public EnvironmentVariables environment = new EnvironmentVariables();

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    
```

- La segunda parte es anotar bien un campo o un método con la anotación `@Rule`. El campo debe ser de un tipo `org.junit.rules.TestRule` y ser público. El método debe devolver un tipo `org.junit.rules.TestRule`.

## Assertions

En la ejecución de los tests desearemos ir aplicando checks/comprobaciones a lo largo de los mismos. 

```java
    @Test
    public void testReadWeatherEvent() {
        String message = "{\"locationName\":\"Brooklyn, NY\",\"temperature\":91.0,\"timestamp\":1564428897,\"longitude\":-73.99,\"latitude\":40.7}";

        SingleEventLambda lambda = new SingleEventLambda();
        WeatherEvent weatherEvent = lambda.readWeatherEvent(message);

        Assert.assertEquals("Brooklyn, NY", weatherEvent.locationName);
        Assert.assertEquals(91.0, weatherEvent.temperature, 0.0);
        Assert.assertEquals(1564428897L, weatherEvent.timestamp, 0);
        Assert.assertEquals(40.7, weatherEvent.latitude, 0.0);
        Assert.assertEquals(-73.99, weatherEvent.longitude, 0.0);
    }
```

Podemos también usar las `@Rules` que hemos definido para validar cosas, aquí por ejemplo comprobamos que se hayan arrojado ciertas excepciones - como parte de un resultado positivo esperamos que se haya generado alguna excepción:


```java
   @Rule
   public ExpectedException thrown = ExpectedException.none();
    
   @Test
    public void testReadWeatherEventBadData() {
        String message = "{\"locationName\":\"Brooklyn, NY\",\"temperature\":91.0,\"timestamp\":\"Wrong data type\",\"longitude\":-73.99,\"latitude\":40.7}";

        // Expect exception
        thrown.expect(RuntimeException.class);
        thrown.expectCause(CoreMatchers.instanceOf(InvalidFormatException.class));
        thrown.expectMessage("Cannot deserialize value of type `java.lang.Long` from String \"Wrong data type\": not a valid Long value");

        // Invoke
        SingleEventLambda lambda = new SingleEventLambda();
        lambda.readWeatherEvent(message);
    }
```

En este otro ejemplo, usamos una regla para comprobar que se haya generado un log:

```java
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests()
    
	@Test
    public void testLogWeatherEvent() {
        WeatherEvent weatherEvent = new WeatherEvent();
        weatherEvent.locationName = "Foo, Bar";
        weatherEvent.latitude = 100.0;
        weatherEvent.longitude = -100.0;
        weatherEvent.temperature = 32.0;
        weatherEvent.timestamp = 0L;

        SingleEventLambda lambda = new SingleEventLambda();
        lambda.logWeatherEvent(weatherEvent);

        Assert.assertEquals(
                "Received weather event:\nWeatherEvent{locationName='Foo, Bar', temperature=32.0, timestamp=0, longitude=-100.0, latitude=100.0}\n"
                , systemOutRule.getLog());
    }
```

## Mockito

