package org.example.groupservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.groupservice.grpc.auth.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel authManagedChannel(
            @Value("${auth.grpc.host:localhost}") String host,
            @Value("${auth.grpc.port:9091}") int port
    ) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(ManagedChannel authManagedChannel) {
        return UserServiceGrpc.newBlockingStub(authManagedChannel);
    }
}
