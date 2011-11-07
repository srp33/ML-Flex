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
//
///** This weight-based ensemblelearners-learning approaches uses the "squared weight" assigned to each individual prediction. The squared weights are assigned based on AUC values attained within inner cross-validation folds. The AUC values are squared in an attempt to put even higher emphasis on individual predictions that are expected to perform well based on the performance within the inner cross-validation folds.
// * @author Stephen Piccolo
// */
//public class SquaredWeightedVoteEnsembleLearner extends AbstractWeightedVoteEnsembleLearner
//{
//    @Override
//    protected double GetWeight(EnsemblePredictionInfo info) throws Exception
//    {
//        return info.GetSquaredWeight();
//    }
//}