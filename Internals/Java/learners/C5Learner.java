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

import java.util.*;

/** This class provides functionality for interfacing with the C5.0 Decision Trees software package.
 * @author Stephen Piccolo
 */
public class C5Learner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        throw new Exception("Not implemented");
    }

    @Override
    public ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        String uniqueID = MiscUtilities.GetUniqueID();

        // Create input files for C5
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, true).CreateC5NamesFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, true).CreateC5TrainDataFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, testData, trainingData, true).CreateC5TestDataFile();

        String tempFileDescription = Settings.TEMP_DATA_DIR + uniqueID;

        // Construct an object with command-line arguments specified and another for parameters
        ArrayList<String> commandArgs = ListUtilities.CreateStringList(classificationParameters.get(0));
        commandArgs.add("-f");
        commandArgs.add(tempFileDescription);

        // Invoke C5 from the command line to build the model
        CommandLineClient.RunAnalysis(commandArgs);

        commandArgs = ListUtilities.CreateStringList(classificationParameters.get(1));
        commandArgs.add("-f");
        commandArgs.add(tempFileDescription);

        // Invoke C5 from the command line to make predictions for the test instances
        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs);

        // Clean up
        FileUtilities.DeleteFilesInDirectory(Settings.TEMP_DATA_DIR, uniqueID + ".*");

        // Parse the output file lines
        ArrayList<String> outputLines = ListUtilities.CreateStringList(CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY).split("\n"));
        outputLines.remove(0);
        outputLines.remove(0);
        outputLines.remove(0);

        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        // Parse through the output and generate a prediction object for each test instance
        for (int i = 0; i < outputLines.size(); i++)
        {
            String prediction = outputLines.get(i).split("\\s+")[3];
            double confidence = Double.parseDouble(outputLines.get(i).split("\\s+")[4].replace("[", "").replace("]", ""));

            DataValues instance = testData.Get(i);

            ArrayList<String> dependentVariableOptions = Singletons.InstanceVault.TransformedDependentVariableOptions;
            ArrayList<Double> classProbabilities = new ArrayList<Double>();

            //It uses the confidence value assigned by C5.0 for the predicted class and splits the remaining confidence equally across the other classes (for lack of a better solution).
            for (String dependentVariableValue : dependentVariableOptions)
                classProbabilities.add(prediction.equals(dependentVariableValue) ? confidence : ((1 - confidence) / ((double)(dependentVariableOptions.size()-1))));

            predictions.add(new Prediction(instance.GetID(), Singletons.InstanceVault.GetTransformedDependentVariableValue(instance.GetID()), prediction, classProbabilities));
        }

        return new ModelPredictions(CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY), new Predictions(predictions));
    }
}