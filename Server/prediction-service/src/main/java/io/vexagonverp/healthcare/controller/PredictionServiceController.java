package io.vexagonverp.healthcare.controller;

import io.vexagonverp.healthcare.model.HeartPredictionPayload;
import io.vexagonverp.healthcare.service.PredictionService;
import io.vexagonverp.healthcare.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class PredictionServiceController {


    private final PredictionService predictionService;
    private final TrainingService trainingService;

    @Autowired
    public PredictionServiceController(PredictionService predictionService, TrainingService trainingService) {
        this.predictionService = predictionService;
        this.trainingService = trainingService;
    }

    @PostMapping("/predict-import")
    public ResponseEntity<?> getPredictionImport(@RequestBody HeartPredictionPayload heartPredictionPayload) {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders)
                .body(predictionService.predictionFromImport(heartPredictionPayload));
    }

    @PostMapping("/predict-train")
    public ResponseEntity<?> getPublic(@RequestBody HeartPredictionPayload heartPredictionPayload) {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders)
                .body(predictionService.predictionFromTraining(heartPredictionPayload));
    }

}