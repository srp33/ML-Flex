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
        String arffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), trainData, null, true).CreateArffFile().GetArffFilePath();

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
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection testData) throws Exception
    {
        // Create ARFF file for training data
        String trainingArffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), trainData, testData, true).CreateArffFile().GetArffFilePath();

        // Create ARFF file for test data
        String testArffFilePath = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), testData, trainData, true).CreateArffFile().GetArffFilePath();

        // Replace tokens to indicate paths to input files
        String command = commandTemplate.replace("{INPUT_TRAINING_FILE}", trainingArffFilePath);
        command = command.replace("{INPUT_TEST_FILE}", testArffFilePath);

        // Parse the classifier information and paste it together in proper order
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
            // Invoke Weka at the command line
            HashMap<String, String> results = CommandLineClient.RunAnalysis(command);

            // Delete ARFF files
            FileUtilities.DeleteFile(trainingArffFilePath);
            FileUtilities.DeleteFile(testArffFilePath);

            // Retrieve output
            String output = CommandLineClient.GetCommandResult(results, CommandLineClient.STANDARD_OUT_KEY);

            ArrayList<String> rawOutputLines = ListUtilities.CreateStringList(output.split("\n"));
            ArrayList<String> outputLines = new ArrayList<String>();

            // Build predictions by parsing through output
            ArrayList<Prediction> predictions = new ArrayList<Prediction>();

            // Remove header lines
            for (String line : rawOutputLines)
                if ((outputLines.size() > 0 || line.contains("inst#     actual  predicted error distribution")) && line.length() > 0)
                    outputLines.add(line);
            outputLines.remove(0);

            // Sort the test instance IDs because they will be returned from Weka in sorted order
            ArrayList<String> testInstanceIDs = ListUtilities.SortStringList(testData.GetIDs());

            // Parse through the Weka custom output
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
                String predictedClass = Singletons.InstanceVault.TransformedDependentVariableOptions.get(predictedClassIndex);

                String testInstanceID = testInstanceIDs.get(i);
                predictions.add(new Prediction(testInstanceID, Singletons.InstanceVault.GetTransformedDependentVariableValue(testInstanceID), predictedClass, probabilities));
            }

            return new ModelPredictions(output, new Predictions(predictions));
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("An error occurred while attempting to perform training and testing. Below is the output of Weka:");
            throw ex;
        }
    }

    /** Creates Weka instances from ML-Flex collections.
     *
     * @param dataInstances ML-Flex collection of dataInstances
     * @return Weka instances
     * @throws Exception
     */
    private static Instances GetInstances(DataInstanceCollection dataInstances) throws Exception
    {
        FastVector wekaAttributeVector = GetAttributeVector(dataInstances);

        Instances wekaInstances = new Instances("DataSet", wekaAttributeVector, dataInstances.Size());
        wekaInstances.setClass((Attribute)wekaAttributeVector.elementAt(wekaAttributeVector.size()-1));

        for (DataValues dataInstance : dataInstances)
            wekaInstances.add(GetInstance(wekaInstances, dataInstance, wekaAttributeVector));

        return wekaInstances;
    }

    private static Instance GetInstance(Instances wekaInstances, DataValues dataInstance, FastVector wekaAttributeVector) throws Exception
    {
        Instance wekaInstance = new Instance(wekaAttributeVector.size());
        wekaInstance.setDataset(wekaInstances);

        for (int i=0; i< wekaAttributeVector.size()-1; i++)
        {
            Attribute attribute = (Attribute) wekaAttributeVector.elementAt(i);
            String dataPointValue = dataInstance.GetDataPointValue(attribute.name());

            SetAttributeValue(wekaInstance, attribute, dataPointValue);
        }

        SetAttributeValue(wekaInstance, (Attribute) wekaAttributeVector.elementAt(wekaAttributeVector.size() - 1), Singletons.InstanceVault.TransformedDependentVariableInstances.Get(dataInstance.GetID()).GetDataPointValue(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName));

        return wekaInstance;
    }

    private static void SetAttributeValue(Instance wekaInstance, Attribute attribute, String value)
    {
        try
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
            {
                wekaInstance.setMissing(attribute);
            }
            else
            {
                if (attribute.isNominal())
                {
                    wekaInstance.setValue(attribute, value);
                }
                else
                {
                    wekaInstance.setValue(attribute, Double.parseDouble(value));
                }
            }
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("Data point name: " + attribute.name());
            Singletons.Log.Debug("Data point value:");
            Singletons.Log.Debug(value);
            Singletons.Log.Debug("Is double: " + DataTypeUtilities.IsDouble(value));
            Singletons.Log.Debug("Is binary: " + DataTypeUtilities.IsBinary(value));
            Singletons.Log.Debug("Is integer: " + DataTypeUtilities.IsInteger(value));
            Singletons.Log.ExceptionFatal(ex);
        }
    }

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

    private static FastVector GetAttributeVector(DataInstanceCollection dataInstances) throws Exception
    {
        FastVector attVector = new FastVector();

        for (String dataPointName : ListUtilities.SortStringList(dataInstances.GetDataPointNames()))
        {
            ArrayList<String> uniqueValuesList = dataInstances.GetUniqueValues(dataPointName);

            if (DataTypeUtilities.HasOnlyBinary(uniqueValuesList))
                attVector.addElement(new Attribute(dataPointName, GetAttributeOptions(uniqueValuesList, false)));
            else
            {
                if (DataTypeUtilities.HasOnlyNumeric(uniqueValuesList))
                    attVector.addElement(new Attribute(dataPointName));
                else
                    attVector.addElement(new Attribute(dataPointName, GetAttributeOptions(uniqueValuesList, false)));
            }
        }

        attVector.addElement(new Attribute(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName, GetAttributeOptions(Singletons.InstanceVault.TransformedDependentVariableOptions, true)));

        return attVector;
    }

    private void SaveWekaInstances(Instances instances, String filePath) throws Exception
    {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(new java.io.File(filePath));
        saver.writeBatch();

    }

    /** Creates a custom object that can use the Weka library to calculate many of the performance metrics.
     *
     * @param predictions List of predictions that have been made
     * @return Custom object that uses the Weka library to calculate performance metrics
     * @throws Exception
     */
    public CustomWekaEvaluation GetWekaEvaluation(Predictions predictions) throws Exception
    {
        DataInstanceCollection dataInstances = Singletons.InstanceVault.TransformedDependentVariableInstances.Get(predictions.GetInstanceIDs()).Clone();
        dataInstances.RemoveDataPointName(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName);

        Instances wekaInstances = GetInstances(dataInstances);

        CustomWekaEvaluation evaluation = new CustomWekaEvaluation(wekaInstances);

        for (int i=0; i<dataInstances.Size(); i++)
        {
            DataValues dataInstance = dataInstances.Get(i);

            if (predictions.HasPrediction(dataInstance.GetID()))
                evaluation.AddInstancePrediction(wekaInstances.instance(i), predictions.Get(dataInstance.GetID()));
        }

        return evaluation;
    }
}