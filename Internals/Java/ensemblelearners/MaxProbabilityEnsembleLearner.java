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

import java.util.ArrayList;
import java.util.Random;

/** This class represents an ensemblelearners/combiner approach that computes the combined prediction according to the class probabilities that were assigned for each individual prediction. This particular approach uses the maximum across the probabilities to compute the combined prediction.
 * @author Stephen Piccolo
 */
public class MaxProbabilityEnsembleLearner extends AbstractEnsembleLearner
{
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        // Determine the maximum probability value across all classes
        double maxProbability = GetMaxOverallProbability(combinedPredictionInfos.GetOuterPredictions());

        // Determine which prediction(s) had the max probability value
        ArrayList<Prediction> maxPredictions = GetMaxPredictions(combinedPredictionInfos.GetOuterPredictions(), maxProbability);

        // If more than one prediction had the max probability, then randomly pick one as the predicted class
        long randomSeed = Singletons.RandomSeed * (long)instanceID.hashCode();
        Prediction maxPrediction = (Prediction) ListUtilities.PickRandomObject(maxPredictions, new Random(randomSeed));
        String description = "";

        return new ModelPrediction(description, maxPrediction);
    }

    private double GetMaxOverallProbability(Predictions predictions) throws Exception
    {
        double highestProbability = -0.01;

        for (String instanceID : predictions.GetInstanceIDs())
            for (double classProbability : predictions.Get(instanceID).ClassProbabilities)
                if (classProbability > highestProbability)
                    highestProbability = classProbability;

        return highestProbability;
    }

    private ArrayList<Prediction> GetMaxPredictions(Predictions predictions, double highestProbability) throws Exception
    {
        ArrayList<Prediction> maxPredictions = new ArrayList<Prediction>();

        for (String instanceID : predictions.GetInstanceIDs())
        {
            Prediction prediction = predictions.Get(instanceID);

            for (int i=0; i< prediction.ClassProbabilities.size(); i++)
            {
                double classProbability = prediction.ClassProbabilities.get(i);
                if (classProbability == highestProbability)
                    maxPredictions.add(prediction);

            }
        }

        return maxPredictions;
    }
}
