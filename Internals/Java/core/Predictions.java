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

import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class stores information about predictions that have been made. It contains methods to make it easier to deal with multiple predictions.
 * @author Stephen Piccolo
 */
public class Predictions
{
    private HashMap<String, Prediction> _predictionMap = new HashMap<String, Prediction>();

    /** Default constructor
     */
    public Predictions()
    {
    }

    /** Alternate constructor
     *
     * @param predictions List of predictions
     */
    public Predictions(ArrayList<Prediction> predictions)
    {
        for (Prediction prediction : predictions)
            Add(prediction);
    }

    /** Adds a prediction to this set of predictions
     *
     * @param prediction A single prediction to add
     * @return This instance
     */
    private Predictions Add(Prediction prediction)
    {
        _predictionMap.put(prediction.InstanceID, prediction);
        return this;
    }

    /** Retrieves the prediction for a given instance ID.
     *
     * @param instanceID Data instance ID
     * @return A prediction
     */
    public Prediction Get(String instanceID)
    {
        return _predictionMap.get(instanceID);
    }

    private ArrayList<Prediction> _all = null;
    /** Gets a list of all predictions that are in this object, sorted by instance ID.
     *
     * @return List of all predictions that are in this object
     */
    public ArrayList<Prediction> GetAll()
    {
        if (_all == null)
        {
            _all = new ArrayList<Prediction>();
            for (String instanceID : GetInstanceIDs())
                _all.add(Get(instanceID));
        }

        return _all;
    }

    private ArrayList<String> _instanceIDs = null;
    /** Retrieves a list of instance IDs for which predictions have been made
     *
     * @return List of instance IDs for which predictions have been made
     */
    public ArrayList<String> GetInstanceIDs()
    {
        if (_instanceIDs == null)
            _instanceIDs = ListUtilities.SortStringList(new ArrayList<String>(_predictionMap.keySet()));

        return _instanceIDs;
    }

    /** Convenience method to indicate how many predictions matched the specified class value
     *
     * @param predictedClass Predicted class value
     * @return Number of predictions matching the specified class value
     */
    public int GetNumMatchingPredictedClasses(String predictedClass)
    {
        int count = 0;

        for (String instanceID : GetInstanceIDs())
            if (Get(instanceID).Prediction.equals(predictedClass))
                count++;

        return count;
    }

    /** Gets a list of all the class predictions for the instances represented here.
     *
     * @return List of all class predictions
     */
    public ArrayList<String> GetPredictedClasses()
    {
        ArrayList<String> predictedClasses = new ArrayList<String>();

        for (Prediction prediction : _predictionMap.values())
            predictedClasses.add(prediction.Prediction);

        return predictedClasses;
    }

    /** Indicates the number of unique predicted classes
     *
     * @return Number of unique predicted classes
     */
    public ArrayList<String> GetUniquePredictedClasses()
    {
        return ListUtilities.SortStringList(new ArrayList<String>(new HashSet<String>(GetPredictedClasses())));
    }

    /** Indicates whether a prediction has been made for a given instance ID
     *
     * @param instanceID Data instance ID
     * @return Whether a prediction has been made
     * @throws Exception
     */
    public boolean HasPrediction(String instanceID) throws Exception
    {
        return _predictionMap.containsKey(instanceID);
    }

    /** Reads predictions from a text file when those predictions have already been made and stored.
     *
     * @param filePath Absolute path to the file containing predictions
     * @return Predictions that were in the file
     * @throws Exception
     */
    public static Predictions ReadFromFile(String filePath) throws Exception
    {
        if (!FileUtilities.FileExists(filePath))
            return new Predictions();

        // Retrieve the predictions text from an existing file
        ArrayList<ArrayList<String>> fileLines = FileUtilities.ParseDelimitedFile(filePath);

        // Make sure the file is not empty
        if (fileLines.size() == 0)
            return new Predictions();

        // Remove the header information
        fileLines.remove(0);

        Predictions predictions = new Predictions();

        // Loop through the text and parse out the prediction information
        for (ArrayList<String> row : fileLines)
        {
            String id = row.get(0);
            String actualClass = row.get(1);
            String predictedClass = row.get(2);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (int i=3; i<row.size(); i++)
                classProbabilities.add(Double.parseDouble(row.get(i)));

            predictions.Add(new Prediction(id, actualClass, predictedClass, classProbabilities));
        }

        return predictions;
    }

    /** Saves predictions that have already been made, to a file.
     *
     * @param filePath Absolute file path where the predictions will be stored
     * @throws Exception
     */
    public void SaveToFile(String filePath) throws Exception
    {
        ArrayList<String> header = new ArrayList<String>();

        // Create the header
        header.addAll(ListUtilities.CreateStringList("Instance_ID", "Dependent_Variable_Value", "Prediction"));
        for (String x : Singletons.InstanceVault.TransformedDependentVariableOptions)
            header.add(x + "_Probability");

        StringBuffer buffer = new StringBuffer();
        buffer.append(ListUtilities.Join(header, "\t") + "\n");

        // Loop through the predictions and construct the output
        for (String instanceID : GetInstanceIDs())
        {
            Prediction prediction = Get(instanceID);

            ArrayList<String> outputVals = new ArrayList<String>();

            outputVals.add(prediction.InstanceID);
            outputVals.add(prediction.DependentVariableValue);
            outputVals.add(prediction.Prediction);

            for (double classProbability : prediction.ClassProbabilities)
                outputVals.add(String.valueOf(classProbability));

            buffer.append(ListUtilities.Join(outputVals, "\t") + "\n");
        }

        // Save the output to a file
        FileUtilities.WriteLineToFile(filePath, buffer.toString());
    }

    /** Indicates the number of predictions that have been made
     *
     * @return The number of predictions that have been made
     */
    public int Size()
    {
        return _predictionMap.size();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (String instanceID : GetInstanceIDs())
            builder.append("\n" + Get(instanceID).toString());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof Predictions))
            return false;

        return ((Predictions)obj)._predictionMap.equals(_predictionMap);
    }

    @Override
    public int hashCode()
    {
        return _predictionMap.hashCode();
    }
}