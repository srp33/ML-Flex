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

import mlflex.learners.AbstractMachineLearner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** This class acts as a wrapper for performing feature-selection tasks. It interprets parameters for executing these tasks, based on what has been configured in ML-Flex's configuration files.
 * @author Stephen Piccolo
 */
public class FeatureSelectionAlgorithm
{
    /** Description of the algorithm */
    public String Description;

    private AbstractMachineLearner _learner;
    private ArrayList<String> _algorithmParameters;

    /** Constructor
     *
     * @param description Name of the algorithm
     */
    public FeatureSelectionAlgorithm(String description)
    {
        Description = description;
        _learner = null;
        _algorithmParameters = null;
    }

    /** Constructor
     *
     * @param description Name of the algorithm
     * @param learnerClassName Full name of the ML-Flex Java class that extends AbstractMachineLearner that contains the algorithm's logic
     * @param algorithmParameters List of parameters that are passed to the algorithm
     * @throws Exception
     */
    public FeatureSelectionAlgorithm(String description, String learnerClassName, ArrayList<String> algorithmParameters) throws Exception
    {
        Description = description;
        _learner = (AbstractMachineLearner) ((Constructor) Class.forName(learnerClassName).getConstructor()).newInstance();
        _algorithmParameters = algorithmParameters;
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
            return _learner.SelectOrRankFeatures(_algorithmParameters, trainData.Clone());
        }
        catch (Exception ex)
        {
            Singletons.Log.Exception(ex);

            String errorMessage = "An exception occurred while selecting features. ";
            errorMessage += "Algorithm: " + Description + ". ";
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
        return Description.equals("None");
    }

    /** Indicates wheter prior-knowledge selection/ranking shold be performed. In this approach, the user typically specifies a list of features that are considered to be most informative based on a literature review.
     *
     * @return Whether prior-knowledge selection/ranking should be performed.
     */
    public boolean IsPriorKnowledge()
    {
        return Description.equals("PriorKnowledge");
    }

    @Override
    public String toString()
    {
        return Description;
    }
}
