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
import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;

import java.util.ArrayList;
import java.util.Random;

/** This class represents an ensemble/combiner approach that forms an aggregate prediction based on a majority vote on the individual predictions.
 * @author Stephen Piccolo
 */
public class MajorityVoteEnsembleLearner extends AbstractEnsembleLearner
{
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos predictionInfos) throws Exception
    {
        return Vote(instanceID, predictionInfos.GetOuterPredictions());
    }

    private static ModelPrediction Vote(String instanceID, Predictions predictions) throws Exception
    {
        // Make sure we have at least one prediction
        if (predictions.Size() == 0)
            throw new Exception("No votes were cast for " + instanceID + ", so it's impossible to make a majority vote.");

        // If there is only one prediction, use that as the majority
        if (predictions.Size() == 1 || predictions.GetUniquePredictedClasses().size() == 1)
            return new ModelPrediction("", predictions.Get(predictions.GetInstanceIDs().get(0)));

        // Find out how many predictions were made for each class
        ArrayList<Double> numClassPredictions = new ArrayList<Double>();
        for (String x : Singletons.InstanceVault.TransformedDependentVariableOptions)
            numClassPredictions.add((double)predictions.GetNumMatchingPredictedClasses(x));

        // Choose a winner
        String predictedClass = ChoosePredictedClass(instanceID, numClassPredictions);

        // Calculate probabilities based on the counts
        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        for (String x : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            double numPredictions = (double)predictions.GetNumMatchingPredictedClasses(x);
            classProbabilities.add(numPredictions / (double)predictions.Size());
        }

        Prediction prediction = new Prediction(instanceID, Singletons.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);
        
        return new ModelPrediction(GetDescription(predictedClass, classProbabilities), prediction);
    }

    /** When majority voting is performed, this method provides a text description of how the majority voting was calculated.
     *
     * @param predictedClass The class that was predicted by majority vote
     * @param classProbabilities The probabilities of each class
     * @return Text description
     * @throws Exception
     */
    public static String GetDescription(String predictedClass, ArrayList<Double> classProbabilities) throws Exception
    {
        ArrayList<String> classes = Singletons.InstanceVault.TransformedDependentVariableOptions;

        String description = "";
        for (int i=0; i<classes.size(); i++)
            description += classes.get(i) + " probability = " + classProbabilities.get(i) + ", ";
        description += "Predicted class = " + predictedClass;

        return description;
    }

    /** When majority voting is used, it produces a summary value indicating the votes that are assigned to each class. This method determines which class has the most votes.
     *
     * @param instanceID Instance ID
     * @param dependentVariableSummaryValues Numeric values corresponding to number of votes per class
     * @return Predicted class
     * @throws Exception
     */
    public static String ChoosePredictedClass(String instanceID, ArrayList<Double> dependentVariableSummaryValues) throws Exception
    {
        ArrayList<String> classes = Singletons.InstanceVault.TransformedDependentVariableOptions;

        ArrayList<Integer> indicesOfMaxValues = ListUtilities.GetIndices(dependentVariableSummaryValues, MathUtilities.Max(dependentVariableSummaryValues));

        String predictedClass;

        if (indicesOfMaxValues.size() == 1)
            predictedClass = classes.get(indicesOfMaxValues.get(0));
        else
        {
            long randomSeed = Singletons.RandomSeed * (long)instanceID.hashCode();
            predictedClass = ListUtilities.PickRandomString(ListUtilities.Subset(classes, indicesOfMaxValues), new Random(randomSeed));
        }

        return predictedClass;
    }
}