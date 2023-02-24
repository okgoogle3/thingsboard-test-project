package com.example.MyProject.controller.DTO.Request;

import com.example.MyProject.controller.DTO.AggregationFunction;
import lombok.Data;

import java.util.Locale;

@Data
public class TelemetryAggregationDTO {
    private String name;
    private String type;
    private Long startTs;
    private Long endTs;
    private Long aggregationPeriod;
    private AggregationFunction aggregationFunction;

    public void setAggregationFunction(String aggregationFunction) {
        this.aggregationFunction = AggregationFunction.valueOf(aggregationFunction.toUpperCase(Locale.ENGLISH));
    }
}
