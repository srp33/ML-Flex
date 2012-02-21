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

package mlflex.core;

import mlflex.helper.ListUtilities;

import java.util.ArrayList;

/** This class acts as a wrapper for performing classification tasks. It interprets parameters for executing these tasks, based on what has been configured in ML-Flex's configuration files.
 * @author Stephen Piccolo
 */
public class ClassificationAlgorithm
{
    public String Key;
    public String LearnerKey;
    public ArrayList<String> AlgorithmParameters;

    /** Constructor
     *
     * @param key Unique name to reference the classification algorithm
     * @param learnerKey Key that corresponds with a configured learner
     * @param algorithmParameters List of parameters that are passed to the algorithm
     * @throws Exception
     */
    public ClassificationAlgorithm(String key, String learnerKey, ArrayList<String> algorithmParameters) throws Exception
    {
        Key = key;
        LearnerKey = learnerKey;
        AlgorithmParameters = algorithmParameters;
    }

    /** This is a pass-through method to perform training and testing. It throws a detailed exception if it cannot be performed.
     *
     * @param trainData Training data instances
     * @param testData Testing data instances
     * @param dependentVariableInstances Dependent-variable instances
     * @return Predictions and model information
     * @throws Exception
     */
    public ModelPredictions TrainTest(DataInstanceCollection trainData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        if (trainData.Size() == 0 || testData.Size() == 0)
            throw new Exception("No predictions can be made because the training and/or test set have no data");

        // For any data point that is in the training set but not the test set, add it to the test set and specify that the values are missing
        for (String dataPoint : ListUtilities.RemoveAll(trainData.GetDataPointNames(), testData.GetDataPointNames()))
            for (String testInstanceID : testData.GetIDs())
                testData.Add(dataPoint, testInstanceID, Settings.MISSING_VALUE_STRING);

        // For any data point that is in the test set but not the training set, add it to the training set and specify that the values are missing
        for (String dataPoint : ListUtilities.RemoveAll(testData.GetDataPointNames(), trainData.GetDataPointNames()))
            for (String trainInstanceID : trainData.GetIDs())
                trainData.Add(dataPoint, trainInstanceID, Settings.MISSING_VALUE_STRING);

        // Do a sanity check to make sure that no instances overlap between the training and test sets
        if (ListUtilities.Intersect(trainData.GetIDs(), testData.GetIDs()).size() > 0)
        {
            String errorMessage = "The training and test sets overlap. ";
            errorMessage += "Algorithm: " + Key + ". ";
            errorMessage += "Train IDs: " + ListUtilities.Join(trainData.GetIDs(), ", ");
            errorMessage += "Test IDs: " + ListUtilities.Join(testData.GetIDs(), ", ") + ".";

            throw new Exception(errorMessage);
        }

        if (trainData.GetNumDataPoints() == 0)
            throw new Exception("The training data had no data points");

        try
        {
            // Execute the learning task
            LearnerConfig learnerConfig = Settings.LearnerConfigMap.get(LearnerKey);
            String commandTemplate = learnerConfig.CommandTemplate.replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR);

            return learnerConfig.MachineLearner.TrainTest(commandTemplate, AlgorithmParameters, trainData, testData);
        }
        catch (Exception ex)
        {
            Singletons.Log.Exception(ex);

            String errorMessage = "An exception occurred while training and testing. ";
            errorMessage += "Algorithm: " + Key + ". ";
            errorMessage += "Training data (first five instances):\n" + trainData.toShortString() + "\n";
            errorMessage += "Test data (first five instances):\n" + testData.toShortString() + "\n";
            errorMessage += "Dependent variable data (first five instances):\n" + dependentVariableInstances.toShortString() + "\n";
            throw new Exception(errorMessage);
        }
    }

    @Override
    public String toString()
    {
        return Key;
    }
}