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

package mlflex.learners;

import mlflex.core.ModelPredictions;
import mlflex.core.Prediction;
import mlflex.core.Predictions;
import mlflex.core.*;
import mlflex.helper.*;

import java.util.*;

/** This class provides an interface into the Orange machine-learning software package. Using this interface, Orange can be used for feature selection and classification. Please see the README files for information about how to install and configure Orange on the computer where ML-Flex is being executed.
 * @author Stephen Piccolo
 */
public class OrangeLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        // Create a file with the training data that can be used as an input to Orange
        String dataFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeDataForRanking_" + MiscUtilities.GetUniqueID(), trainData, null, true).CreateOrangeFile().GetOrangeFilePath();

        // Specify output paths
        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + MiscUtilities.GetUniqueID() + "/";
        String outputFileName = "RankedFeatures_" + MiscUtilities.GetUniqueID() + ".txt";

        FileUtilities.CreateDirectoryIfNotExists(outputDirectoryPath);

        // Build the command arguments that will be passed to Orange
        String command = commandTemplate.replace("{ALGORITHM}", "\"" + algorithmParameters.get(0) + "\"").replace("{INPUT_TRAINING_FILE}", dataFilePath).replace("{OUTPUT_FILE}", outputDirectoryPath + outputFileName);

        // Invoke Orange at the command line
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command, outputDirectoryPath);

        // Parse the selected features from the output
        ArrayList<String> features = ListUtilities.CreateStringList(CommandLineClient.GetCommandResult(results, outputFileName).split("\n"));

        // Delete unnecessary files
        FileUtilities.DeleteFile(dataFilePath);
        FileUtilities.DeleteFile(outputDirectoryPath + outputFileName);

        return features;
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection testData) throws Exception
    {
        // Create the input file for training data
        String trainingFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeTrain_" + MiscUtilities.GetUniqueID(), trainData, testData, true).CreateOrangeFile().GetOrangeFilePath();

        // Create the input file for test data
        String testFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeTest_" + MiscUtilities.GetUniqueID(), testData, trainData, false).CreateOrangeFile().GetOrangeFilePath();

        // Specify file paths
        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + MiscUtilities.GetUniqueID() + "/";
        String predictionsFileName = "Predictions_" + MiscUtilities.GetUniqueID();
        String probabilitiesFileName = "Probabilities_" + MiscUtilities.GetUniqueID();

        FileUtilities.CreateDirectoryIfNotExists(outputDirectoryPath);

        // Construct the command arguments that will be passed to Orange
        String command = commandTemplate.replace("{ALGORITHM}", "\"" + algorithmParameters.get(0) + "\"").replace("{INPUT_TRAINING_FILE}", trainingFilePath).replace("{INPUT_TEST_FILE}", testFilePath).replace("{PREDICTIONS_FILE}", outputDirectoryPath + predictionsFileName).replace("{PROBABILITIES_FILE}", outputDirectoryPath + probabilitiesFileName);

        // Obtain results that resulted from Orange processing
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command, outputDirectoryPath);

        // Get raw prediction information
        String predictionText = CommandLineClient.GetCommandResult(results, predictionsFileName);
        String probabilityText = CommandLineClient.GetCommandResult(results, probabilitiesFileName);

        // Separate the raw output into lines of text
        ArrayList<String> predictionLines = ListUtilities.CreateStringList(predictionText.trim().split("\n"));
        ArrayList<String> probabilityLines = ListUtilities.CreateStringList(probabilityText.trim().split("\n"));
        ArrayList<String> probabilityClasses = ListUtilities.CreateStringList(probabilityLines.remove(0).split("\t"));

        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        // Parse through the raw output and extract a prediction + probabilities for each test instance
        for (DataValues testInstance : testData)
        {
            String actual = Singletons.InstanceVault.GetTransformedDependentVariableValue(testInstance.GetID());
            String prediction = predictionLines.remove(0);
            ArrayList<String> probabilities = ListUtilities.CreateStringList(probabilityLines.remove(0).trim().split("\t"));

            ArrayList<Double> classProbabilities = new ArrayList<Double>();

            for (String x : Singletons.InstanceVault.TransformedDependentVariableOptions)
                classProbabilities.add(ParseProbability(probabilityClasses, probabilities, x, prediction));

            predictions.add(new Prediction(testInstance.GetID(), actual, prediction, classProbabilities));
        }

        // Clean up
        FileUtilities.DeleteFile(trainingFilePath);
        FileUtilities.DeleteFile(testFilePath);

        return new ModelPredictions(CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY), new Predictions(predictions));
    }

    private double ParseProbability(ArrayList<String> probabilityClasses, ArrayList<String> probabilities, String classDescriptor, String predictedClass)
    {
        if (!probabilityClasses.contains(classDescriptor))
            return 0.0;

        String rawProbability = probabilities.get(probabilityClasses.indexOf(classDescriptor));

        if (rawProbability.equals("nan"))
        {
            if (classDescriptor.equals(predictedClass))
                return 1.0;
            return 0.0;
        }

        return Double.parseDouble(rawProbability);
    }
}
