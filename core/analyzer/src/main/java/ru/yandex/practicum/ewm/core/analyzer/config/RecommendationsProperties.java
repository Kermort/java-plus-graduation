package ru.yandex.practicum.ewm.core.analyzer.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.api.analyzer.enums.UserActionType;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "analyzer.recommendations")
@Getter
@Setter
@NoArgsConstructor
public class RecommendationsProperties {
    private Map<String, Double> actionWeights;

    public Double getActionWeight(UserActionType actionType) {
        return this.actionWeights.get(actionType.name());
    }
}
