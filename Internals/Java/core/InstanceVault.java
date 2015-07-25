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
import mlflex.dataprocessors.RandomDataProcessor;
import mlflex.helper.DataTypeUtilities;
import mlflex.helper.ListUtilities;

import java.util.*;

/** This class provides convenience methods for accessing information about data instances that are used for machine-learning analyses.
 * @author Stephen Piccolo
 */
public class InstanceVault
{
    /** These are the dependent variable (class) instances before any transformation has occurred on them. */
    public DataInstanceCollection RawDependentVariableInstances = null;
    /** These are the dependent variable (class) instances after any transformation has occurred on them. */
    public DataInstanceCollection TransformedDependentVariableInstances = null;
    /** These are the unique dependent variable values. They are sorted also. */
    public ArrayList<String> TransformedDependentVariableOptions = null;

    private HashMap<AbstractDataProcessor, DataInstanceCollection> _processorInstancesMap = new HashMap<AbstractDataProcessor, DataInstanceCollection>();
    private CrossValidationAssignments _cvAssignments = null;

    /** This method retrieves data that have been processed and stores it in memory.
     *
     * @throws Exception
     */
    public void LoadDataInstancesIntoMemory() throws Exception
    {
        // This will be a list of all the unique instances that have data
        HashSet<String> independentVariableDataInstanceIDs = new HashSet<String>();
        // This list will indicate if any data processors should not be processed further
        ArrayList<AbstractDataProcessor> processorsToRemove = new ArrayList<AbstractDataProcessor>();

        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
        {
            if (_processorInstancesMap.containsKey(processor) && !(processor instanceof RandomDataProcessor)) // Don't reload data if it's already been loaded, unless it's random
                continue;

            Singletons.Log.Debug("Loading data instances for " + processor.GetDescription());

            // Retrieve the instances from a file
            DataInstanceCollection dataInstances = GetInstances(processor);

            // Look for any data point that contains class information
            ExtractRawDependentVariableValues(dataInstances);

            // If there is data to processor, store it in memory
            if (dataInstances.Size() > 0 && dataInstances.GetNumDataPoints() > 0)
                _processorInstancesMap.put(processor, dataInstances);
            else
                processorsToRemove.add(processor);

            independentVariableDataInstanceIDs.addAll(dataInstances.GetIDs());
        }

        Singletons.Log.Debug(RawDependentVariableInstances.Size() + " raw dependent variable instances");

        // Remove any data processors that have no data
        for (AbstractDataProcessor processor : processorsToRemove)
        {
            Singletons.ProcessorVault.IndependentVariableDataProcessors.remove(processor);
            Singletons.ProcessorVault.AllDataProcessors.remove(processor);
        }

        // If necessary, transform the dependent variable instances
        if (TransformedDependentVariableInstances == null)
        {
            Singletons.Log.Debug("Reconciling dependent variable values");

            // Finds any instances for which there is class information but no independent variables and vice versa
            ArrayList<String> independentVariableInstancesWithNoDependentVariable = ListUtilities.RemoveAll(new ArrayList<String>(independentVariableDataInstanceIDs), RawDependentVariableInstances.GetIDs());
            ArrayList<String> dependentVariableInstancesWithNoIndependentVariable = ListUtilities.RemoveAll(RawDependentVariableInstances.GetIDs(), new ArrayList<String>(independentVariableDataInstanceIDs));

            // Remove the instances identified above
            for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
                if (_processorInstancesMap.containsKey(processor))
                    _processorInstancesMap.get(processor).RemoveInstances(independentVariableInstancesWithNoDependentVariable);

            RawDependentVariableInstances.RemoveInstances(dependentVariableInstancesWithNoIndependentVariable);

            Singletons.Log.Debug("Transforming dependent variable values");
            TransformDependentVariableInstances();
        }
    }

    /** This method looks in a data instance collection a data point that contains class information. If found, this information is extracted and stored separately.
     *
     * @param dataInstances Data instances that may contain class information
     * @throws Exception
     */
    private void ExtractRawDependentVariableValues(DataInstanceCollection dataInstances) throws Exception
    {
        if (RawDependentVariableInstances == null)
            RawDependentVariableInstances = new DataInstanceCollection();

        String dependentVariableName = Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName;

        for (DataValues instance : dataInstances)
            if (instance.HasDataPoint(dependentVariableName))
                RawDependentVariableInstances.Add(dependentVariableName, instance.GetID(), instance.GetDataPointValue(dependentVariableName));

        dataInstances.RemoveDataPointName(dependentVariableName);
    }

    /** This method contains logic for transforming dependent variable values. This will be relevant if the values are continuous or if it is desired to permute the values, etc.
     *
     * @throws Exception
     */
    private void TransformDependentVariableInstances() throws Exception
    {
        Singletons.Log.Debug("Transforming dependent variable instances");

        if (Singletons.Config.GetBooleanValue("PERMUTE_DEPENDENT_VARIABLE_VALUES", "false"))
        {
            Singletons.Log.Debug("Permuting class labels");
            RawDependentVariableInstances = DataInstanceCollection.PermuteIDs(RawDependentVariableInstances, new Random(Singletons.RandomSeed));
        }

        TransformedDependentVariableInstances = Singletons.ProcessorVault.DependentVariableDataProcessor.TransformDependentVariableInstances(RawDependentVariableInstances);

        if (TransformedDependentVariableInstances.Size() == 0)
            throw new Exception("No dependent variable values were loaded.");

        // Get the unique values so it will be easy to loop through these values later
        TransformedDependentVariableOptions = ListUtilities.SortStringList(TransformedDependentVariableInstances.GetUniqueValues(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName));

        Singletons.Log.Debug("Done transforming dependent variable instances");
    }

    /** This method indicates whether the raw dependent variable has continuous values. ML-Flex needs to know this for processing.
     * @return Value indicateing whether dependent variable has continuous values
     */
    public boolean RawDependentVariableValuesAreContinuous() throws Exception
    {
        return DataTypeUtilities.HasOnlyNumeric(RawDependentVariableInstances.GetUniqueValues(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName));
    }

    /** Retrieves a data instance collection that has already been stored within ML-Flex and performs additional preprocessing steps depending on the settings of the specified processor.
     *
     * @param processor Data processor
     * @return Processed data instances
     * @throws Exception
     */
    private DataInstanceCollection GetInstances(AbstractDataProcessor processor) throws Exception
    {
        Singletons.Log.Debug("Getting analysis instances for " + processor.GetDescription());
        DataInstanceCollection instances = new DataInstanceCollection();
        for (DataValues instance : processor.GetTransformedInstances())
            if (processor.KeepTransformedInstance(instance))
                instances.Add(instance);
        Singletons.Log.Debug("Keeping " + instances.Size() + " transformed instances");

        instances.RemoveInstances(Singletons.Config.GetInstanceIDsToExclude());
        Singletons.Log.Debug(instances.Size() + " instances remaining after excluding any instances based on config");

        Singletons.Log.Debug("Updating instances for analysis for " + processor.GetDescription());
        processor.UpdateInstancesForAnalysis(instances);
        Singletons.Log.Debug(instances.Size() + " instances remaining after updating instances for analysis");

        Singletons.Log.Debug("Removing sparse data points for " + processor.GetDescription());
        processor.RemoveSparseDataPoints(instances);
        Singletons.Log.Debug(instances.GetNumDataPoints() + " data points remaining after removing any sparse data points");

        Singletons.Log.Debug("Removing sparse instances for " + processor.GetDescription());
        processor.RemoveSparseInstances(instances);
        Singletons.Log.Debug(instances.Size() + " instances remaining after removing any sparse instances");

        return instances;
    }

    /** Retrieves a list of data instances that can be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @return Collection of data instances that can be used in machine-learning analyses
     * @throws Exception
     */
    public DataInstanceCollection GetInstancesForAnalysis(AbstractDataProcessor processor) throws Exception
    {
        if (processor.equals(Singletons.ProcessorVault.DependentVariableDataProcessor))
            return TransformedDependentVariableInstances;

        if (!_processorInstancesMap.containsKey(processor))
            return null;

        return _processorInstancesMap.get(processor).Clone();
    }

    /** Retrieves a list of data instances that can be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @param instanceIDs List of data instance IDs (any instance ID not in this list will be ignored)
     * @param dataPoints List of data points (any data point not in this list will be ignored)
     * @return Collection of data instances that can be used in machine-learning analyses
     * @throws Exception
     */
    public DataInstanceCollection GetInstancesForAnalysis(AbstractDataProcessor processor, ArrayList<String> instanceIDs, ArrayList<String> dataPoints) throws Exception
    {
        if (!_processorInstancesMap.containsKey(processor))
            return null;

        return _processorInstancesMap.get(processor).Clone(instanceIDs, dataPoints);
    }

    /** Retrieves a list of data instance IDs that will be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @return List of data instance IDs that will be used in machine-learning analyses
     * @throws Exception
     */
    public ArrayList<String> GetInstanceIDsForAnalysis(AbstractDataProcessor processor) throws Exception
    {
        ArrayList<String> ids = new ArrayList<String>();

        if (_processorInstancesMap.containsKey(processor))
            ids = _processorInstancesMap.get(processor).GetIDs();

        return ids;
    }

    /** Retrieves a list of data points that will be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @return List of data points that will be used in machine-learning analyses
     * @throws Exception
     */
    public ArrayList<String> GetDataPointsForAnalysis(AbstractDataProcessor processor) throws Exception
    {
        ArrayList<String> dataPointNames = new ArrayList<String>();

        if (_processorInstancesMap.containsKey(processor))
            dataPointNames = _processorInstancesMap.get(processor).GetDataPointNames();

        return dataPointNames;
    }

    /** This method return the raw dependent-variable value for a given data instance ID
     *
     * @param instanceID Data instance ID
     * @return Raw dependent-variable instances
     * @throws Exception
     */
    public String GetRawDependentVariableValue(String instanceID) throws Exception
    {
        return RawDependentVariableInstances.Get(instanceID).GetDataPointValue(0);
    }

    /** This method return the transformed dependent-variable value for a given data instance ID
     *
     * @param instanceID Data instance ID
     * @return Transformed dependent-variable instances
     * @throws Exception
     */
    public String GetTransformedDependentVariableValue(String instanceID) throws Exception
    {
        return TransformedDependentVariableInstances.Get(instanceID).GetDataPointValue(0);
    }

    /** This is a convenience method that returns the cross-validation assignments that can be used in machine-learning analyses.
     *
     * @return Cross-validation assignments
     * @throws Exception
     */
    public CrossValidationAssignments GetCrossValidationAssignments() throws Exception
    {
        return GetCrossValidationAssignments(false);
    }

    /** This is a convenience method that returns the cross-validation assignments that can be used in machine-learning analyses.
     *
     * @param reset Whether to initialize (or reinitialize) the cross-validation assignments
     * @return Cross-validation assignments
     * @throws Exception
     */
    public CrossValidationAssignments GetCrossValidationAssignments(boolean reset) throws Exception
    {
        if (_cvAssignments == null || reset)
        {
            Singletons.Log.Debug("Getting cross-validation assignments");
            _cvAssignments = new CrossValidationAssignments(Singletons.Config.GetNumOuterCrossValidationFolds(), TransformedDependentVariableInstances, false).AssignFolds();
        }

        return _cvAssignments;
    }
}
