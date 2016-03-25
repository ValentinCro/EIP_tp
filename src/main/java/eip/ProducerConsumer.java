package eip;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.BasicConfigurator;

import java.util.Scanner;

public class ProducerConsumer {

    public static void main (String [] args) throws Exception {
        final String url = "http://127.0.0.1:8084";
        //Configure le logger par défaut
        BasicConfigurator.configure();

        //Contexte Camel par défaut
        CamelContext context = new DefaultCamelContext();

        //Crée une route contenant le consommateur
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                //On définit un consommateur 'consumer-1'
                //qui va écrire le message
                from("direct:consumer-1").to("log:afficher-1-log");
                from("direct:consumer-2").to("file:messages");
                from("direct:consumer-all")
                        .choice()
                            .when(header("destinataire").isEqualTo("écrire"))
                                .to("direct:consumer-2")
                            .otherwise()
                                .to("direct:consumer-1");
                from("direct:CityManager")
                        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                        .toD(url + "/${header.method}")
                        .log("reponse received OMG : ${body}");
            }
        };

        //on ajoute la route au contexte
        routeBuilder.addRoutesToCamelContext(context);

        //On démarre le contexte pour activer les routes
        context.start();

        //On créé un producteur
        ProducerTemplate pt = context.createProducerTemplate();

        String userEntry = "";

        do {
            Scanner sc = new Scanner(System.in);
            userEntry = sc.nextLine();
            if (!userEntry.equals("")) {
                //qui envoie un message au consommateur 'consumer-1'
                pt.sendBodyAndHeader("direct:CityManager", "", "method", userEntry);
            }
        } while (!userEntry.equals("exit"));
    }
}
