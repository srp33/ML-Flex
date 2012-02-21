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
import mlflex.helper.MathUtilities;

import java.util.ArrayList;

/** This class represents an ensemblelearners/combiner approach that computes the combined prediction according to the class probabilities that were assigned for each individual prediction. This particular approach uses the mean across the probabilities to compute the combined prediction.
 * @author Stephen Piccolo
 */
public class MeanProbabilityEnsembleLearner extends AbstractEnsembleLearner
{
    private boolean _assignWeights;

    /** Constuctor
     */
    public MeanProbabilityEnsembleLearner()
    {
        this(false);
    }

    /** Pass-through constructor
     *
     * @param assignWeights Indicates whether weights should be assigned to the individual probabilities, based on inner cross-validation performance.
     */
    protected MeanProbabilityEnsembleLearner(boolean assignWeights)
    {
        _assignWeights = assignWeights;
    }

    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        ArrayList<String> classes = Singletons.InstanceVault.TransformedDependentVariableOptions;

        // Set default values for the combined probabilities
        for (String x : classes)
            classProbabilities.add(0.0);

        // Calculate the mean probabilities for each class using the inner AUC values
        for (String x : classes)
            for (EnsemblePredictionInfo info : combinedPredictionInfos.Infos)
                for (int i=0; i<info.OuterPrediction.ClassProbabilities.size(); i++)
                {
                    Double probability = info.OuterPrediction.ClassProbabilities.get(i);

                    if (_assignWeights)
                        probability *= info.GetWeight();

                    classProbabilities.set(i, classProbabilities.get(i) + probability); // Sum has same effect as mean
                }

        // Pick a winning class
        String predictedClass = MajorityVoteEnsembleLearner.ChoosePredictedClass(instanceID, classProbabilities);

        // Calculate a probability for each class
        double totalProbability = MathUtilities.Sum(classProbabilities);
        for (int i=0; i<classProbabilities.size(); i++)
            classProbabilities.set(i, classProbabilities.get(i) / totalProbability);

        // Construct the ensemble prediction
        Prediction prediction = new Prediction(instanceID, Singletons.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);

        return new ModelPrediction(MajorityVoteEnsembleLearner.GetDescription(predictedClass, classProbabilities), prediction);
    }
}