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

package mlflex.learners;

import mlflex.core.DataInstanceCollection;
import mlflex.core.ModelPredictions;

import java.util.ArrayList;

/** This class provides a template for classes that perform general machine-learning tasks. It can be overridden with custom classes that implement these tasks for new third-party packages or for custom implementation.
 * @author Stephen Piccolo
 */
public abstract class AbstractMachineLearner
{
    /** This method is used by custom machine learner classes either 1) to select features that the algorithm determines to be most relevant to the dependent variable, or 2) to rank all features according to their relevance to the dependent variable. If feature selection is used, the features should still be ranked, if possible.
     *
     *
     *
     * @param commandTemplate
     * @param algorithmParameters General parameter values that are used by the machine learner to execute. These are usually stored in the FeatureSelectionParameters.txt file in the Config directory.
     * @param trainData Training data instances
     * @return A list of data point names, ranked according to their perceived relevance to the dependent variable
     * @throws Exception
     */
    public abstract ArrayList<String> SelectOrRankFeatures(String commandTemplate, ArrayList<String> algorithmParameters, DataInstanceCollection trainData) throws Exception;

    /** This method is used by custom machine learner classes to perform classification.
     *
     *
     *
     *
     *
     *
     * @param commandTemplate
     * @param classificationParameters General parameter values that are used by the machine learner to perform classification. These parameters are usually stored in the ClassificationParameters.txt file in the Config directory.
     * @param trainingData Training data instances
     * @param testData Test data instances
     * @return Predictions for each test data instance
     * @throws Exception
     */
    public abstract ModelPredictions TrainTest(String commandTemplate, ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception;
}
