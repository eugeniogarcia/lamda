package book.pipeline.bulk;

import book.pipeline.common.WeatherEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BulkEventsLambda {
	//Para deserializar el json en un objeto
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);
    //Cliente SNS
    private final AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
    //Cliente S3
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    //El topic de SNS lo recuperamos de una variable de entorno
    private final String snsTopic = System.getenv("FAN_OUT_TOPIC");

    //Cada registro incluido en el evento S3 es un bucket & key
    //El contenido del bucket & key es una lista de eventos
    //Cada uno de los eventos se convierte en json y se publica en un topico sns
    public void handler(S3Event event) {
        event.getRecords().forEach(this::processS3EventRecord);
    }

    //Procesado de un evento individual
    private void processS3EventRecord(S3EventNotification.S3EventNotificationRecord record) {
    	//Obtiene una lista con los eventos guardados en el bucket & key
    	final List<WeatherEvent> weatherEvents = readWeatherEventsFromS3(
                record.getS3().getBucket().getName(),
                record.getS3().getObject().getKey());

    	//Procesa cada evento de la lista. Convierte el evento en un json y lo publica en SNS
        weatherEvents.stream()
                .map(this::weatherEventToSnsMessage)
                .forEach(message -> sns.publish(snsTopic, message));

        System.out.println("Published " + weatherEvents.size() + " weather events to SNS");
    }

    //Recupera el contenido de un bucket & key
    private List<WeatherEvent> readWeatherEventsFromS3(String bucket, String key) {
        try {
        	//Stream al objeto
            final S3ObjectInputStream s3is = s3.getObject(bucket, key).getObjectContent();
            //Lee el contenido del stream y lo deserializa en un array de Eventos
            final WeatherEvent[] weatherEvents =
                    objectMapper.readValue(s3is, WeatherEvent[].class);
            s3is.close();
            //Devuelve una lista con los eventos
            return Arrays.asList(weatherEvents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Crea un json con los datos del evento
    private String weatherEventToSnsMessage(WeatherEvent weatherEvent) {
        try {
            return objectMapper.writeValueAsString(weatherEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
