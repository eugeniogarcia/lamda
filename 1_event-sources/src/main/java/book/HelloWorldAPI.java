package book;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class HelloWorldAPI {
	//En lugar de usar un POJO, usamos la definición de los eventos del API Gateway proporcionada por AWS
	//Para ello incluimos en el pom la dependencia aws-lambda-java-events
    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent event, Context c) {
        System.out.println(event);
        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("HelloAPIWorld");
    }
}
