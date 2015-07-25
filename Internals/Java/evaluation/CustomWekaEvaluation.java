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

package mlflex.evaluation;

import mlflex.core.Prediction;
import mlflex.helper.ListUtilities;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/** This custom class helps in calculating some of the performance metrics. It takes advantage of the code in Weka that calculates these metrics. A custom class was necessary because one of the methods on the Evaluation class in weka is protected. */
public class CustomWekaEvaluation extends Evaluation
{
    /** Constructor that accepts some data instances.
     *
     * @param data Data instances that will be used for the calculations
     * @throws Exception
     */
    public CustomWekaEvaluation(Instances data) throws Exception
    {
        super(data);
    }

    /** Add a combination of a data instance and a prediction that was made for that instance.
     *
     * @param wekaInstance A Weka data instance
     * @param prediction A prediction object for the instance
     * @throws Exception
     */
    public void AddInstancePrediction(Instance wekaInstance, Prediction prediction) throws Exception
    {
        super.evaluateModelOnceAndRecordPrediction(ListUtilities.ConvertToDoubleArray(prediction.ClassProbabilities), wekaInstance);
    }
}
