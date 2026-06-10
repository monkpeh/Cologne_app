package com.example.colognerecommendation.listener;

import com.example.colognerecommendation.config.RabbitConfig;
import com.example.colognerecommendation.dto.FragranceDto;
import com.example.colognerecommendation.dto.RecommendationRequest;
import com.example.colognerecommendation.dto.RecommendationResponse;
import com.example.colognerecommendation.dto.ScoredFragranceDto;
import com.example.colognerecommendation.engine.RecommendationEngine;
import com.example.colognerecommendation.engine.RecommendationResult;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecommendationListener {

    private final RecommendationEngine engine = new RecommendationEngine();

    @RabbitListener(queues = RabbitConfig.RECOMMENDATION_QUEUE)
    public RecommendationResponse handle(RecommendationRequest request) {
        Weather weather   = Weather.valueOf(request.getWeather());
        Occasion occasion = Occasion.valueOf(request.getOccasion());

        List<FragranceDto> collection = request.getCollection();
        List<RecommendationResult> results = engine.recommend(collection, weather, occasion);

        List<ScoredFragranceDto> scored = results.stream()
                .map(r -> new ScoredFragranceDto(r.getFragrance(), r.getScore(), r.getReasons()))
                .collect(Collectors.toList());

        return new RecommendationResponse(request.getCorrelationId(), scored);
    }
}
