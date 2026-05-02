package ru.yandex.practicum.ewm.core.analyzer.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.grpc.*;
import ru.yandex.practicum.ewm.core.analyzer.mapper.RecommendationsMapper;
import ru.yandex.practicum.ewm.core.analyzer.model.Recommendation;
import ru.yandex.practicum.ewm.core.analyzer.service.RecommendationsService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcRecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsService recommendationsService;

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        log.info("[Grpc recommendations controller] get similar events (request = {})", request);

        try {
            List<Recommendation> recommendations = recommendationsService.getSimilarEvents(
                    request.getEventId(),
                    request.getUserId(),
                    request.getMaxResults()
            );

            for (Recommendation recommendation : recommendations) {
                RecommendedEventProto protoMessage = RecommendationsMapper.toProto(recommendation);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("[Grpc recommendations controller] Error processing GetSimilarEvents request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        log.info("[Grpc recommendations controller] get interactions count (request = {})", request);

        try {
            List<Recommendation> recommendations = recommendationsService.getInteractionsCount(
                    request.getEventIdsList()
            );

            for (Recommendation recommendation : recommendations) {
                RecommendedEventProto protoMessage = RecommendationsMapper.toProto(recommendation);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("[Grpc recommendations controller] Error processing GetInteractionsCount request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        log.info("[Grpc recommendations controller] get recommendations for user (request = {})", request);

        log.info("gRPC request received for GetRecommendationsForUser: {}", request);
        try {
            List<Recommendation> predictions = recommendationsService.getRecommendationsForUser(
                    request.getUserId(),
                    request.getMaxResults()
            );

            for (Recommendation prediction : predictions) {
                RecommendedEventProto protoMessage = RecommendationsMapper.toProto(prediction);
                responseObserver.onNext(protoMessage);
            }

            responseObserver.onCompleted();
            log.info("Successfully streamed {} user predictions.", predictions.size());

        } catch (Exception e) {
            log.error("Error processing GetRecommendationsForUser request", e);

            Status status = Status.INTERNAL.withDescription("An error occurred: " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }
}
