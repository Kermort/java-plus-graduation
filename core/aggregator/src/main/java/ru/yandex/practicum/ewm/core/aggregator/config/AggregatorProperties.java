package ru.yandex.practicum.ewm.core.aggregator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.ActionTypeAvro;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "aggregator.recommendations")
@Getter
@Setter
@NoArgsConstructor
public class AggregatorProperties {
    private Map<String, Double> actionWeights;

    public Double getActionWeightByAvroType(ActionTypeAvro actionType) {
        return this.actionWeights.get(actionType.name());
    }
}
