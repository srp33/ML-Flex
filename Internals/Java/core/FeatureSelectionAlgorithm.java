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

import java.util.ArrayList;

/** This class acts as a wrapper for performing feature-selection tasks. It interprets parameters for executing these tasks, based on what has been configured in ML-Flex's configuration files.
 * @author Stephen Piccolo
 */
public class FeatureSelectionAlgorithm
{
    public String Key;
    public String LearnerKey;
    public ArrayList<String> AlgorithmParameters;

    /** Constructor
     *
     * @param key Unique name to reference the algorithm
     * @param learnerKey Unique name of the ML-Flex Java class that extends AbstractMachineLearner and contains the logic for interfacing with the external library (where applicable)
     * @param algorithmParameters List of parameters that are passed to the algorithm
     * @throws Exception
     */
    public FeatureSelectionAlgorithm(String key, String learnerKey, ArrayList<String> algorithmParameters) throws Exception
    {
        Key = key;
        LearnerKey = learnerKey;
        AlgorithmParameters = algorithmParameters;
    }

    /** This is a pass-through method to rank/select features. It throws a detailed exception if it cannot be performed.
     *
     *
     * @param trainData Training data instances
     * @return List of ranked features (in order)
     * @throws Exception
     */
    public ArrayList<String> SelectFeatures(DataInstanceCollection trainData) throws Exception
    {
        // Verify that there is data to work with
        if (trainData.Size() == 0)
            throw new Exception("The training data had no data instances.");
        if (trainData.GetNumDataPoints() == 0)
            throw new Exception("The training data had no data points");

        try
        {
            // Perform the actual feature ranking
            LearnerConfig learnerConfig = Settings.LearnerConfigMap.get(LearnerKey);
            String commandTemplate = learnerConfig.CommandTemplate.replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR);

            return learnerConfig.MachineLearner.SelectOrRankFeatures(commandTemplate, AlgorithmParameters, trainData.Clone());
        }
        catch (Exception ex)
        {
            Singletons.Log.Exception(ex);

            String errorMessage = "An exception occurred while selecting features. ";
            errorMessage += "Algorithm Key: " + Key + ". ";
            errorMessage += "Train data (first five instances):\n" + trainData.toShortString() + "\n";
            errorMessage += "Dependent variable data (first five instances):\n" + Singletons.InstanceVault.TransformedDependentVariableInstances.toShortString() + "\n";

            throw new Exception(errorMessage);
        }
    }

    /** Indicates whether no feature selection/ranking should be performed.
     *
     * @return Whether no feature selection/ranking should be performed.
     */
    public boolean IsNone()
    {
        return Key.equals("None");
    }

    /** Indicates wheter prior-knowledge selection/ranking shold be performed. In this approach, the user typically specifies a list of features that are considered to be most informative based on a literature review.
     *
     * @return Whether prior-knowledge selection/ranking should be performed.
     */
    public boolean IsPriorKnowledge()
    {
        return Key.equals("PriorKnowledge");
    }

    @Override
    public String toString()
    {
        return Key;
    }
}
