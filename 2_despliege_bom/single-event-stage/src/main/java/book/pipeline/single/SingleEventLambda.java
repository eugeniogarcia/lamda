package book.pipeline.single;

import book.pipeline.common.WeatherEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class SingleEventLambda {
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //Para cada registro recibido desde SNS, deserializamos el json, y publicamos el contenido en CloudWatch
    public void handler(SNSEvent event) {
        event.getRecords().forEach(this::processSNSRecord);
    }

    private void processSNSRecord(SNSEvent.SNSRecord snsRecord) {
        try {
        	//Deserializa el json recibido por el topico SNS en un evento
        	final WeatherEvent weatherEvent = objectMapper.readValue(
                    snsRecord.getSNS().getMessage(),
                    WeatherEvent.class);
        	//Escribe el evento en el log. Lo podremos ver en CloudWatch
            System.out.println("Received weather event:");
            System.out.println(weatherEvent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
