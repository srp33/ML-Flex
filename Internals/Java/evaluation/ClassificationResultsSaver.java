// THIS SOURCE CODE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND, AND ITS AUTHOR AND THE JOURNAL OF MACHINE LEARNING RESEARCH (JMLR) AND JMLR'S PUBLISHERS AND DISTRIBUTORS, DISCLAIM ANY AND ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND ANY WARRANTIES OR NON INFRINGEMENT. THE USER ASSUMES ALL LIABILITY AND RESPONSIBILITY FOR USE OF THIS SOURCE CODE, AND NEITHER THE AUTHOR NOR JMLR, NOR JMLR'S PUBLISHERS AND DISTRIBUTORS, WILL BE LIABLE FOR DAMAGES OF ANY KIND RESULTING FROM ITS USE. Without lim- iting the generality of the foregoing, neither the author, nor JMLR, nor JMLR's publishers and distributors, warrant that the Source Code will be error-free, will operate without interruption, or will meet the needs of the user.
// 
// --------------------------------------------------------------------------
// 
// Copyright 2011 Stephen Piccolo
// 
// This file is part of ML-Flex.
// 
// ML-Flex is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
// 
// ML-Flex is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with ML-Flex. If not, see <http://www.gnu.org/licenses/>.

package mlflex.evaluation;

import mlflex.core.*;
import mlflex.ensemblelearners.AbstractEnsembleLearner;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;
import mlflex.helper.ResultsFileUtilities;

import java.util.ArrayList;

/** This class has functionality to save results from classification analyses. These results are saved in a variety of text files within the Output directory.
 * @author Stephen Piccolo
 */
public class ClassificationResultsSaver
{
    // These descriptions are used in headers of the results files as an aid to users
    private static final String PERFORMANCE_METRICS_COMMENT = " Measures of classification performance across all classes (dependent variables). The weighted average metrics were weighted by the class distribution. Cross-validation assignments were stratified by class, where possible.";
    private static final String PER_CLASS_METRICS_COMMENT = " Measures of classification performance for each class (dependent variable).";
    private static final String CONFUSION_MATRIX_COMMENT = " Confusion matrix that helps visual classification performance for each class (dependent variable).";
    private static final String NUM_FEATURES_COMMENT = " For the top N features identified via feature selection, the AUC that resulted when N features were used for classification. Note that it is possible none of these AUC values will be equal to the AUC values in the performance metrics files because the 'best' number of features is selected in each cross-validation fold, whereas these values are calculated using the same number of features in each fold.";
    private static final String ALGORITHM_OUTPUT_COMMENT = " Standard output produced by the external machine-learning library that was used for classification.\n";
    private static final String PREDICTIONS_COMMENT = " Instance-level predictions produced via classification.";
    private static final String ENSEMBLE_COMMENT = " Various ensemble-learning approaches were used for classification. See the README files for details.";
    private static final String MULTI_ITERATION_COMMENT = " Each column contains the results for a given iteration.";
    private static final String MULTI_ITERATION_AVERAGE_COMMENT = " For each combination, results were averaged across all iterations.";
    private static final String OVERALL_COMMENT = " Each column contains results for a particular combination of data set and classification algorithm (and feature selection algorithm where applicable).";

    private ModelSelector _modelSelector;
    private boolean _evaluateInner;

    /** Constructor that allows specification of objects that contain information about the classification tasks that have been performed.
     *
     * @param modelSelector Model selector representing the combination of data processor and algorithms for which results will be saved.
     * @param evaluateInner Whether to save results for inner cross-validation folds
     */
    public ClassificationResultsSaver(ModelSelector modelSelector, boolean evaluateInner) throws Exception
    {
        _modelSelector = modelSelector;
        _evaluateInner = evaluateInner;
    }

    /** Saves results from a classification experiment to output files for outer (and possibly inner) cross-validation folds.
     *
     * @throws Exception
     */
    public void SaveClassificationResults() throws Exception
    {
        // Save results for outer fold(s)
        SaveClassificationResultValues(_modelSelector.GetResultsFilePaths(true, false), true);

        // Is this a training/testing experiment rather than cross-validation? If so, save prediction results for training data.
        if (_evaluateInner && Settings.IsTrainTestExperiment())
            SaveClassificationResultValues(_modelSelector.GetResultsFilePaths(true, true), false);

        SaveAlgorithmOutputSummary(_modelSelector.GetResultsFilePaths(true, false).ALGORITHM_OUTPUT);
    }

    /** Saves classification result values for either inner or outer cross-validation folds. This should only get called for inner folds if we are performing a training/testing experiment.
     *
     * @param filePaths File paths indicating where the results will be stored
     * @param outer Whether results should be saved for outer cross-validation folds
     * @throws Exception
     */
    private void SaveClassificationResultValues(ClassificationResultsFilePaths filePaths, boolean outer) throws Exception
    {
        // Assign variables that depend on whether results are being saved for inner or outer cross-validation folds
        Predictions predictions = outer ? _modelSelector.GetBestOuterPredictionsAllFolds() : _modelSelector.GetBestInnerPredictions(1);

        // Calculate and save performance metrics
        SavePerformanceMetrics(new PredictionResults(predictions), filePaths.PERFORMANCE_METRICS, PERFORMANCE_METRICS_COMMENT);

        // Calculate and save per-class metrics
        SavePerClassMetrics(new PredictionResults(predictions), filePaths.PER_CLASS_METRICS, PER_CLASS_METRICS_COMMENT);
        SaveConfusionMatrix(new PredictionResults(predictions), filePaths.CONFUSION_MATRIX, CONFUSION_MATRIX_COMMENT);

        // Save results that indicate how performance varied per number of features included in models
        SaveNumFeaturesResults(filePaths.NUM_FEATURES, _modelSelector, outer, NUM_FEATURES_COMMENT);

        // Save file that describes predictions that have been made, including class probabilities when applicable
        SavePredictionsFile(predictions, filePaths.PREDICTIONS, PREDICTIONS_COMMENT);
    }

    /** Saves results to output files for a wide variety of performance metrics.
     *
     * @param results Prediction results object used for calculating the metrics
     * @param outFilePath Absolute path where the file will be saved
     * @param headerComment Descriptive comment that will be placed at the top of the output file
     * @throws Exception
     */
    private static void SavePerformanceMetrics(PredictionResults results, String outFilePath, String headerComment) throws Exception
    {
        ArrayList<NameValuePair> nameValueResults = new ArrayList<NameValuePair>();

        if (results.Predictions.Size() == 0)
            return;

        // Basic definitions for many of the evaluation metrics can be found here: http://weka.sourceforge.net/doc/weka/classifiers/Evaluation.html

        nameValueResults.add(NameValuePair.Create("Weighted average AUC", results.GetWekaEvaluation().weightedAreaUnderROC()));
        nameValueResults.add(NameValuePair.Create("Weighted average F-Measure", results.GetWekaEvaluation().weightedFMeasure()));
        nameValueResults.add(NameValuePair.Create("Weighted average true positive rate", results.GetWekaEvaluation().weightedTruePositiveRate()));
        nameValueResults.add(NameValuePair.Create("Weighted average false positive rate", results.GetWekaEvaluation().weightedFalsePositiveRate()));
        nameValueResults.add(NameValuePair.Create("Weighted average sensitivity (recall)", results.GetWekaEvaluation().weightedRecall()));

        // Not sure what the following should mean because you don't know which class is 'positive' and which is negative' and because you may have more than two classes. These aren't displayed in Weka output, possible for same reason.
        //nameValueResults.add(NameValuePair.Create("Weighted True Negative Rate", results.GetWekaEvaluation().weightedTrueNegativeRate()));
        //nameValueResults.add(NameValuePair.Create("Weighted False Negative Rate", results.GetWekaEvaluation().weightedFalseNegativeRate()));
        // This one is displayed in Weka, but I'm not sure what they mean by it
        //nameValueResults.add(NameValuePair.Create("Weighted Specificity / Precision", results.GetWekaEvaluation().weightedPrecision()));

        nameValueResults.add(NameValuePair.Create("Total number correct", results.GetTotalNumberCorrect()));
        nameValueResults.add(NameValuePair.Create("Total number incorrect", results.GetTotalNumberIncorrect()));
        nameValueResults.add(NameValuePair.Create("Accuracy", results.GetAccuracy()));
        nameValueResults.add(NameValuePair.Create("Error rate", results.GetErrorRate()));
        nameValueResults.add(NameValuePair.Create("Error rate expected by chance", results.GetBaselineErrorRate()));
        nameValueResults.add(NameValuePair.Create("Improvement over error rate expected by chance", results.GetBaselineImprovement()));
        nameValueResults.add(NameValuePair.Create("Kappa statistic", results.GetWekaEvaluation().kappa()));
        nameValueResults.add(NameValuePair.Create("Mean absolute error", results.GetWekaEvaluation().meanAbsoluteError()));
        nameValueResults.add(NameValuePair.Create("Mean prior absolute error", results.GetWekaEvaluation().meanPriorAbsoluteError()));
        nameValueResults.add(NameValuePair.Create("Root mean squared error", results.GetWekaEvaluation().rootMeanSquaredError()));
        nameValueResults.add(NameValuePair.Create("Root mean prior squared error", results.GetWekaEvaluation().rootMeanPriorSquaredError()));
        nameValueResults.add(NameValuePair.Create("Relative absolute error (%)", results.GetWekaEvaluation().relativeAbsoluteError()));
        nameValueResults.add(NameValuePair.Create("Root relative squared error (%)", results.GetWekaEvaluation().rootRelativeSquaredError()));

        // These copy what is output in Weka. I need to find a reference to what they actually mean
        nameValueResults.add(NameValuePair.Create("Kononenko & Bratko information score (bits)", results.GetWekaEvaluation().KBInformation()));
        nameValueResults.add(NameValuePair.Create("Kononenko & Bratko information score (bits / instance)", results.GetWekaEvaluation().KBMeanInformation()));
        nameValueResults.add(NameValuePair.Create("Kononenko & Bratko relative information score (%)", results.GetWekaEvaluation().KBRelativeInformation()));

        // These copy what is output in Weka. I need to find a reference to what they actually mean
        nameValueResults.add(NameValuePair.Create("Class complexity - order (bits)", results.GetWekaEvaluation().SFPriorEntropy()));
        nameValueResults.add(NameValuePair.Create("Class complexity - order (bits / instance)", results.GetWekaEvaluation().SFMeanPriorEntropy()));
        nameValueResults.add(NameValuePair.Create("Class complexity - scheme (bits)", results.GetWekaEvaluation().SFSchemeEntropy()));
        nameValueResults.add(NameValuePair.Create("Class complexity - scheme (bits / instance)", results.GetWekaEvaluation().SFMeanSchemeEntropy()));
        nameValueResults.add(NameValuePair.Create("Complexity improvement (bits)", results.GetWekaEvaluation().SFEntropyGain()));
        nameValueResults.add(NameValuePair.Create("Complexity improvement (bits / instance)", results.GetWekaEvaluation().SFMeanEntropyGain()));

//        if (Singletons.InstanceVault.RawDependentVariableValuesAreContinuous())
//            nameValueResults.add(NameValuePair.Create("Log-Rank Statistic", new SurvivalHelper(results.Predictions).GetLogRankStatistic()));

        nameValueResults.add(0, new NameValuePair("Metric", "Result"));

        // Save the metrics to a file
        ResultsFileUtilities.AppendMatrixColumn(nameValueResults, outFilePath, headerComment);
    }

    /** Saves results to output files for a wide variety of per-class metrics.
     *
     * @param results Prediction results object used for calculating the metrics
     * @param outFilePath File path where the results will be saved
     * @param headerComment Descriptive comment that will be placed at the top of the output file
     * @throws Exception
     */
    private static void SavePerClassMetrics(PredictionResults results, String outFilePath, String headerComment) throws Exception
    {
        if (results.Predictions.Size() == 0)
            return;

        ArrayList<NameValuePair> nameValueResults = new ArrayList<NameValuePair>();

        for (String dependentVariableClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            nameValueResults.add(NameValuePair.Create("Number instances predicted as [" + dependentVariableClass + "]", results.GetNumPredictedAsDependentVariableClass(dependentVariableClass)));
            nameValueResults.add(NameValuePair.Create("Number instances predicted as [" + dependentVariableClass + "] correctly", results.GetNumPredictedAsDependentVariableClassCorrectly(dependentVariableClass)));
            nameValueResults.add(NameValuePair.Create("Number instances predicted as [" + dependentVariableClass + "] incorrectly", results.GetNumPredictedAsDependentVariableClassIncorrectly(dependentVariableClass)));
        }

        for (String dependentVariableClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            nameValueResults.add(NameValuePair.Create("Proportion instances predicted as [" + dependentVariableClass + "]", results.GetProportionPredictedAsDependentVariableClass(dependentVariableClass)));
            // Could be considered true-positive rate
            nameValueResults.add(NameValuePair.Create("Proportion instances predicted as [" + dependentVariableClass + "] correctly", results.GetProportionPredictedAsDependentVariableClassCorrectly(dependentVariableClass)));
            // Could be considered false-positive rate
            nameValueResults.add(NameValuePair.Create("Proportion instances predicted as [" + dependentVariableClass + "] incorrectly", results.GetProportionPredictedAsDependentVariableClassIncorrectly(dependentVariableClass)));
        }

        for (String dependentVariableClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            nameValueResults.add(NameValuePair.Create("Number [" + dependentVariableClass + "] instances predicted correctly", results.GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass)));
            nameValueResults.add(NameValuePair.Create("Number [" + dependentVariableClass + "] instances predicted incorrectly", results.GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass)));
        }

        for (String dependentVariableClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            // Could be considered sensitivity / recall
            nameValueResults.add(NameValuePair.Create("Proportion [" + dependentVariableClass + "] instances predicted correctly", results.GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass)));
            nameValueResults.add(NameValuePair.Create("Proportion [" + dependentVariableClass + "] instances predicted incorrectly", results.GetProportionActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass)));
        }

        nameValueResults.add(0, new NameValuePair("Metric", "Result"));

        ResultsFileUtilities.AppendMatrixColumn(nameValueResults, outFilePath, headerComment);
    }

    private static void SaveConfusionMatrix(PredictionResults predictionResults, String outFilePath, String headerComment) throws Exception
    {
        ArrayList<ArrayList<String>> outLines = new ArrayList<ArrayList<String>>();

        // Add actual classes as header row
        outLines.add(ListUtilities.CreateStringList(""));
        outLines.get(0).addAll(ListUtilities.Prefix(Singletons.InstanceVault.TransformedDependentVariableOptions, "Predicted as "));

        // Add rows
        for (String actualClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            ArrayList<String> row = ListUtilities.CreateStringList(actualClass);

            for (String predictedClass : Singletons.InstanceVault.TransformedDependentVariableOptions)
                row.add(String.valueOf(predictionResults.GetNumActualsPredictedAs(actualClass, predictedClass)));

            outLines.add(row);
        }

        FileUtilities.WriteLinesToFile(outFilePath, outLines, headerComment);
    }

    /** Saves results that indicate how performance varied per number of features included in models.
     *
     * @param filePath Absolute path where the results file will be saved
     * @param modelSelector Model selector representing the combination of data processor and algorithms for which results will be saved
     * @param outer Whether results should be saved for outer cross-validation folds or not
     * @param headerComment Descriptive comment that will be placed at the top of the output file
     * @throws Exception
     */
    private void SaveNumFeaturesResults(String filePath, ModelSelector modelSelector, boolean outer, String headerComment) throws Exception
    {
        ArrayList<Integer> numFeaturesOptions = Singletons.Config.GetNumFeaturesOptions(modelSelector.Processor, modelSelector.FeatureSelectionAlgorithm);

        if (numFeaturesOptions.size() <= 1)
            return;

        ArrayList<NameValuePair> nameValueResults = new ArrayList<NameValuePair>();

        for (int numFeatures : numFeaturesOptions)
        {
            Predictions predictions = outer ? modelSelector.GetOuterPredictionsAllFolds(numFeatures) : modelSelector.GetInnerPredictions(numFeatures, 1);
            nameValueResults.add(NameValuePair.Create(String.valueOf(numFeatures), new PredictionResults(predictions).GetWekaEvaluation().weightedAreaUnderROC()));
        }

        nameValueResults.add(0, new NameValuePair("Number of Features", "AUC"));

        ResultsFileUtilities.AppendMatrixColumn(nameValueResults, filePath, headerComment);
    }

    /** Saves an output file that summarizes predictions that were made. This output file can be used by external applications to analyze the predictions.
     *
     * @param predictions Predictions object  @throws Exception
     * @param outFilePath Absolute path where the file will be stored
     * @param headerComment Descriptive comment that will be placed at the top of the output file
     */
    private static void SavePredictionsFile(Predictions predictions, String outFilePath, String headerComment) throws Exception
    {
        ArrayList<String> dependentVariableClasses = Singletons.InstanceVault.TransformedDependentVariableOptions;
        ArrayList<ArrayList<String>> outRows = new ArrayList<ArrayList<String>>();

        // Format the file header
        ArrayList<String> headerVals = ListUtilities.CreateStringList("Instance_ID", "Dependent_Variable_Value", "Prediction");
        for (String x : dependentVariableClasses)
            headerVals.add(x + "_Probability");

        // Include the raw dependent variable when relevant
        if (Singletons.InstanceVault.RawDependentVariableValuesAreContinuous())
            headerVals.add(1, "Raw_Dependent_Variable_Value");

        outRows.add(headerVals);

        // Build output for each prediction that has been made
        for (String instanceID : predictions.GetInstanceIDs())
        {
            Prediction prediction = predictions.Get(instanceID);

            ArrayList<String> predictionVals = ListUtilities.CreateStringList(prediction.InstanceID, prediction.DependentVariableValue, prediction.Prediction);

            for (int i=0; i<dependentVariableClasses.size(); i++)
                predictionVals.add(String.valueOf(prediction.ClassProbabilities.get(i)));

            // Include the raw dependent variable value when relevant
            if (Singletons.InstanceVault.RawDependentVariableValuesAreContinuous())
                predictionVals.add(1, Singletons.InstanceVault.GetRawDependentVariableValue(prediction.InstanceID));

            outRows.add(predictionVals);
        }

        // Write the information to a file
        FileUtilities.WriteLinesToFile(outFilePath, outRows, headerComment);
    }

    private void SaveAlgorithmOutputSummary(String outFilePath) throws Exception
    {
        ArrayList<String> inFilePaths = new ArrayList<String>();
        ArrayList<String> inFileDescriptions = new ArrayList<String>();

        for (PredictionEvaluator evaluator : _modelSelector.PredictionEvaluators)
            if (FileUtilities.FileExists(evaluator.GetAlgorithmOutputFilePath()))
            {
                inFilePaths.add(evaluator.GetAlgorithmOutputFilePath());

                if (_modelSelector.PredictionEvaluators.size() > 1)
                {
                    String description = "";

                    if (inFileDescriptions.size() > 0)
                        description = "\n";

                    description += ListUtilities.Join(ListUtilities.CreateStringList("#", 50), "") + "\n";
                    description += "## Fold " + evaluator.OuterFold;

                    if (Singletons.Config.GetNumFeaturesOptions(_modelSelector.Processor, _modelSelector.FeatureSelectionAlgorithm).size() > 1)
                        description += ", Number Features = "  + evaluator.NumFeatures;

                    description += "\n";
                    description += ListUtilities.Join(ListUtilities.CreateStringList("#", 50), "") + "\n\n";

                    inFileDescriptions.add(description);
                }
                else
                    inFileDescriptions.add("");
            }

        if (inFilePaths.size() > 0)
            FileUtilities.ConcatenateFiles(inFilePaths, inFileDescriptions, outFilePath, ALGORITHM_OUTPUT_COMMENT);
    }

    /** When an experiment has multiple iterations, this method saves a summary that spans all iterations.
     *
     * @param isForTrainingData Whether the results are for training data (inner folds)
     * @throws Exception
     */
    public void SaveMultiIterationClassificationResults(boolean isForTrainingData) throws Exception
    {
        // Get the column names that will be used (one for each iteration)
        ArrayList<String> columnNames = ListUtilities.CreateStringList(ListUtilities.CreateIntegerSequenceList(1, Singletons.Config.GetNumIterations()));
        columnNames = ListUtilities.Prefix(columnNames, "Iteration ");

        // Combine results for performance metrics
        ResultsFileUtilities.CombineMatrixFiles(PERFORMANCE_METRICS_COMMENT + MULTI_ITERATION_COMMENT, ResultsFileUtilities.GetAllIterationFilePaths(_modelSelector.GetResultsFilePaths(true, isForTrainingData).PERFORMANCE_METRICS), _modelSelector.GetResultsFilePaths(false, isForTrainingData).PERFORMANCE_METRICS, false, ListUtilities.InsertIntoStringList(columnNames, "Metric", 0));

        // Combine results for per-class metrics
        ResultsFileUtilities.CombineMatrixFiles(PER_CLASS_METRICS_COMMENT + MULTI_ITERATION_COMMENT, ResultsFileUtilities.GetAllIterationFilePaths(_modelSelector.GetResultsFilePaths(true, isForTrainingData).PER_CLASS_METRICS), _modelSelector.GetResultsFilePaths(false, isForTrainingData).PER_CLASS_METRICS, false, ListUtilities.InsertIntoStringList(columnNames, "Metric", 0));

        // Combine results for number of features vs. AUC
        if (FeatureSelectionEvaluator.NeedToSelectFeatures(_modelSelector.Processor, _modelSelector.FeatureSelectionAlgorithm) && Singletons.Config.GetNumFeaturesOptions(_modelSelector.Processor, _modelSelector.FeatureSelectionAlgorithm).size() > 1)
            ResultsFileUtilities.CombineMatrixFiles(NUM_FEATURES_COMMENT + MULTI_ITERATION_COMMENT, ResultsFileUtilities.GetAllIterationFilePaths(_modelSelector.GetResultsFilePaths(true, isForTrainingData).NUM_FEATURES), _modelSelector.GetResultsFilePaths(false, isForTrainingData).NUM_FEATURES, false, ListUtilities.InsertIntoStringList(columnNames, "Number of Features", 0));
    }

    /** This method saves results for ensemble learning.
     *
     * @param predictions Ensemble learning predictions that were made
     * @param filePaths Paths where the files should be stored
     * @throws Exception
     */
    public static void SaveEnsembleResults(Predictions predictions, ClassificationResultsFilePaths filePaths) throws Exception
    {
        SavePerformanceMetrics(new PredictionResults(predictions), filePaths.PERFORMANCE_METRICS, PERFORMANCE_METRICS_COMMENT + ENSEMBLE_COMMENT);
        SavePerClassMetrics(new PredictionResults(predictions), filePaths.PER_CLASS_METRICS, PER_CLASS_METRICS_COMMENT + ENSEMBLE_COMMENT);
        SaveConfusionMatrix(new PredictionResults(predictions), filePaths.CONFUSION_MATRIX, CONFUSION_MATRIX_COMMENT + ENSEMBLE_COMMENT);
        SavePredictionsFile(predictions, filePaths.PREDICTIONS, PREDICTIONS_COMMENT + ENSEMBLE_COMMENT);
    }

    /** This method stores ensemble results when multiple iterations were performed.
     *
     * @param ensembleLearner The ensemble learner that generated the predictions
     * @throws Exception
     */
    public static void SaveMultiIterationEnsembleResults(AbstractEnsembleLearner ensembleLearner) throws Exception
    {
        ArrayList<String> columnNames = ListUtilities.CreateStringList(ListUtilities.CreateIntegerSequenceList(1, Singletons.Config.GetNumIterations()));
        columnNames = ListUtilities.Prefix(columnNames, "Iteration ");

        ResultsFileUtilities.CombineMatrixFiles(PERFORMANCE_METRICS_COMMENT + MULTI_ITERATION_COMMENT, ResultsFileUtilities.GetAllIterationFilePaths(ensembleLearner.GetResultsFilePaths(true).PERFORMANCE_METRICS), ensembleLearner.GetResultsFilePaths(false).PERFORMANCE_METRICS, false, ListUtilities.InsertIntoStringList(columnNames, "Metric", 0));
        ResultsFileUtilities.CombineMatrixFiles(PER_CLASS_METRICS_COMMENT + MULTI_ITERATION_COMMENT, ResultsFileUtilities.GetAllIterationFilePaths(ensembleLearner.GetResultsFilePaths(true).PER_CLASS_METRICS), ensembleLearner.GetResultsFilePaths(false).PER_CLASS_METRICS, false, ListUtilities.InsertIntoStringList(columnNames, "Metric", 0));
    }

    /** This method saves output files that summarize the performance of the entire experiment at a high level. When multiple iterations are used, the values are averaged.
     *
     * @throws Exception
     */
    public static void SaveOverallResultsSummaries() throws Exception
    {
        String outDirPath = Settings.GetOutputResultsDir("", false);

        ArrayList<String> inDirPaths = new ArrayList<String>();
        ArrayList<String> headerItems = ListUtilities.CreateStringList("");

        // Add a column name for each model selector
        for (ModelSelector modelSelector : ModelSelector.GetAllModelSelectors())
        {
            inDirPaths.add(modelSelector.GetResultsFilePaths(false, false).DIRECTORY_PATH);
            headerItems.add(modelSelector.GetDescription(" - "));
        }

        // Add a column name for each ensemble learner
        for (AbstractEnsembleLearner ensembleLearner : AbstractEnsembleLearner.GetAllEnsembleLearners())
        {
            inDirPaths.add(ensembleLearner.GetResultsFilePaths(false).DIRECTORY_PATH);
            headerItems.add(ensembleLearner.GetFormattedDescription());
        }

        ClassificationResultsFilePaths fileNames = new ClassificationResultsFilePaths("");

        // Save the summary for performance metrics
        ResultsFileUtilities.CombineMatrixFiles("", ListUtilities.AppendStringToListItems(inDirPaths, fileNames.PERFORMANCE_METRICS), outDirPath + fileNames.PERFORMANCE_METRICS, true, null);
        String headerComment = "#" + PERFORMANCE_METRICS_COMMENT + OVERALL_COMMENT + (Singletons.Config.GetNumIterations() > 1 ? MULTI_ITERATION_AVERAGE_COMMENT : "");
        FileUtilities.InsertFileHeaderLine(outDirPath + fileNames.PERFORMANCE_METRICS, headerComment + "\n" + ListUtilities.Join(headerItems, "\t"));

        // Save the summary for per-class metrics
        ResultsFileUtilities.CombineMatrixFiles("", ListUtilities.AppendStringToListItems(inDirPaths, fileNames.PER_CLASS_METRICS), outDirPath + fileNames.PER_CLASS_METRICS, true, null);
        headerComment = "#" + PER_CLASS_METRICS_COMMENT + OVERALL_COMMENT + (Singletons.Config.GetNumIterations() > 1 ? MULTI_ITERATION_AVERAGE_COMMENT : "");
        FileUtilities.InsertFileHeaderLine(outDirPath + fileNames.PER_CLASS_METRICS, headerComment + "\n" + ListUtilities.Join(headerItems, "\t"));
    }
}