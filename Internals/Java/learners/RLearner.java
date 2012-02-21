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

import mlflex.core.*;
import mlflex.helper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class contains methods for interacting with the R Project software via command-line calls.
 * @author Stephen Piccolo
 */
public class RLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        throw new Exception("Not implemented");
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        CheckDataTypes(trainingData, testData);

        // Create training data file that will be used as input
        String trainingFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "RTrain_" + MiscUtilities.GetUniqueID(), trainingData, testData, true).CreateTransposedTabDelimitedFile(false).GetTransposedTabDelimitedFilePath();

        // Create test data file that will be used as input
        String testFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "RTest_" + MiscUtilities.GetUniqueID(), testData, trainingData, false).CreateTransposedTabDelimitedFile(false).GetTransposedTabDelimitedFilePath();

        // Create output directory
        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + MiscUtilities.GetUniqueID() + "/";
        FileUtilities.CreateDirectoryIfNotExists(outputDirectoryPath);

        // Specify the name of the output file
        String outputFileName = "Output_" + MiscUtilities.GetUniqueID() + ".txt";

        // Build a list of command arguments
        String command = commandTemplate.replace("{ALGORITHM}", algorithmParameters.get(0)).replace("{INPUT_TRAINING_FILE}", trainingFilePath).replace("{INPUT_TEST_FILE}", testFilePath).replace("{OUTPUT_FILE}", outputDirectoryPath + outputFileName);

        // Retrieve the results
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command, outputDirectoryPath);

        // Remove the input files
        FileUtilities.DeleteFile(trainingFilePath);
        FileUtilities.DeleteFile(testFilePath);

        // Parse the output
        String outputText = CommandLineClient.GetCommandResult(results, outputFileName);

        return new ModelPredictions(CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY), ParsePredictions(outputText, testData));
    }

    private Predictions ParsePredictions(String outputText, DataInstanceCollection testData) throws Exception
    {
        ArrayList<String> outputLines = ListUtilities.CreateStringList(outputText.split("\n"));
        ArrayList<String> headerItems = ListUtilities.CreateStringList(outputLines.remove(0).split("\t"));

        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (int i=0; i<testData.Size(); i++)
        {
            DataValues testInstance = testData.Get(i);
            ArrayList<String> outputItems = ListUtilities.CreateStringList(outputLines.get(i).split("\t"));
            String predictedClass = outputItems.get(0);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (String dependentVariableValue : Singletons.InstanceVault.TransformedDependentVariableOptions)
                classProbabilities.add(Double.parseDouble(outputItems.get(headerItems.indexOf(dependentVariableValue))));

            predictions.add(new Prediction(testInstance.GetID(), Singletons.InstanceVault.GetTransformedDependentVariableValue(testInstance.GetID()), predictedClass, classProbabilities));
        }

        return new Predictions(predictions);
    }

    private void CheckDataTypes(DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        for (String dataPointName : trainingData.GetDataPointNames())
        {
            HashSet<String> uniqueValues = new HashSet<String>(trainingData.GetUniqueValues(dataPointName));
            uniqueValues.addAll(testData.GetUniqueValues(dataPointName));

            if (DataTypeUtilities.GetGeneralDataType(new ArrayList<String>(uniqueValues)).equals(GeneralDataType.Nominal))
            {
                if (uniqueValues.size() == 2)
                {
                    String oneOption = ListUtilities.SortStringList(new ArrayList<String>(uniqueValues)).get(0);
                    trainingData.BinarizeDataPoint(dataPointName, oneOption);
                    testData.BinarizeDataPoint(dataPointName, oneOption);
                    continue;
                }

                if (uniqueValues.size() > 2)
                    throw new Exception("The " + this.getClass().getName() + " class is not equipped to handle discrete variables with more than two value options.");
            }
        }
    }
}