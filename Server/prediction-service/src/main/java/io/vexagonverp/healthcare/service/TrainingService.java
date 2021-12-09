package io.vexagonverp.healthcare.service;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.InputStreamInputSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.autodiff.listeners.records.EvaluationRecord;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.el.EvaluationListener;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TrainingService {
    private static Logger log = LoggerFactory.getLogger(TrainingService.class);
    private static MultiLayerNetwork model;
    private static DataNormalization normalizer;

    @Autowired
    public TrainingService() {

    }

    public DataNormalization getNormalizer(){
        return normalizer;
    }

    public MultiLayerNetwork trainDataSet() {
        if (model != null) {
            return model;
        }
        //First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
        boolean skipFirstLine = true;
        Character delimiter = ',';
        RecordReader recordReader = new CSVRecordReader(delimiter);
        try {
            InputStream is = TrainingService.class.getClassLoader().getResourceAsStream("heart.csv");
            recordReader.initialize(new InputStreamInputSplit(is));
            if (skipFirstLine) {
                recordReader.nextRecord();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
        int labelIndex = 13;     //14 values in each row of the heart.csv CSV: 13 input features followed by an integer label (class) index. Labels are the 14th value (index 13) in each row
        int numClasses = 2;     //2 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
        int batchSize = 303;    //Test data set: 303 examples total. We are loading all of them into one DataSet (not recommended for large data sets)

        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
        DataSet allData = iterator.next();
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.75);  //Use 75% of data for training

        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        normalizer = new NormalizerStandardize();
        normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData);     //Apply normalization to the training data
        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set


        final int numInputs = labelIndex;//Only if labelIndex last column
        int outputNum = numClasses;
        int iterations = 2000; //Epoch
        long seed = 42;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
                .gradientNormalizationThreshold(0.5)
                .list()
                .layer(0, new DenseLayer.Builder().units(numInputs)
                        .nIn(numInputs)
                        .l2(0.001)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DropoutLayer(0.75))
                .layer(2, new DenseLayer.Builder().units(numInputs / 2)
                        .l2(0.001)
                        .activation(Activation.TANH)
                        .build())
                .layer(3, new DropoutLayer(0.75))
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).units(outputNum)
                        .nOut(outputNum).build())
                .build();

        //run the model
        model = new MultiLayerNetwork(conf);
        model.init();
        //model.setLearningRate(0.005);
        model.setListeners(new ScoreIterationListener(iterations/10));

        for (int i = 0; i < iterations; i++) {
            model.fit(trainingData);
        }

        Evaluation evaluation = new Evaluation(2);
        INDArray output = model.output(testData.getFeatures());
        evaluation.eval(testData.getLabels(), output);

        log.info("Accuracy :\t"+evaluation.accuracy());
        log.info("Precision :\t"+evaluation.precision());
        log.info("Recall :\t"+evaluation.recall());
        log.info("F1 Score :\t"+evaluation.f1());

        return model;
    }

}
