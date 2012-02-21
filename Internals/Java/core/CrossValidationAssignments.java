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

import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.helper.ListUtilities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** This class is designed to store information about which cross-validation fold is assigned to each data instance. It also provides methods to make it easier to access this information for machine-learning tasks.
 * @author Stephen Piccolo
 */
public class CrossValidationAssignments
{
    /** A mapping of the actual assignments that have been made */
    protected HashMap<Integer, ArrayList<String>> Assignments = new HashMap<Integer, ArrayList<String>>();
    /** A mapping of cross-validation assignments for each inner fold */
    private ConcurrentHashMap<Integer, CrossValidationAssignments> _innerAssignments = null;
    /** Number of cross validation folds */
    public int NumFolds;
    /** The dependent variable instances that will be used for assigning to the folds */
    protected DataInstanceCollection DependentVariableInstances;
    /** Whether we are assigning inner cross-validation folds */
    protected boolean IsInner;

    /** Constructor
     *
     * @param numFolds Number of cross validation folds to use
     * @param dependentVariableInstances Dependent variable data instances
     * @param isInner Indicates whether this is dealing with inner folds or outer folds
     * @throws Exception
     */
    public CrossValidationAssignments(int numFolds, DataInstanceCollection dependentVariableInstances, boolean isInner) throws Exception
    {
        NumFolds = numFolds;
        DependentVariableInstances = dependentVariableInstances;
        IsInner = isInner;
    }

    /** Assigns data instances to cross-validation folds that have been created
     *
     * @return This object
     * @throws Exception
     */
    public CrossValidationAssignments AssignFolds() throws Exception
    {
        // Check for any assignments that have been explicitly defined in the experiment configuration
        ArrayList<String> configTrainIDs = Singletons.Config.GetTrainingInstanceIDs();
        ArrayList<String> configTestIDs = Singletons.Config.GetTestInstanceIDs();

        // Check whether we are assigning instances to folds that have been explicitly defined in the experiment configuration
        if (!IsInner && configTrainIDs.size() > 0 && configTestIDs.size() > 0)
        {
            Assignments.put(1, configTrainIDs);
            Assignments.put(2, configTestIDs);

            // Make sure the instances configured in the experiment configuration are valid
            if (ListUtilities.Intersect(Singletons.InstanceVault.TransformedDependentVariableInstances.GetIDs(), configTrainIDs).size() == 0)
                Singletons.Log.ExceptionFatal("None of the training IDs specified in the experiment file overlap with the actual data instances.");

            if (ListUtilities.Intersect(Singletons.InstanceVault.TransformedDependentVariableInstances.GetIDs(), configTestIDs).size() == 0)
                Singletons.Log.ExceptionFatal("None of the test IDs specified in the experiment file overlap with the actual data instances.");

            Singletons.Log.Debug("Training and testing will be performed using assignments from the TRAINING_INSTANCE_IDS and TEST_INSTANCE_IDS experiment configuration settings.");
            return new TrainTestValidationAssignments(Assignments, DependentVariableInstances, IsInner);
        }

        // Check whether this is a train/test experiment
        if (NumFolds == 1)
        {
            AssignToFoldsViaStratification(2);
            return new TrainTestValidationAssignments(Assignments, DependentVariableInstances, IsInner);
        }

        // Check whether this is a leave-one-out cross-validation experiment
        if (NumFolds == DependentVariableInstances.Size())
        {
            // Assign each instance to its own fold
            for (int i = 1; i <= DependentVariableInstances.Size(); i++)
                Assignments.put(i, ListUtilities.CreateStringList(DependentVariableInstances.Get(i-1).GetID()));

            return this;
        }

        // For regular cross validation, assign folds
        AssignToFoldsViaStratification(NumFolds);

        return this;
    }

    /** Assigns each instance to a fold via stratification. This means that an attempt is made to distribute the instances even across the classes as much as possible.
     *
     * @param numFolds Number of cross-validation folds
     * @throws Exception
     */
    private void AssignToFoldsViaStratification(int numFolds) throws Exception
    {
        int currentFold = 1;

        for (String option : Singletons.InstanceVault.TransformedDependentVariableOptions)
        {
            // Get all instances of the given class
            ArrayList<String> instanceIDs = DependentVariableInstances.FilterByDataPointValue(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName, option).GetIDs();

            // Randomly shuffle the instances
            Collections.shuffle(instanceIDs, new Random(Singletons.RandomSeed));

            for (String instanceID : instanceIDs)
            {
                AssignToFold(currentFold, instanceID);

                if (currentFold == numFolds)
                    currentFold = 1;
                else
                    currentFold++;
            }
        }
    }

    /** Assigns a given instance to a given fold.
     *
     * @param foldNumber Fold number
     * @param id Instance ID
     */
    private void AssignToFold(int foldNumber, String id)
    {
        if (Assignments.containsKey(foldNumber))
        {
            ArrayList<String> existing = Assignments.get(foldNumber);
            existing.add(id);
            Assignments.put(foldNumber, existing);
        }
        else
            Assignments.put(foldNumber, ListUtilities.CreateStringList(id));
    }

    /** Indicates which fold a given instance is assigned to
     *
     * @param instanceID Instance ID
     * @return Which fold the instance is assigned to
     * @throws Exception
     */
    public int GetFoldNumber(String instanceID) throws Exception
    {
        for (Map.Entry<Integer, ArrayList<String>> entry : Assignments.entrySet())
            if (entry.getValue().contains(instanceID))
                return entry.getKey();
        
        throw new Exception("The instance ID (" + instanceID + ") is not assigned to a fold");
    }

    /** Indicates which folds have at least one data instance assigned to them, after filtering has occurred
     *
     * @param processor Data processor
     * @return List of folds
     * @throws Exception
     */
    public ArrayList<Integer> GetFoldsWithTestData(AbstractDataProcessor processor) throws Exception
    {
        ArrayList<Integer> folds = new ArrayList<Integer>();

        for (int fold : GetAllFoldNumbers())
            if (HasTestData(processor, fold))
                folds.add(fold);

        return folds;
    }

     /** Indicates all cross-validation folds, whether or not any data instance have been assigned to them
     *
     * @return List of folds
     * @throws Exception
     */
    public ArrayList<Integer> GetAllFoldNumbers() throws Exception
    {
        ArrayList<Integer> folds = new ArrayList<Integer>(Assignments.keySet());
        Collections.sort(folds);
        return folds;
    }

    /** Indicates IDs for data instances that have been assigned to cross-validation folds
     *
     * @return Data instance IDs
     */
    private ArrayList<String> GetAllIDs()
    {
        ArrayList<String> ids = new ArrayList<String>();

        for (ArrayList<String> x : Assignments.values())
            ids.addAll(x);

        return ids;
    }

    /** Indicates test instance IDs for a given cross-validation fold
     *
     * @param fold Number of cross validation fold
     * @return Data instance IDs assigned to that fold
     * @throws Exception
     */
    public ArrayList<String> GetTestIDs(int fold) throws Exception
    {
        return new ArrayList<String>(Assignments.get(fold));
    }

    /** Indicates training instance IDs for a given cross-validation fold
     *
     * @param fold Number of cross validation fold
     * @return Data instance IDs assigned to that fold
     * @throws Exception
     */
    public ArrayList<String> GetTrainIDs(int fold) throws Exception
    {
        return FilterTrainIDs(ListUtilities.RemoveAll(GetAllIDs(), GetTestIDs(fold)));
    }

    /** Returns a list of training instance IDs that have been excluded across all cross-validation folds.
     *
     * @return Training instance IDs that have been excluded
     * @throws Exception
     */
    public ArrayList<String> GetAllExcludedTrainIDs() throws Exception
    {
        ArrayList<String> excluded = new ArrayList<String>();

        for (int fold : GetAllFoldNumbers())
        {
            ArrayList<String> trainIDs = ListUtilities.RemoveAll(GetAllIDs(), GetTestIDs(fold));
            excluded.addAll(GetTrainIDsToExclude(trainIDs));
        }

        return excluded;
    }

    /** If the relevant configuration value is specified, this method randomly excludes a subset of training IDs.
     *
     * @param trainIDs List of training IDs to be filtered
     * @throws Exception
     */
    protected ArrayList<String> FilterTrainIDs(ArrayList<String> trainIDs) throws Exception
    {
        return ListUtilities.RemoveAll(trainIDs, GetTrainIDsToExclude(trainIDs));
    }

    /** This method indicates which training instances, if any, should be excluded randomly from the analysis.
     * @param instanceIDs List of all instance IDs that may be excluded
     * @return List of instance IDs to exclude
     * @throws Exception
     */
    public ArrayList<String> GetTrainIDsToExclude(ArrayList<String> instanceIDs) throws Exception
    {
        ArrayList<String> filterIDs = new ArrayList<String>();

        if (!IsInner)
        {
            int numInstancesToExclude = Singletons.Config.GetNumTrainingInstancesToExcludeRandomly();
            if (numInstancesToExclude > 0)
                filterIDs = ListUtilities.GetRandomSubset(instanceIDs, numInstancesToExclude, new Random(Singletons.RandomSeed));
        }

        return filterIDs;
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTrainInstances(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetTrainInstances(processor, fold, null);
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @param dataPoints List of data points that should be included for the given data instances
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTrainInstances(AbstractDataProcessor processor, int fold, ArrayList<String> dataPoints) throws Exception
    {
        return Singletons.InstanceVault.GetInstancesForAnalysis(processor, GetTrainIDs(fold), dataPoints);
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @param dataPoints List of data points that should be included for the given data instances
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTestInstances(AbstractDataProcessor processor, int fold, ArrayList<String> dataPoints) throws Exception
    {
        return Singletons.InstanceVault.GetInstancesForAnalysis(processor, GetTestIDs(fold), dataPoints);
    }

    /** Indicates how many test instances are assigned to a given fold for a given data processor. This method is provided to improve performance.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Number of instances
     * @throws Exception
     */
    public int GetNumTestInstances(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetTestInstances(processor, fold, new ArrayList<String>()).Size();
    }

    /** Indicates whether a given combination of data processor and cross-validation fold have any test instances.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Whether there are any test instances
     * @throws Exception
     */
    public boolean HasTestData(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetNumTestInstances(processor, fold) > 0;
    }

    /** Indicates whether a given data processor has any test instances for any cross-validation fold.
     *
     * @param processor Data processor
     * @return Whether there are any test instances
     * @throws Exception
     */
    public boolean HasAnyTestData(AbstractDataProcessor processor) throws Exception
    {
        for (int outerFold : GetAllFoldNumbers())
            if (HasTestData(processor, outerFold))
                return true;

        return false;
    }

    /** Returns the inner cross-validation assignments for a given outer cross-validation fold
     *
     * @param outerFold Outer cross-validation fold
     * @return Cross-validation assignments
     * @throws Exception
     */
    public CrossValidationAssignments GetInnerAssignments(int outerFold) throws Exception
    {
        if (_innerAssignments == null)
        {
            _innerAssignments = new ConcurrentHashMap<Integer, CrossValidationAssignments>();

            for (int f : GetAllFoldNumbers())
            {
                CrossValidationAssignments assignments = new CrossValidationAssignments(Singletons.Config.GetNumInnerCrossValidationFolds(), DependentVariableInstances.Get(GetTrainIDs(f)), true).AssignFolds();
                _innerAssignments.put(f, assignments);
            }
        }

        return _innerAssignments.get(outerFold);
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();

        for (Map.Entry<Integer, ArrayList<String>> entry : Assignments.entrySet())
        {
            output.append("Fold " + Integer.toString(entry.getKey()) + " (" + entry.getValue().size() + " instances): ");
            output.append(ListUtilities.Join(ListUtilities.SortStringList(entry.getValue()), ",") + "\n");
        }

        return output.toString();
    }
}