package io.vexagonverp.healthcare.service;

import io.vexagonverp.healthcare.model.HeartPredictionPayload;
import io.vexagonverp.healthcare.model.HeartPredictionResponse;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PredictionService {
    private static MultiLayerNetwork importModel;
    private final TrainingService trainingService;

    public PredictionService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    private MultiLayerNetwork getImportModel() {
        if (importModel != null) {
            return importModel;
        }
        try {
            InputStream is = PredictionService.class.getClassLoader().getResourceAsStream("heart.h5");
            importModel = KerasModelImport.importKerasSequentialModelAndWeights(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKerasConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedKerasConfigurationException e) {
            e.printStackTrace();
        }
        return importModel;
    }


    public HeartPredictionResponse predictionFromImport(HeartPredictionPayload heartPredictionPayload) {
        MultiLayerNetwork model = getImportModel();

        //Create input INDArray for the user measurements
        INDArray actualInput = Nd4j.zeros(1, 13);
        actualInput.putScalar(new int[]{0, 0}, heartPredictionPayload.getAge());
        actualInput.putScalar(new int[]{0, 1}, heartPredictionPayload.getSex());
        actualInput.putScalar(new int[]{0, 2}, heartPredictionPayload.getCp());
        actualInput.putScalar(new int[]{0, 3}, heartPredictionPayload.getTrestbps());
        actualInput.putScalar(new int[]{0, 4}, heartPredictionPayload.getChol());
        actualInput.putScalar(new int[]{0, 5}, heartPredictionPayload.getFbs());
        actualInput.putScalar(new int[]{0, 6}, heartPredictionPayload.getRestecg());
        actualInput.putScalar(new int[]{0, 7}, heartPredictionPayload.getThalach());
        actualInput.putScalar(new int[]{0, 8}, heartPredictionPayload.getExang());
        actualInput.putScalar(new int[]{0, 9}, heartPredictionPayload.getOldpeak());
        actualInput.putScalar(new int[]{0, 10}, heartPredictionPayload.getSlope());
        actualInput.putScalar(new int[]{0, 11}, heartPredictionPayload.getCa());
        actualInput.putScalar(new int[]{0, 12}, heartPredictionPayload.getThal());
        INDArray prediction = model.output(actualInput);
        double[] result = prediction.toDoubleVector();
        return new HeartPredictionResponse(result[0], result[1]);
    }

    public HeartPredictionResponse predictionFromTraining(HeartPredictionPayload heartPredictionPayload) {
        MultiLayerNetwork trainedMode = trainingService.trainDataSet();

        //Create input INDArray for the user measurements
        INDArray actualInput = Nd4j.zeros(1, 13);
        actualInput.putScalar(new int[]{0, 0}, heartPredictionPayload.getAge());
        actualInput.putScalar(new int[]{0, 1}, heartPredictionPayload.getSex());
        actualInput.putScalar(new int[]{0, 2}, heartPredictionPayload.getCp());
        actualInput.putScalar(new int[]{0, 3}, heartPredictionPayload.getTrestbps());
        actualInput.putScalar(new int[]{0, 4}, heartPredictionPayload.getChol());
        actualInput.putScalar(new int[]{0, 5}, heartPredictionPayload.getFbs());
        actualInput.putScalar(new int[]{0, 6}, heartPredictionPayload.getRestecg());
        actualInput.putScalar(new int[]{0, 7}, heartPredictionPayload.getThalach());
        actualInput.putScalar(new int[]{0, 8}, heartPredictionPayload.getExang());
        actualInput.putScalar(new int[]{0, 9}, heartPredictionPayload.getOldpeak());
        actualInput.putScalar(new int[]{0, 10}, heartPredictionPayload.getSlope());
        actualInput.putScalar(new int[]{0, 11}, heartPredictionPayload.getCa());
        actualInput.putScalar(new int[]{0, 12}, heartPredictionPayload.getThal());
        trainingService.getNormalizer().transform(actualInput);
        INDArray prediction = trainedMode.output(actualInput);
        double[] result = prediction.toDoubleVector();
        return new HeartPredictionResponse(result[0], result[1]);
    }
}
