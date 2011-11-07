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

/** This class represents an ensemblelearners/combiner approach that computes the combined prediction according to the class probabilities that were assigned for each individual prediction. This particular approach uses the mean across the probabilities, weighted by the AUC attained in the inner cross-validation folds, to compute the combined prediction.
 * @author Stephen Piccolo
 */
public class WeightedMeanProbabilityEnsembleLearner extends MeanProbabilityEnsembleLearner
{
    /** Pass-through constructor
     */
    public WeightedMeanProbabilityEnsembleLearner()
    {
        super(true);
    }
}