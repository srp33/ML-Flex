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

import mlflex.helper.ListUtilities;

import java.util.ArrayList;

/** This class stores information about a prediction that has been for the dependent variable of a given data instance. It includes information not only about the predicted class but also about the probabilities assigned to each class.
 * @author Stepen Piccolo
 */
public class Prediction
{
    /** Data instance for which the prediction was made. */
    public String InstanceID;
    /** The dependent variable (class) value. */
    public String DependentVariableValue;
    /** The prediction that was made via classification. */
    public String Prediction;
    /** The posterior probability of each class. */
    public ArrayList<Double> ClassProbabilities;

    /** Constructor
     *
     * @param instanceID Data instance for which prediction was made
     * @param dependentVariableValue Actual dependent-variable value of the specified instance
     * @param prediction Predicted dependent-variable value of the specified instance
     */
    public Prediction(String instanceID, String dependentVariableValue, String prediction) throws Exception
    {
        this(instanceID, dependentVariableValue, prediction, GetDefaultClassProbabilities(prediction));
    }

    /** Constructor
     *
     * @param instanceID Data instance for which prediction was made
     * @param dependentVariableValue Actual dependent-variable value of the specified instance
     * @param prediction Predicted dependent-variable value of the specified instance
     * @param classProbabilities Probabilities of each possible dependent-variable value in the same order that the dependent-variable processor orders the possible dependent-variable values.
     */
    public Prediction(String instanceID, String dependentVariableValue, String prediction, ArrayList<Double> classProbabilities) throws Exception
    {
        InstanceID = instanceID;
        DependentVariableValue = dependentVariableValue;
        Prediction = prediction;
        ClassProbabilities = classProbabilities;
    }

    private static ArrayList<Double> GetDefaultClassProbabilities(String prediction) throws Exception
    {
        ArrayList<Double> classProbabilities = new ArrayList<Double>();

        for (String dependentVariableValue : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            if (dependentVariableValue.equals(prediction))
                classProbabilities.add(1.0);
            else
                classProbabilities.add(0.0);
        }

        return classProbabilities;
    }

    /** This is a convenience method for accessing the raw dependent-variable value of a given data instance
     *
     * @return Raw dependent-variable value
     * @throws Exception
     */
    public String GetRawDependentVariableValue() throws Exception
    {
        return Singletons.InstanceVault.GetRawDependentVariableValue(InstanceID);
    }

    @Override
    public String toString()
    {
        ArrayList<String> values = new ArrayList<String>();
        values.add(InstanceID);
        values.add(DependentVariableValue);
        values.add(Prediction);
        values.addAll(ListUtilities.CreateStringList(ClassProbabilities));

        return ListUtilities.Join(values, "\t");
    }

    /** Convenience method that indicates whether this prediction was correct or not.
     *
     * @return Whether the prediction was correct
     */
    public boolean WasCorrect()
    {
        return DependentVariableValue.equals(Prediction);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof Prediction))
            return false;

        Prediction compareObj = (Prediction)obj;

        return compareObj.InstanceID.equals(this.InstanceID) && compareObj.Prediction.equals(this.Prediction) && compareObj.DependentVariableValue.equals(this.DependentVariableValue) && compareObj.ClassProbabilities.equals(this.ClassProbabilities);
    }

    @Override
    public int hashCode()
    {
        return ClassProbabilities.hashCode(); // Not sure if this makes sense, but it should be fine
    }
}