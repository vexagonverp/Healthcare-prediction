package io.vexagonverp.healthcare.service;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PredictionService {
    private static MultiLayerNetwork model;
    private final TrainingService trainingService;
    public PredictionService(TrainingService trainingService){
        this.trainingService = trainingService;
    }


    public double[] predictionFromImport() {
        if (model == null) {
            try {
                InputStream is = PredictionService.class.getClassLoader().getResourceAsStream("heart.h5");
                model = KerasModelImport.importKerasSequentialModelAndWeights(is);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKerasConfigurationException e) {
                e.printStackTrace();
            } catch (UnsupportedKerasConfigurationException e) {
                e.printStackTrace();
            }
        }


        //Create input INDArray for the user measurements
        INDArray actualInput = Nd4j.zeros(1, 13);
        actualInput.putScalar(new int[]{0, 0}, 63);
        actualInput.putScalar(new int[]{0, 1}, 1);
        actualInput.putScalar(new int[]{0, 2}, 3);
        actualInput.putScalar(new int[]{0, 3}, 145);
        actualInput.putScalar(new int[]{0, 4}, 233);
        actualInput.putScalar(new int[]{0, 5}, 1);
        actualInput.putScalar(new int[]{0, 6}, 0);
        actualInput.putScalar(new int[]{0, 7}, 150);
        actualInput.putScalar(new int[]{0, 8}, 0);
        actualInput.putScalar(new int[]{0, 9}, 2.3);
        actualInput.putScalar(new int[]{0, 10}, 0);
        actualInput.putScalar(new int[]{0, 11}, 0);
        actualInput.putScalar(new int[]{0, 12}, 1);
        INDArray prediction = model.output(actualInput);
        return prediction.toDoubleVector();
    }

    public double[] predictionFromTraining() {
        MultiLayerNetwork trainedMode = trainingService.trainDataSet();


        //Create input INDArray for the user measurements
        INDArray actualInput = Nd4j.zeros(1, 13);
        actualInput.putScalar(new int[]{0, 0}, 63);
        actualInput.putScalar(new int[]{0, 1}, 1);
        actualInput.putScalar(new int[]{0, 2}, 3);
        actualInput.putScalar(new int[]{0, 3}, 145);
        actualInput.putScalar(new int[]{0, 4}, 233);
        actualInput.putScalar(new int[]{0, 5}, 1);
        actualInput.putScalar(new int[]{0, 6}, 0);
        actualInput.putScalar(new int[]{0, 7}, 150);
        actualInput.putScalar(new int[]{0, 8}, 0);
        actualInput.putScalar(new int[]{0, 9}, 2.3);
        actualInput.putScalar(new int[]{0, 10}, 0);
        actualInput.putScalar(new int[]{0, 11}, 0);
        actualInput.putScalar(new int[]{0, 12}, 1);
        trainingService.getNormalizer().transform(actualInput);
        INDArray prediction = trainedMode.output(actualInput);
        return prediction.toDoubleVector();
    }
}
