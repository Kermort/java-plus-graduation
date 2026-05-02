package ru.yandex.practicum.ewm.core.collector.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.grpc.UserActionControllerGrpc;
import ru.practicum.ewm.stats.grpc.UserActionProto;
import com.google.protobuf.Empty;
import ru.yandex.practicum.ewm.core.collector.kafka.KafkaClient;
import ru.yandex.practicum.ewm.core.collector.mapper.UserActionMapper;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcUserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final KafkaClient kafkaClient;

    @Override
    public void collectUserAction(UserActionProto action, StreamObserver<Empty> responseObserver) {
        log.info("[Grpc user action controller] received user action {} ", action);

        kafkaClient.sendUserAction(UserActionMapper.toAvro(action));

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();

    }
}
