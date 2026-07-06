package org.example.groupservice.grpcserver;

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
public class GroupGrpcServer {
    private final int port;
    private final GroupQueryGrpcService groupQueryGrpcService;
    private Server server;

    public GroupGrpcServer(
            @Value("${grpc.server.port:9092}") int port,
            GroupQueryGrpcService groupQueryGrpcService
    ) {
        this.port = port;
        this.groupQueryGrpcService = groupQueryGrpcService;
    }

    @PostConstruct
    void start() {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(groupQueryGrpcService)
                    .build()
                    .start();
            log.info("Group gRPC server started on port {}", server.getPort());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start group gRPC server", ex);
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
