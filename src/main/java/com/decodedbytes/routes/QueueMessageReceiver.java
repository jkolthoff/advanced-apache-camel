package com.decodedbytes.routes;

import com.decodedbytes.policies.DependentRoutePolicy;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.RoutePolicy;
import org.springframework.stereotype.Component;

@Component
public class QueueMessageReceiver extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        RoutePolicy dependentRoutePolicy = new DependentRoutePolicy("batchMessageRouteId", "activeMQSubscriberId");


        from("activemq:queue:nameaddressqueue")
                .routeId("activeMQSubscriberId")
                .routePolicy(dependentRoutePolicy)
                .log(LoggingLevel.INFO, ">>>>>>>>>>> Received Queue Message: ${body}");
    }
}
