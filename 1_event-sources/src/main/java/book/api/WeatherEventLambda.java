package book.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class WeatherEventLambda {
	//Usamos jackson para deserializar el json en el objeto
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //Cliente de Dynamo
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    //Obtenemos la tabla de una variable de entorno
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    public ApiGatewayResponse handler(ApiGatewayRequest request) throws IOException {
    	//Deserializa en un objeto la petición
    	final WeatherEvent weatherEvent = objectMapper.readValue(request.body, WeatherEvent.class);

    	//Obtenemos el nombre de la tabla
        final Table table = dynamoDB.getTable(tableName);
        //Creamos el item que vamos a guardar en Dynamo
        final Item item = new Item()
        		//La primary key
                .withPrimaryKey("locationName", weatherEvent.locationName)
                .withDouble("temperature", weatherEvent.temperature)
                .withLong("timestamp", weatherEvent.timestamp)
                .withDouble("longitude", weatherEvent.longitude)
                .withDouble("latitude", weatherEvent.latitude);
        //Guardamos en DynamoDB
        table.putItem(item);

        return new ApiGatewayResponse(200, weatherEvent.locationName);
    }
}