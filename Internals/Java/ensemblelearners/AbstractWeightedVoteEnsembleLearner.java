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
import mlflex.core.EnsemblePredictionInfo;
import mlflex.core.EnsemblePredictionInfos;
import mlflex.helper.MathUtilities;

import java.util.ArrayList;

/** Several of the ensemblelearners/combiner approaches in ML-Flex have a common characteristic: they assign weights to individual predictions before making the combined prediction. This class abstracts some of that common functionality.
 * @author Stephen Piccolo
 */
public abstract class AbstractWeightedVoteEnsembleLearner extends AbstractEnsembleLearner
{
    /** This method provides a way for inheriting classes to indicate what weight should be assigned to a given prediction for a given data instance.
     * @param info Prediction info
     * @return Weight assigned to the prediction
     * @throws Exception
     */
    protected abstract double GetWeight(EnsemblePredictionInfo info) throws Exception;

    /** This method makes a ensemblelearners/combined prediction for all weight-based combiner classes
     * @param instanceID Data instance ID
     * @param combinedPredictionInfos Prediction info
     * @return Combined prediction
     * @throws Exception
     */
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        ArrayList<String> classes = Singletons.InstanceVault.TransformedDependentVariableOptions;

        // Initialize the class weights
        ArrayList<Double> classWeights = new ArrayList<Double>();
        for (String x : classes)
            classWeights.add(0.0);

        // Update the class weights based on the inner predictions
        for (EnsemblePredictionInfo info : combinedPredictionInfos.Infos)
            classWeights.set(classes.indexOf(info.OuterPrediction.Prediction), GetWeight(info));

        // Pick a winner based on the class weights
        String predictedClass = MajorityVoteEnsembleLearner.ChoosePredictedClass(instanceID, classWeights);

        // Calculate probabilities for each class
        double totalWeight = MathUtilities.Sum(classWeights);
        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        for (int i=0; i<classWeights.size(); i++)
            classProbabilities.add(classWeights.get(i) / totalWeight);

        Prediction prediction = new Prediction(instanceID, Singletons.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);

        return new ModelPrediction(MajorityVoteEnsembleLearner.GetDescription(predictedClass, classProbabilities), prediction);
    }
}
