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

import mlflex.evaluation.PredictionResults;

/** This class stores prediction information about individual predictions that can then be used for making ensemble predictions.
 * @author Stephen Piccolo
 */
public class EnsemblePredictionInfo
{
    /** The outer prediction that was made for a particular data/algorithm combination */
    public Prediction OuterPrediction;
    /** A summary of the results obtained in the associated inner cross-validation folds */
    public PredictionResults InnerPredictionResults;
    /** A short description of this object. */
    public String Description;

    /** Constructor
     *
     * @param prediction Prediction information
     * @param innerPredictionResults PredictionResults object that suggests how well the outer prediction will perform based on how well the corresponding inner-fold predictions performed
     * @param description Name of the prediction information
     */
    public EnsemblePredictionInfo(Prediction prediction, PredictionResults innerPredictionResults, String description)
    {
        OuterPrediction = prediction;
        InnerPredictionResults = innerPredictionResults;
        Description = description;
    }

    /** This simple weight value can be used by ensemblelearners methods to weight each prediction. This value is the AUC attained in the inner cross-validation folds.
     *
     * @return Simple weight value
     * @throws Exception
     */
    public double GetWeight() throws Exception
    {
        //return MathUtility.Round(Utilities.RandomNumberGenerator.nextGaussian(), 5);
        return InnerPredictionResults.GetWekaEvaluation().weightedAreaUnderROC();
    }

    @Override
    public boolean equals(Object obj)
    {
        EnsemblePredictionInfo compareObj = (EnsemblePredictionInfo)obj;

        return this.Description.equals(compareObj.Description);
    }

    @Override
    public int hashCode()
    {
        return this.Description.hashCode();
    }
}
