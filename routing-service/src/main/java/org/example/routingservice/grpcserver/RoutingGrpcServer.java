package org.example.routingservice.grpcserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RoutingGrpcServer {
    private final int port;
    private final RouteCalculatorGrpcService routeCalculatorGrpcService;
    private Server server;

    public RoutingGrpcServer(
            @Value("${grpc.server.port:9093}") int port,
            RouteCalculatorGrpcService routeCalculatorGrpcService
    ) {
        this.port = port;
        this.routeCalculatorGrpcService = routeCalculatorGrpcService;
    }

    @PostConstruct
    void start() {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(routeCalculatorGrpcService)
                    .build()
                    .start();
            log.info("Routing gRPC server started on port {}", server.getPort());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start routing gRPC server", ex);
        }
    }

    @PreDestroy
    void stop() throws InterruptedException {
        if (server == null) {
            return;
        }
        server.shutdown();
        if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
            server.shutdownNow();
        }
    }
}
