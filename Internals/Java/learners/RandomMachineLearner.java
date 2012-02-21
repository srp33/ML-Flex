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
import mlflex.helper.ListUtilities;

import java.util.ArrayList;
import java.util.Random;

/** This class can be used to randomly select features or to randomly assign instances to a given class. This can be used for validation purposes.

 @author Stephen Piccolo
 */
public class RandomMachineLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception
    {
        GenerateRandomSeed(trainData);

        ArrayList<String> features = ListUtilities.Shuffle(trainData.GetDataPointNames(), new Random(GenerateRandomSeed(trainData)));

        if (algorithmParameters.size() > 0)
            features = ListUtilities.Subset(features, 0, Integer.parseInt(algorithmParameters.get(0))); // You can specify how many features to select randomly
        
        return features;
    }

    @Override
    public ModelPredictions TrainTest(String commandTemplate, ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        DataInstanceCollection testDependentVariableInstances = Singletons.InstanceVault.TransformedDependentVariableInstances.Get(testData.GetIDs());
        String dependentVariableName = Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName;

        ArrayList<String> randomTestDataDependentVariableValues = ListUtilities.Shuffle(testDependentVariableInstances.GetDataPointValues(dependentVariableName).GetAllValues(), new Random(GenerateRandomSeed(trainingData)));

        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (int i=0; i<testData.Size(); i++)
        {
            String instanceID = testData.Get(i).GetID();
            String predictedClass = randomTestDataDependentVariableValues.get(i);
            String actualClass = testDependentVariableInstances.Get(instanceID).GetDataPointValue(dependentVariableName);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (String dependentVariableValue : Singletons.InstanceVault.TransformedDependentVariableOptions)
            {
                if (predictedClass.equals(dependentVariableValue))
                    classProbabilities.add(1.0);
                else
                    classProbabilities.add(0.0);
            }

            predictions.add(new Prediction(instanceID, actualClass, predictedClass, classProbabilities));
        }

        return new ModelPredictions("", new Predictions(predictions));
    }

    private long GenerateRandomSeed(DataInstanceCollection dataInstances)
    {
        long randomSeed = 0;
        for (String instanceID : dataInstances.GetIDs())
            randomSeed += (long)instanceID.hashCode();
        randomSeed += Singletons.RandomSeed;

        return randomSeed;
    }
}
