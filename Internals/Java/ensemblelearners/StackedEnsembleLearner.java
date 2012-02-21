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

package mlflex.ensemblelearners;

import mlflex.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class implements the "stacked combiner" approach ensemblelearners learning. This method builds second-level classification models on the individual predictions that have been made for a given data instance. The classification algorithm used for the second-level predictions can be configured in the experiment files.
 * @author Stephen Piccolo
 */
public class StackedEnsembleLearner extends AbstractEnsembleLearner
{
    private boolean _useProbabilities;

    /** Constructor
     *
     * @param useProbabilities Whether to use the class probabilities as variables in the level 2 classification; if false, the predicted classes are used
     * @throws Exception
     */
    public StackedEnsembleLearner(boolean useProbabilities) throws Exception
    {
        _useProbabilities = useProbabilities;
    }

    @Override
    protected Predictions MakeInstancePredictions(HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        ArrayList<String> dependentVariableClasses = Singletons.InstanceVault.TransformedDependentVariableOptions;

        DataInstanceCollection trainData = new DataInstanceCollection();
        DataInstanceCollection testData = new DataInstanceCollection();

        HashSet<EnsemblePredictionInfo> trainInfos = new HashSet<EnsemblePredictionInfo>();

        for (String instanceID : ensemblePredictionInfoMap.keySet())
            trainInfos.addAll(ensemblePredictionInfoMap.get(instanceID).Infos);

        // Build up the training data set using the values from inner folds
        if (_useProbabilities)
            for (EnsemblePredictionInfo info : trainInfos)
                for (String instanceID : info.InnerPredictionResults.Predictions.GetInstanceIDs())
                {
                    Prediction prediction = info.InnerPredictionResults.Predictions.Get(instanceID);

                    for (int i = 0; i<dependentVariableClasses.size(); i++)
                        trainData.Add(FormatName(info.Description + "_" + dependentVariableClasses.get(i)), prediction.InstanceID, String.valueOf((double) prediction.ClassProbabilities.get(i)));
                }
        else
            for (EnsemblePredictionInfo info : trainInfos)
                for (String instanceID : info.InnerPredictionResults.Predictions.GetInstanceIDs())
                {
                    Prediction prediction = info.InnerPredictionResults.Predictions.Get(instanceID);
                    trainData.Add(FormatName(info.Description), prediction.InstanceID, FormatName(prediction.Prediction));
                }

        // Build up the test data using values from the outer folds
        if (_useProbabilities)
        {
            for (String instanceID : ensemblePredictionInfoMap.keySet())
                for (EnsemblePredictionInfo info : ensemblePredictionInfoMap.get(instanceID).Infos)
                    for (int i = 0; i<dependentVariableClasses.size(); i++)
                        testData.Add(FormatName(info.Description + "_" + dependentVariableClasses.get(i)), instanceID, String.valueOf((double) info.OuterPrediction.ClassProbabilities.get(i)));
        }
        else
        {
            for (String instanceID : ensemblePredictionInfoMap.keySet())
                for (EnsemblePredictionInfo info : ensemblePredictionInfoMap.get(instanceID).Infos)
                    testData.Add(FormatName(info.Description), instanceID, FormatName(info.OuterPrediction.Prediction));
        }

        // Make the predictions using the second-level classifier
        return Singletons.Config.GetStackingClassificationAlgorithm().TrainTest(trainData, testData, Singletons.InstanceVault.TransformedDependentVariableInstances).Predictions;
    }

    private String FormatName(String name)
    {
        return name.replace("-", "_").replace(".", "_");
    }
}