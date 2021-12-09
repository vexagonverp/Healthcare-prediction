package io.vexagonverp.healthcare.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class HeartPredictionResponse {
    private double negative;
    private double positive;

    public HeartPredictionResponse(double negative, double positive) {
        this.negative = negative * 100;
        this.positive = positive * 100;
    }
}
