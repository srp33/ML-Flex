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

//package mlflex;
//
//import weka.attributeSelection.ASEvaluation;
//import weka.attributeSelection.ASSearch;
//import weka.attributeSelection.AttributeSelection;
//import weka.attributeSelection.Ranker;
//import weka.classifiers.Classifier;
//import weka.classifiers.evaluation.NominalPrediction;
//import weka.classifiers.evaluation.ThresholdCurve;
//import weka.core.Attribute;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.converters.ArffSaver;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//
///** This class interfaces directly with the application programming interface of the Weka machine-learning software package. The Weka JAR file is packaged with ML-Flex, so this learner should work out of the box. However, Java can get bogged down when you use this on large data sets, so it is often better to use the WekaLearner.
//* @author Stephen Piccolo
//*/
////public class WekaInMemoryLearner extends AbstractMachineLearner
//public class WekaInMemoryLearner
//{
//    @Override
//    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
//    {
//        ArrayList<String> dataPointNames = Lists.SortStringList(trainData.GetDataPointNames());
//
//        FastVector attVector = GetAttributeVector(dependentVariableInstances, dataPointNames, trainData);
//        Instances instances = GetInstances(dependentVariableInstances, attVector, trainData);
//
//        AttributeSelection attsel = new AttributeSelection();
//        ASEvaluation eval = GetAttributeEvaluator(algorithmParameters);
//        ASSearch search = GetSearchMethod(algorithmParameters);
//        attsel.setEvaluator(eval);
//        attsel.setSearch(search);
//
//        boolean isRanker = algorithmParameters.get(2).equals(Ranker.class.getName());
//
//        if (isRanker)
//            attsel.setRanking(true);
//
//        attsel.SelectAttributes(instances);
//
//        ArrayList<String> features = new ArrayList<String>();
//
//        if (isRanker)
//        {
//            for (double[] rank : attsel.rankedAttributes())
//                features.add(instances.attribute((int)rank[0]).name());
//        }
//        else
//        {
//            for (int i : attsel.selectedAttributes())
//                features.add(instances.attribute(i).name());
//        }
//
//        instances = null;
//
//        return features;
//    }
//
//    @Override
//    protected ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainData, DataInstanceCollection testData) throws Exception
//    {
//        ArrayList<String> dataPointNames = Lists.SortStringList(trainData.GetDataPointNames());
//        FastVector attVector = GetAttributeVector(dependentVariableInstances, dataPointNames, trainData, testData);
//
//        Instances wekaTrainingInstances = GetInstances(dependentVariableInstances, attVector, trainData);
//        Instances wekaTestInstances = GetInstances(dependentVariableInstances, attVector, testData);
//
//        ArrayList<String> dependentVariableClasses = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
//
//        Classifier classifier = GetClassifier(classificationParameters);
//        classifier.buildClassifier(wekaTrainingInstances);
//
//        Predictions predictions = new Predictions();
//
//        for (DataValues testInstance : testData)
//        {
//            String dependentVariableValue = dependentVariableInstances.Get(testInstance.GetID()).GetDataPointValue(0);
//
//            // This is the default before the prediction is made
//            Prediction prediction = new Prediction(testInstance.GetID(), dependentVariableValue, Lists.PickRandomValue(dependentVariableClasses), Lists.CreateDoubleList(0.5, dependentVariableClasses.size()));
//
//            if (!testInstance.HasOnlyMissingValues())
//            {
//                Instance wekaTestInstance = GetInstance(wekaTestInstances, attVector, testInstance, null);
//
//                double clsLabel = classifier.classifyInstance(wekaTestInstance);
//                String predictedClass = wekaTestInstance.classAttribute().value((int)clsLabel);
//
//                double[] probabilities = classifier.distributionForInstance(wekaTestInstance);
//                ArrayList<Double> classProbabilities = Lists.CreateDoubleList(probabilities);
//
//                prediction = new Prediction(testInstance.GetID(), dependentVariableValue, predictedClass, classProbabilities);
//            }
//
//            predictions.Add(prediction);
//        }
//
//        classifier = null;
//
//        return new ModelPredictions("", predictions);
//    }
//
//    /** Creates Weka instances from ML-Flex collections.
//     *
//     *
//     * @param dependentVariableInstances Dependent variable data instances
//     * @param attVector Vector of Weka attributes
//     * @param instances ML-Flex collection of instances
//     * @return Weka instances
//     * @throws Exception
//     */
//    public static Instances GetInstances(DataInstanceCollection dependentVariableInstances, FastVector attVector, DataInstanceCollection instances) throws Exception
//    {
//        Instances wekaInstances = new Instances("DataSet", attVector, instances.Size());
//
//        if (dependentVariableInstances != null)
//            wekaInstances.setClass((Attribute)attVector.elementAt(attVector.size()-1));
//
//        for (DataValues instance : instances)
//            wekaInstances.add(GetInstance(wekaInstances,attVector, instance, dependentVariableInstances));
//
//        return wekaInstances;
//    }
//
//    private static Instance GetInstance(Instances wekaInstances, FastVector attVector, DataValues dataInstance, DataInstanceCollection dependentVariableInstances) throws Exception
//    {
//        Instance wekaInstance = new Instance(attVector.size());
//        wekaInstance.setDataset(wekaInstances);
//
//        for (int i=0; i<attVector.size()-1; i++)
//        {
//            Attribute attribute = (Attribute)attVector.elementAt(i);
//            String dataPointValue = dataInstance.GetDataPointValue(attribute.name());
//
//            SetAttributeValue(wekaInstance, attribute, dataPointValue);
//        }
//
//        if (dependentVariableInstances != null)
//            SetAttributeValue(wekaInstance, (Attribute)attVector.elementAt(attVector.size() - 1), dependentVariableInstances.Get(dataInstance.GetID()).GetDataPointValue(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName()));
//
//        return wekaInstance;
//    }
//
//    private static void SetAttributeValue(Instance wekaInstance, Attribute attribute, String value)
//    {
//        try
//        {
//            if (value.equals(Settings.MISSING_VALUE_STRING))
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
//            Utilities.Log.Debug("Data point name: " + attribute.name());
//            Utilities.Log.Debug("Data point value:");
//            Utilities.Log.Debug(value);
//            Utilities.Log.Debug("Is double: " + DataTypes.IsDouble(value));
//            Utilities.Log.Debug("Is binary: " + DataTypes.IsBinary(value));
//            Utilities.Log.Debug("Is integer: " + DataTypes.IsInteger(value));
//            Utilities.Log.ExceptionFatal(ex);
//        }
//    }
//
//    private static FastVector GetAttributeOptions(ArrayList<String> values)
//    {
//        FastVector options = new FastVector();
//            for (String value : new HashSet<String>(values))
//                if (!value.equals(Settings.MISSING_VALUE_STRING))
//                    options.addElement(value);
//
//        return options;
//    }
//
//    private static Classifier GetClassifier(ArrayList<String> algorithmParameters) throws Exception
//    {
//        return Classifier.forName(algorithmParameters.get(0), algorithmParameters.get(1).split(" "));
//    }
//
//    private static ASEvaluation GetAttributeEvaluator(ArrayList<String> algorithmParameters) throws Exception
//    {
//        return ASEvaluation.forName(algorithmParameters.get(0), algorithmParameters.get(1).split(" "));
//    }
//
//    private ASSearch GetSearchMethod(ArrayList<String> algorithmParameters) throws Exception
//    {
//        return ASSearch.forName(algorithmParameters.get(2), algorithmParameters.get(3).split(" "));
//    }
//
//    private FastVector GetAttributeVector(DataInstanceCollection dependentVariableInstances, ArrayList<String> dataPointNames, DataInstanceCollection ... collections) throws Exception
//    {
//        FastVector attVector = new FastVector();
//
//        for (String dataPointName : dataPointNames)
//        {
//            HashSet<String> uniqueValues = new HashSet<String>();
//
//            for (DataInstanceCollection collection : collections)
//                uniqueValues.addAll(collection.GetDataPointValues(dataPointName).GetAllValues());
//
//            ArrayList<String> uniqueValuesList = new ArrayList<String>(uniqueValues);
//
//            if (DataTypes.HasOnlyBinary(uniqueValuesList))
//                attVector.addElement(new Attribute(dataPointName, GetAttributeOptions(uniqueValuesList)));
//            else
//            {
//                if (DataTypes.HasOnlyNumeric(uniqueValuesList))
//                    attVector.addElement(new Attribute(dataPointName));
//                else
//                    attVector.addElement(new Attribute(dataPointName, GetAttributeOptions(uniqueValuesList)));
//            }
//        }
//
//        if (dependentVariableInstances != null)
//            attVector.addElement(new Attribute(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName(), GetAttributeOptions(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())));
//
//        return attVector;
//    }
//
//    private void SaveWekaInstances(Instances instances, String filePath) throws Exception
//    {
//        ArffSaver saver = new ArffSaver();
//        saver.setInstances(instances);
//        saver.setFile(new java.io.File(filePath));
//        saver.writeBatch();
//
//    }
//}