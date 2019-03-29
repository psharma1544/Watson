package com.jps.predictions.dl4j;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.deeplearning4j.eval.RegressionEvaluation.Metric;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class RNNEquityPricePredictor {

    /**
     * Size of recurring window for training. Keeping 5 for weekly precitions
     */

    private static final String SQL_DUMP_FOLDER = "C:\\Users\\pshar\\Dropbox\\Programming\\DL4J_Dumps";

    private static final Logger log = LoggerFactory.getLogger(RNNEquityPricePredictor.class);
    private SQLConnector sqlConnector = new SQLConnector();

    //'baseDir': Base directory for the data. Change this if you want to save the data somewhere else
    // TODO: Push the data appropriate to training and test directories
    private static File baseDir = new File(SQL_DUMP_FOLDER);
    //private static File baseTrainDir = new File(baseDir, "train");
    //private static File baseTestDir = new File(baseDir, "test");
    /*
    private static File featuresDirTrain = new File(baseTrainDir, "features");
    private static File labelsDirTrain = new File(baseTrainDir, "labels");
    private static File featuresDirTest = new File(baseTestDir, "features");
    private static File labelsDirTest = new File(baseTestDir, "labels");
    */
    public void RNNEquityPricePredictor() throws Exception {
        downloadUCIData();
        int retCount = 0;
        File dir = new File(SQL_DUMP_FOLDER);
        if (dir.exists() && dir.isDirectory()) {
            retCount = dir.listFiles().length;
        }

        int miniBatchSize = 1;
        int numLabelClasses = 1; // Number of Y to be predicted or classified. Set to 1 for regression.
        int labelIndex = 88; // PXS: Understanding is that everything before labelIndex column is the X value and everything afterwards is Y
        //boolean regression = true;

        SequenceRecordReader reader = new CSVSequenceRecordReader(0, ",");
        reader.initialize(new NumberedFileInputSplit(SQL_DUMP_FOLDER + File.separator + "SQLOut_%d.csv", 0, retCount - 2));
        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(reader, miniBatchSize, -1, labelIndex, true);

        //Normalize the training data
        DataNormalization normalizer = new NormalizerStandardize();

        normalizer.fit(trainData);
        trainData.reset();

        //Use previously collected statistics to normalize on-the-fly. Each DataSet returned by 'trainData' iterator will be normalized
        trainData.setPreProcessor(normalizer);

        // ----- Load the test data -----
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader();
        testFeatures.initialize(new NumberedFileInputSplit(SQL_DUMP_FOLDER + File.separator + "SQLOut_%d.csv", retCount - 1, retCount-1));
        DataSetIterator testDataIter = new SequenceRecordReaderDataSetIterator(testFeatures, miniBatchSize, -1, labelIndex, true);

        testDataIter.setPreProcessor(normalizer);   //Note that we are using the exact same normalization process as the training data
        DataSet testData = testDataIter.next();
        // ----- Configure the network -----
        /**
         * Need to play more here and bring the predictions close to 190 which was the range
         * of AAPL stocks around the target time here. Adding more layer hasnt helped nor changing
         * the seed value, or activation functions to ELU / RELU.
         */
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.9)) // updated value based on example in the book
                .activation(Activation.RELU)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                //.updater(Updater.ADAM)
                //.learningRate(0.0015)
                .l2(0.001)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
                .gradientNormalizationThreshold(0.5)
                .list()
                .layer(0, new LSTM.Builder().activation(Activation.TANH).nIn(88).nOut(10).build())
                //.layer(1, new LSTM.Builder().activation(Activation.TANH).nIn(10).nOut(10).build())
                //.layer(2, new LSTM.Builder().activation(Activation.TANH).nIn(10).nOut(10).build())
                //.layer(3, new LSTM.Builder().activation(Activation.TANH).nIn(10).nOut(10).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(10).nOut(numLabelClasses).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(20).tBPTTBackwardLength(5)
                //.pretrain(true)
                //.backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(20));   //Print the score (loss function value) every 20 iterations

        // ----- Train the network, evaluating the test set performance at each epoch -----
        int nEpochs = 5;
        //String str = "Test set evaluation at epoch %d: Accuracy = %.2f, F1 = %.2f";
        for (int i = 0; i < nEpochs; i++) {
            net.fit(trainData);
            RegressionEvaluation evaluation = net.evaluateRegression(testDataIter);
            log.info(evaluation.stats());
            testDataIter.reset();
            trainData.reset();
        }

        //Init rrnTimeStemp with train data and predict test data
        //net.rnnTimeStep(trainData.getLabels());
        INDArray predicted = net.rnnTimeStep(testData.getFeatures());
        normalizer.revertLabels(predicted);
        log.info("PREDICTIONS:\n"+predicted);


    }


    //This method downloads the data, and converts the "one time series per line" format into a suitable
    //CSV sequence format that DataVec (CsvSequenceRecordReader) and DL4J can read.
    private void downloadUCIData() throws Exception {

        String query = "";
        String strTableHeading = "";
        int numTableColumns = 89;
        //boolean overwrite = false; // Flag to avoid SQL calls if we are okay with using existing files
        sqlConnector.outSqlToCSV(query, strTableHeading, numTableColumns, false);

        /*
        if (baseDir.exists()) return;    //Data already exists, don't download it again

        String url = "https://archive.ics.uci.edu/ml/machine-learning-databases/synthetic_control-mld/synthetic_control.data";
        String data = IOUtils.toString(new URL(url));

        String[] lines = data.split("\n");

        //Create directories
        baseDir.mkdir();
        baseTrainDir.mkdir();
        featuresDirTrain.mkdir();
        labelsDirTrain.mkdir();
        baseTestDir.mkdir();
        featuresDirTest.mkdir();
        labelsDirTest.mkdir();

        int lineCount = 0;
        List<Pair<String, Integer>> contentAndLabels = new ArrayList<>();
        for (String line : lines) {
            String transposed = line.replaceAll(" +", "\n");

            //Labels: first 100 examples (lines) are label 0, second 100 examples are label 1, and so on
            contentAndLabels.add(new Pair<>(transposed, lineCount++ / 100));
        }

        //Randomize and do a train/test split:
        Collections.shuffle(contentAndLabels, new Random(12345));

        int nTrain = 450;   //75% train, 25% test
        int trainCount = 0;
        int testCount = 0;
        for (Pair<String, Integer> p : contentAndLabels) {
            //Write output in a format we can read, in the appropriate locations
            File outPathFeatures;
            File outPathLabels;
            if (trainCount < nTrain) {
                outPathFeatures = new File(featuresDirTrain, trainCount + ".csv");
                outPathLabels = new File(labelsDirTrain, trainCount + ".csv");
                trainCount++;
            } else {
                outPathFeatures = new File(featuresDirTest, testCount + ".csv");
                outPathLabels = new File(labelsDirTest, testCount + ".csv");
                testCount++;
            }

            FileUtils.writeStringToFile(outPathFeatures, p.getFirst());
            FileUtils.writeStringToFile(outPathLabels, p.getSecond().toString());
        }
        */
    }


}