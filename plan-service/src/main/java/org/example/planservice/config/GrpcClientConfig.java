package org.example.planservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.planservice.grpc.group.GroupQueryServiceGrpc;
import org.example.planservice.grpc.routing.RouteCalculatorServiceGrpc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel groupManagedChannel(
            @Value("${group.grpc.host:localhost}") String host,
            @Value("${group.grpc.port:9092}") int port
    ) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    GroupQueryServiceGrpc.GroupQueryServiceBlockingStub groupQueryServiceBlockingStub(
            @Qualifier("groupManagedChannel") ManagedChannel channel
    ) {
        return GroupQueryServiceGrpc.newBlockingStub(channel);
    }


    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel routingManagedChannel(
            @Value("${routing.grpc.host:localhost}") String host,
            @Value("${routing.grpc.port:9093}") int port
    ) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    RouteCalculatorServiceGrpc.RouteCalculatorServiceBlockingStub routingServiceBlockingStub(
            @Qualifier("routingManagedChannel") ManagedChannel channel
    ) {
        return RouteCalculatorServiceGrpc.newBlockingStub(channel);
    }

}
