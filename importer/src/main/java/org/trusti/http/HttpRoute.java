package org.trusti.http;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class HttpRoute extends EndpointRouteBuilder {

    private static final Pattern HREF_PATTERN = Pattern.compile("href=\"(.*?)\"");

    @Override
    public void configure() throws Exception {
        from("direct:start-importer-http")
                .to("direct:discover-url-directory");

        from("direct:discover-url-directory")
                .onException(IOException.class)
                    .useOriginalMessage()
                    .maximumRedeliveries(2)
                    .redeliveryDelay(200)
                    .logExhausted(false)
                    .log(LoggingLevel.ERROR, "Could not reach the given URL  ${body}")
                .end()

                .setHeader("base_url", body())
                .process(exchange -> {
                    String url = exchange.getIn().getBody(String.class);

                    Scanner out = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8).useDelimiter("\n");
                    List<String> hrefs = new ArrayList<>(8);

                    while (out.hasNext()) {
                        Matcher match = HREF_PATTERN.matcher(out.next());
                        if (match.find()) {
                            hrefs.add(match.group(1));
                        }
                    }

                    out.close();

                    exchange.getIn().setBody(hrefs);
                })
                .split(body()).parallelProcessing()
                    .filter(body().not().contains("../"))
                    .process(exchange -> {
                        String url = exchange.getIn().getHeader("base_url", String.class);
                        String next = exchange.getIn().getBody(String.class);
                        exchange.getIn().setBody(url + next);
                    })
                    .choice()
                        .when(body().endsWith("/"))
                            .to("direct:discover-url-directory")
                        .endChoice()
                        .when(body().endsWith(".json"))
                            .to("direct:download-url")
                        .endChoice()
                        .otherwise()
                            .log(LoggingLevel.TRACE, "Ignoring ${body}")
                        .endChoice()
                    .end()
                .end();

        from("direct:download-url")
                .setHeader("file_url", body())
                .log(LoggingLevel.INFO, "${body}")

                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .toD("${body}")

                .to("direct:send-file")

                .onException(HttpOperationFailedException.class)
                    .maximumRedeliveries(2)
                    .redeliveryDelay(0)
                    .handled(true)
                    .logExhausted(false)
                    .logExhaustedMessageBody(false)
                    .log(LoggingLevel.ERROR, "${header.file_url} could not be downloaded")
                .end()

                .onException(SocketException.class)
                    .maximumRedeliveries(2)
                    .redeliveryDelay(0)
                    .handled(true)
                    .logExhausted(false)
                    .logExhaustedMessageBody(false)
                    .log(LoggingLevel.ERROR, "${header.file_url} could not be sent")
                .end();
    }

}
