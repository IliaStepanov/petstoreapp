package com.chtrembl.petstoreapp.client;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.Order;
import com.chtrembl.petstoreapp.service.PetStoreServiceImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class ReserveOrderClient {

    private static final Logger logger = LoggerFactory.getLogger(PetStoreServiceImpl.class);

    private WebClient orderItemsReserverClient;
    private final ContainerEnvironment containerEnvironment;


    public ReserveOrderClient(ContainerEnvironment containerEnvironment) {
        this.containerEnvironment = containerEnvironment;
    }

    @PostConstruct
    public void initialize() {
        this.orderItemsReserverClient = WebClient.builder()
                .baseUrl(this.containerEnvironment.getOrderItemsReserverServiceURL())
                .build();
    }

    public void reserveOrder(String sessionId, Order order) {

        logger.info("Reserving order by sessionId {}", sessionId);
        try {
            String orderJSON = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false).writeValueAsString(order);

            String response = orderItemsReserverClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("api/reserveOrder")
                            .queryParam("sessionId", sessionId)
                            .build())
                    .body(BodyInserters.fromPublisher(Mono.just(orderJSON), String.class))
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Cache-Control", "no-cache")
                    .retrieve()
                    .bodyToMono(String.class).block();

            logger.info(response);

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }
}
