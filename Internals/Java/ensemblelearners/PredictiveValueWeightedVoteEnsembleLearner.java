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

//package mlflex.ensemblelearners;
//
//import mlflex.core.EnsemblePredictionInfo;
//import mlflex.helper.MiscUtilities;
//
///** The ensemblelearners/combiner approach represented in this class uses the "predictive value" for the specified dependent-variable value to determine weights. The predictive value is the proportion of instances that were predicted to be the specified class that were predicted correctly.
// * @author Stephen Piccolo
// */
//public class PredictiveValueWeightedVoteEnsembleLearner extends AbstractWeightedVoteEnsembleLearner
//{
//    private String _dependentVariableClass;
//
//    /** Pass-through constructor
//     *
//     * @param dependentVariableClass Dependent-variable value for which this combiner should be used
//     * @throws Exception
//     */
//    public PredictiveValueWeightedVoteEnsembleLearner(String dependentVariableClass) throws Exception
//    {
//        _dependentVariableClass = dependentVariableClass;
//    }
//
//    @Override
//    protected double GetWeight(EnsemblePredictionInfo info) throws Exception
//    {
//        return info.InnerPredictionResults.GetProportionPredictedAsDependentVariableClassCorrectly(_dependentVariableClass);
//    }
//
//    @Override
//    public String GetDescription()
//    {
//        return MiscUtilities.BuildDescription(GetDescription(), _dependentVariableClass);
//    }
//}