// THIS SOURCE CODE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND, AND ITS AUTHOR AND THE JOURNAL OF MACHINE LEARNING RESEARCH (JMLR) AND JMLR'S PUBLISHERS AND DISTRIBUTORS, DISCLAIM ANY AND ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND ANY WARRANTIES OR NON INFRINGEMENT. THE USER ASSUMES ALL LIABILITY AND RESPONSIBILITY FOR USE OF THIS SOURCE CODE, AND NEITHER THE AUTHOR NOR JMLR, NOR JMLR'S PUBLISHERS AND DISTRIBUTORS, WILL BE LIABLE FOR DAMAGES OF ANY KIND RESULTING FROM ITS USE. Without lim- iting the generality of the foregoing, neither the author, nor JMLR, nor JMLR's publishers and distributors, warrant that the Source Code will be error-free, will operate without interruption, or will meet the needs of the user.
// 
// --------------------------------------------------------------------------
// 
// Copyright 2016 Stephen Piccolo
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
import mlflex.evaluation.CustomWekaEvaluation;
import mlflex.helper.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class interfaces directly with the application programming interface of the Weka machine-learning software package. The Weka JAR file is packaged with ML-Flex, so this learner should work out of the box.
 * @author Stephen Piccolo
 */
public class WekaLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        // Create an ARFF file with the training data
        String arffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), trainData, null, true, trainData.GetDataPointNames()).CreateArffFile().GetArffFilePath();

        // Replace token to indicate path to input file
        String command = commandTemplate.replace("{INPUT_TRAINING_FILE}", arffFilePath);
        command = command.replace("{ALGORITHM}", algorithmParameters.get(0));

        // Invoke Weka at the command line
        HashMap<String, String> results = CommandLineClient.RunAnalysis(command);

        // Delete the ARFF file
        FileUtilities.DeleteFile(arffFilePath);

        // Retrieve the output
        String output = CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY);
        ArrayList<String> outputLines = ListUtilities.CreateStringList(output.split("\n"));

        ArrayList<String> dataPointNames = ListUtilities.SortStringList(trainData.GetDataPointNames());

        // Parse through the Weka formatted output data
        for (String line : outputLines)
        {
            if (line.startsWith("Selected attributes:"))
            {
                String rawSelectedAttributes = line.replace("Selected attributes: ", "");
                rawSelectedAttributes = rawSelectedAttributes.substring(0, rawSelectedAttributes.indexOf(":") - 1);
                ArrayList<Integer> selectedAttributeIndices = ListUtilities.CreateIntegerList(ListUtilities.CreateStringList(rawSelectedAttributes.split(",")));
                ArrayList<String> selectedAttributes = ListUtilities.Get(dataPointNames, MathUtilities.Add(selectedAttributeIndices, -1));

                return selectedAttributes;
             }
        }

        throw new Exception("Weka found no selected attributes. Command: " + command + ". Output: " + output);
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection testData, ArrayList<String> features) throws Exception
    {
        Singletons.Log.Debug("Create ARFF file for training data");
        String trainingArffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), trainData, testData, true, features).CreateArffFile().GetArffFilePath();

        Singletons.Log.Debug("Create ARFF file for test data");
        String testArffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), testData, trainData, true, features).CreateArffFile().GetArffFilePath();

        Singletons.Log.Debug("Replace tokens to indicate paths to input files");
        String command = commandTemplate.replace("{INPUT_TRAINING_FILE}", trainingArffFilePath);
        command = command.replace("{INPUT_TEST_FILE}", testArffFilePath);

        Singletons.Log.Debug("Parse the classifier information and paste it together in proper order");
        String classifier = algorithmParameters.get(0);
        String additionalParameters = "";
        if (classifier.contains(" --"))
        {
            additionalParameters = classifier.substring(classifier.indexOf(" --"));
            classifier = classifier.substring(0, classifier.indexOf(" --"));
        }
        command = command.replace("{ALGORITHM}", classifier) + additionalParameters;

        try
        {
        	Singletons.Log.Debug("Invoke Weka at the command line");
            HashMap<String, String> results = CommandLineClient.RunAnalysis(command);

            Singletons.Log.Debug("Delete ARFF files");
            FileUtilities.DeleteFile(trainingArffFilePath);
            FileUtilities.DeleteFile(testArffFilePath);

            Singletons.Log.Debug("Retrieve output");
            String output = CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY);

            ArrayList<String> rawOutputLines = ListUtilities.CreateStringList(output.split("\n"));
            ArrayList<String> outputLines = new ArrayList<String>();

            Singletons.Log.Debug("Build predictions by parsing through output");
            ArrayList<Prediction> predictions = new ArrayList<Prediction>();

            Singletons.Log.Debug("Remove header lines");
            for (String line : rawOutputLines)
                if ((outputLines.size() > 0 || line.contains("inst#     actual  predicted error distribution")) && line.length() > 0)
                    outputLines.add(line);
            outputLines.remove(0);

            Singletons.Log.Debug("Sort the test instance IDs because they will be returned from Weka in sorted order");
            ArrayList<String> testInstanceIDs = ListUtilities.SortStringList(testData.GetIDs());

            Singletons.Log.Debug("Parse through the Weka custom output");
            for (int i=0; i<outputLines.size(); i++)
            {
                ArrayList<String> lineItems = ListUtilities.CreateStringList(outputLines.get(i).trim().split("\\s+"));

                String rawProbabilities = lineItems.get(lineItems.size() - 1);
                ArrayList<String> rawProbabilitiesList = ListUtilities.CreateStringList(rawProbabilities.split(","));

                int predictedClassIndex = -1;
                for (int j=0; j<rawProbabilitiesList.size(); j++)
                    if (rawProbabilitiesList.get(j).startsWith("*"))
                        predictedClassIndex = j;

                rawProbabilitiesList.set(predictedClassIndex, rawProbabilitiesList.get(predictedClassIndex).substring(1));

                ArrayList<Double> probabilities = ListUtilities.CreateDoubleList(rawProbabilitiesList);
                String predictedClass = Singletons.InstanceVault.DependentVariableOptions.get(predictedClassIndex);

                String testInstanceID = testInstanceIDs.get(i);
                predictions.add(new Prediction(testInstanceID, Singletons.InstanceVault.GetDependentVariableValue(testInstanceID), predictedClass, probabilities));
            }

            return new ModelPredictions(output, new Predictions(predictions));
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("An error occurred while attempting to perform training and Action. Below is the output of Weka:");
            throw ex;
        }
    }
    
    /** Creates a custom object that can use the Weka library to calculate many of the performance metrics.
    *
    * @param predictions List of predictions that have been made
    * @return Custom object that uses the Weka library to calculate performance metrics
    * @throws Exception
    */
    public CustomWekaEvaluation GetWekaEvaluation(Predictions predictions) throws Exception
    {
    	Singletons.Log.Debug("Get Weka data instances");
    	Instances wekaInstances = GetEvaluationInstances(predictions);

    	Singletons.Log.Debug("Create custom weka evaluation object");
    	CustomWekaEvaluation evaluation = new CustomWekaEvaluation(wekaInstances);

    	Singletons.Log.Debug("Add instance predictions");
    	ArrayList<String> instanceIDs = new ArrayList<String>();
    	for (Prediction prediction : predictions.GetAll())
    		instanceIDs.add(prediction.InstanceID);
    	
    	for (int i=0; i<instanceIDs.size(); i++)
    	{
    		if (predictions.HasPrediction(instanceIDs.get(0)))
    			evaluation.AddInstancePrediction(wekaInstances.instance(i), predictions.Get(instanceIDs.get(i)));
    	}
       
       return evaluation;
   }

    /** Creates Weka instances from ML-Flex collections.
     *
     * @param dependentVariableInstances ML-Flex collection of dataInstances
     * @return Weka instances
     * @throws Exception
     */
    private static Instances GetEvaluationInstances(Predictions predictions) throws Exception
    {
        FastVector wekaAttributeVector = GetAttributeVector(predictions);

        Instances wekaInstances = new Instances("DataSet", wekaAttributeVector, predictions.Size());
        wekaInstances.setClass((Attribute)wekaAttributeVector.elementAt(1));

        for (Prediction prediction : predictions.GetAll())
            wekaInstances.add(GetInstance(wekaInstances, wekaAttributeVector, prediction));

        return wekaInstances;
    }

    private static Instance GetInstance(Instances wekaInstances, FastVector wekaAttributeVector, Prediction prediction) throws Exception
    {
        Instance wekaInstance = new Instance(wekaAttributeVector.size());
        wekaInstance.setDataset(wekaInstances);

        wekaInstance.setValue((Attribute)wekaAttributeVector.elementAt(0), prediction.Prediction);
        wekaInstance.setValue((Attribute)wekaAttributeVector.elementAt(1), prediction.DependentVariableValue);

        return wekaInstance;
    }

//    private static void SetAttributeValue(Instance wekaInstance, Attribute attribute, String value)
//    {
//        try
//        {
//            if (MiscUtilities.IsMissing(value))
//            {
//                wekaInstance.setMissing(attribute);
//            }
//            else
//            {
//                if (attribute.isNominal())
//                {
//                    wekaInstance.setValue(attribute, value);
//                }
//                else
//                {
//                    wekaInstance.setValue(attribute, Double.parseDouble(value));
//                }
//            }
//        }
//        catch (Exception ex)
//        {
//            Singletons.Log.Debug("Data point name: " + attribute.name());
//            Singletons.Log.Debug("Data point value:");
//            Singletons.Log.Debug(value);
//            Singletons.Log.Debug("Is double: " + DataTypeUtilities.IsDouble(value));
//            Singletons.Log.Debug("Is binary: " + DataTypeUtilities.IsBinary(value));
//            Singletons.Log.Debug("Is integer: " + DataTypeUtilities.IsInteger(value));
//            Singletons.Log.ExceptionFatal(ex);
//        }
//    }

    private static FastVector GetAttributeOptions(ArrayList<String> values, boolean sort)
    {
        ArrayList<String> values2 = new ArrayList<String>(new HashSet<String>(values));

        if (values2.contains(Settings.MISSING_VALUE_STRING))
            values2.remove(Settings.MISSING_VALUE_STRING);

        if (sort)
            values2 = ListUtilities.SortStringList(values2);

        FastVector options = new FastVector();
            for (String value : values2)
                options.addElement(value);

        return options;
    }

    private static FastVector GetAttributeVector(Predictions predictions) throws Exception
    {
        FastVector attVector = new FastVector();

        ArrayList<String> predictionValues = new ArrayList<String>();
        for (Prediction prediction : predictions.GetAll())
        	predictionValues.add(prediction.Prediction);
        
        ArrayList<String> uniquePredictions = ListUtilities.GetUniqueValues(predictionValues);

        if (DataTypeUtilities.HasOnlyBinary(uniquePredictions))
            attVector.addElement(new Attribute("Prediction", GetAttributeOptions(uniquePredictions, false)));
        else
        {
            if (DataTypeUtilities.HasOnlyNumeric(uniquePredictions))
                attVector.addElement(new Attribute("Prediction"));
            else
                attVector.addElement(new Attribute("Prediction", GetAttributeOptions(uniquePredictions, false)));
        }

        attVector.addElement(new Attribute(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName, GetAttributeOptions(Singletons.InstanceVault.DependentVariableOptions, true)));

        return attVector;
    }
}