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

/** This class provides functionality for interfacing with the C5.0 Decision Trees software package.
 * @author Stephen Piccolo
 */
public class C5Learner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        throw new Exception("Not implemented");
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> parameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        String uniqueID = MiscUtilities.GetUniqueID();

        // Create input files for C5
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, true).CreateC5NamesFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, true).CreateC5TrainDataFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, testData, trainingData, true).CreateC5TestDataFile();

        String inputPath = Settings.TEMP_DATA_DIR + uniqueID;

        // Construct command-line arguments
        String command = commandTemplate.replace("{PROGRAM}", parameters.get(0)).replace("{INPUT_PATH}", inputPath);

        CommandLineClient.RunAnalysis(command);

        command = commandTemplate.replace("{PROGRAM}", parameters.get(1)).replace("{INPUT_PATH}", inputPath);

        // Invoke C5 from the command line to make predictions for the test instances
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command);

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