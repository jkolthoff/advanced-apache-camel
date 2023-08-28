package com.decodedbytes.routes;

import com.decodedbytes.beans.InboundNameAddress;
import com.decodedbytes.policies.DependentRoutePolicy;
import com.decodedbytes.processor.NameAddressProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.RoutePolicy;
import org.springframework.stereotype.Component;

@Component
public class BatchMessageProcessingRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        RoutePolicy dependentRoutePolicy = new DependentRoutePolicy("batchMessageRouteId", "activeMQSubscriberId");

        from("timer:batch?period=60000")
                .routeId("batchMessageRouteId")
                .routePolicy(dependentRoutePolicy)
                .to("jpa:"+ InboundNameAddress.class.getName()+"?namedQuery=fetchAllRows")
                .split(body())
                .log(LoggingLevel.INFO, "Read Row: ${body}")
                .process(new NameAddressProcessor())
                .convertBodyTo(String.class)
                .to("file:src/data/output?fileName=outputFile.csv&fileExist=append&appendChars=\\n")
                .toD("jpa:"+InboundNameAddress.class.getName()+"?nativeQuery=DELETE FROM NAME_ADDRESS where id = ${header.consumedId}&useExecuteUpdate=true")
                .end();

        from("timer:startBatchRoute?repeatCount=1&delay=5000")
                .routeId("batchStopRouteId")
                .to("controlbus:route?routeid=batchMessageRouteId&action=stop")
                .to("controlbus:route?routeid=batchMessageRouteId&action=status")
                .to("controlbus:route?routeid=activeMQSubscriberId&action=status");

        from("timer:startBatchRoute?repeatCount=1&delay=15000")
                .routeId("batchStartRouteId")
                .to("controlbus:route?routeid=activeMQSubscriberId&action=stop")
                .to("controlbus:route?routeid=batchMessageRouteId&action=status")
                .to("controlbus:route?routeid=activeMQSubscriberId&action=status");

    }
}
