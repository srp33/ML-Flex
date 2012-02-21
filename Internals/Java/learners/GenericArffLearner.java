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

/** This class is designed to support interfacing between ML-Flex and any third-party machine-learning software that can handle input files in the ARFF format. The user specifies all command-line arguments in the Config/* files. It is also important that the third-party software output the results in a specific (simple) format and in a certain order. Within the ML-Flex distribution is a tutorial that explains more about how to do this.
 */
public class GenericArffLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        // Create an input file with the training data
        String inputFilePath = CreateInputFile(trainData);

        // Generate an output directory
        String outDirectory = GetOutputDirectory();

        // Specify the output file name
        String outFileName = GetRanksOutFileName();

        // Create command that will be passed to the program. Look for tokens so these file paths can be passed to the program
        String command = commandTemplate.replace("{INPUT_TRAINING_FILE}", inputFilePath).replace("{OUTPUT_FILE}", outDirectory + outFileName).replace("{PARAMETERS}", "\"" + ListUtilities.Join(algorithmParameters, "\" \"") + "\"");

        // Invoke the program at the command line
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command, outDirectory);

        // Delete the input file
        FileUtilities.DeleteFile(inputFilePath);

        // Retrieve the output file text
        String outputFileText = CommandLineClient.GetCommandResult(results, outFileName);

        // Create a list of features by splitting the output text by line markers
        return ListUtilities.CreateStringList(outputFileText.split("\n"));
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        // Create an input file with the training data
        String inputTrainingFilePath = CreateInputFile(trainingData);
        // Create an input file with the test data
        String inputTestFilePath = CreateInputFile(testData);

        // Generate the output directory
        String outDirectory = GetOutputDirectory();

        // Specify the output file name
        String outFileName = GetPredictionsOutFileName();

        // Create command that will be passed to the program. Look for tokens so these file paths can be passed to the program
        String command = commandTemplate.replace("{INPUT_TRAINING_FILE}", inputTrainingFilePath).replace("{INPUT_TEST_FILE}", inputTestFilePath).replace("{OUTPUT_FILE}", outDirectory + outFileName).replace("{PARAMETERS}", "\"" + ListUtilities.Join(algorithmParameters, "\" \"") + "\"");

        // Invoke the program at the command line
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command, outDirectory);

        // Delete the input files
        FileUtilities.DeleteFile(inputTrainingFilePath);
        FileUtilities.DeleteFile(inputTestFilePath);

        // Retrieve the standard output
        String standardOutput = CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY);

        // Retrieve the output file text
        String outputFileText = CommandLineClient.GetCommandResult(results, outFileName);

        // Split the output file text into lines, according to new line markers
        ArrayList<String> outputFileLines = ListUtilities.CreateStringList(outputFileText.split("\n"));

        // Retrieve the header information
        ArrayList<String> headerItems = ListUtilities.CreateStringList(outputFileLines.remove(0).split("\t"));

        // Build a Predictions object by parsing the output text
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        // Sort the test instance IDs because the predictions will be sorted
        ArrayList<String> testInstanceIDs = ListUtilities.SortStringList(testData.GetIDs());

        for (int i=0; i<testData.Size(); i++) // Assumes the predictions are in the same order as they were sent to the program
        {
            // Split the prediction line into separate items
            ArrayList<String> lineItems = ListUtilities.CreateStringList(outputFileLines.get(i).split("\t"));

            // Get the class value predicted by the program for this instance
            String predictionValue = lineItems.get(0);

            // Get the probability for each class that is specified by the program for this instance
            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (String dependentVariableOption : Singletons.InstanceVault.TransformedDependentVariableOptions)
                classProbabilities.add(Double.parseDouble(lineItems.get(headerItems.indexOf(dependentVariableOption))));

            // Create a prediction object using the parsed values
            Prediction prediction = new Prediction(testInstanceIDs.get(i), Singletons.InstanceVault.GetTransformedDependentVariableValue(testInstanceIDs.get(i)), predictionValue, classProbabilities);

            predictions.add(prediction);
        }

        return new ModelPredictions(standardOutput, new Predictions(predictions));
    }

    /** This method creates an input file in the format that is specific to this learner. It is designed to be extensible to support various input file formats.
     *
     * @param instances Data instances that will be output to the file
     * @return Path to the saved file
     * @throws Exception
     */
    protected String CreateInputFile(DataInstanceCollection instances) throws Exception
    {
        return new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), instances, null, true).CreateArffFile().GetArffFilePath();
    }

    /** Creates an output directory to where results will be stored.
     *
     * @return Path to the directory
     * @throws Exception
     */
    private String GetOutputDirectory() throws Exception
    {
        String outDirectory = Settings.TEMP_DATA_DIR + MiscUtilities.GetUniqueID() + "/";
        FileUtilities.CreateDirectoryIfNotExists(outDirectory);
        return outDirectory;
    }

    private String GetRanksOutFileName()
    {
        return "Ranks.txt";
    }

    private String GetPredictionsOutFileName()
    {
        return "Predictions.txt";
    }
}
