package book.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//Lambda que devuelve una query con los eventos guardados en la base de datos
public class WeatherQueryLambda {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
    //El nombre de la tabla lo guardamos en una variable de entorno
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    private static final String DEFAULT_LIMIT = "50";

    public ApiGatewayResponse handler(ApiGatewayRequest request) throws IOException {
    	//Obtenemos del query parameter el limite
    	final String limitParam = request.queryStringParameters == null
                ? DEFAULT_LIMIT
                : request.queryStringParameters.getOrDefault("limit", DEFAULT_LIMIT);
        //Convertimos a número
    	final int limit = Integer.parseInt(limitParam);

    	//Usamos la api de dynamo para hacer un table scan en una tabla, limitando el tamaño de la respuesta
        final ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName)
                .withLimit(limit);
        //Ejecuta el scan
        final ScanResult scanResult = dynamoDB.scan(scanRequest);

        //Procesamos la respuesta
        //Con cada item creamos un evento
        //Que convertimos en una lista
        final List<WeatherEvent> events = scanResult.getItems().stream()
                .map(item -> new WeatherEvent(
                        item.get("locationName").getS(),
                        Double.parseDouble(item.get("temperature").getN()),
                        Long.parseLong(item.get("timestamp").getN()),
                        Double.parseDouble(item.get("longitude").getN()),
                        Double.parseDouble(item.get("latitude").getN())
                ))
                .collect(Collectors.toList());

        //Convertimos la lista de eventos en un json, en un json string
        final String json = objectMapper.writeValueAsString(events);

        //Devolvemos el json
        return new ApiGatewayResponse(200, json);
    }
}
