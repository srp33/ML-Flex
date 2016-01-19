// THIS SOURCE CODE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND, AND ITS AUTHOR AND THE JOURNAL OF MACHINE LEARNING RESEARCH (JMLR) AND JMLR'S PUBLISHERS AND DISTRIBUTORS, DISCLAIM ANY AND ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND ANY WARRANTIES OR NON INFRINGEMENT. THE USER ASSUMES ALL LIABILITY AND RESPONSIBILITY FOR USE OF THIS SOURCE CODE, AND NEITHER THE AUTHOR NOR JMLR, NOR JMLR'S PUBLISHERS AND DISTRIBUTORS, WILL BE LIABLE FOR DAMAGES OF ANY KIND RESULTING FROM ITS USE. Without lim- iting the generality of the foregoing, neither the author, nor JMLR, nor JMLR's publishers and distributors, warrant that the Source Code will be error-free, will operate without interruption, or will meet the needs of the user.
// 
// --------------------------------------------------------------------------
// 
// Copyright 2016 Stephen Piccolo
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
import mlflex.dataprocessors.AggregateDataProcessor;
import mlflex.dataprocessors.RandomDataProcessor;
import mlflex.helper.Config;
import mlflex.helper.DataTypeUtilities;
import mlflex.helper.ListUtilities;

import java.util.*;

/** This class provides convenience methods for accessing information about data instances that are used for machine-learning analyses.
 * @author Stephen Piccolo
 */
public class InstanceVault
{
    /** These are the dependent variable (class) instances. */
    public HashMap<String, String> DependentVariableInstances = null;
    /** These are the unique dependent variable values. They are sorted. */
    public ArrayList<String> DependentVariableOptions = null;

    private HashMap<AbstractDataProcessor, DataInstanceCollection> _processorInstancesMap;
    private CrossValidationAssignments _cvAssignments = null;

    public InstanceVault()
    {
    	DependentVariableInstances = new HashMap<String, String>();
    	_processorInstancesMap = new HashMap<AbstractDataProcessor, DataInstanceCollection>();
    }
    
    /** This method retrieves data that have been processed and stores it in memory.
     *
     * @throws Exception
     */
    public void PrepareDataInstances() throws Exception
    {
    	// Surely a better way to do this...
    	boolean hasRandomProcessor = HasRandomProcessor();
    	boolean hasAggregateProcessor = HasAggregateProcessor();

		// Reload this data processor each iteration
    	if (hasRandomProcessor && !Config.IsFirstIteration())
    	{
			for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
				if (processor instanceof RandomDataProcessor)
				{
	        		Singletons.Log.Debug("Loading data instances for " + processor.GetDescription());
					_processorInstancesMap.put(processor, processor.GetDataInstances());
				}
    	}

    	if (Config.IsFirstIteration())
			for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
			{
				if (!(processor instanceof AggregateDataProcessor))
				{
	        		Singletons.Log.Debug("Loading data instances for " + processor.GetDescription());
					_processorInstancesMap.put(processor, processor.GetDataInstances());
				}
			}

		for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
			if (processor instanceof AggregateDataProcessor)
			{
				if (Config.IsFirstIteration() || hasRandomProcessor)
				{
					Singletons.Log.Debug("Loading data instances for " + processor.GetDescription());
					_processorInstancesMap.put(processor, processor.GetDataInstances());
				}
			}
    	
    	if (Config.IsFirstIteration() || hasRandomProcessor || hasAggregateProcessor)
    	{
	        // This will be a list of all the unique instances that have data
	        HashSet<String> independentVariableDataInstanceIDs = new HashSet<String>();
	        // This list will indicate if any data processors should not be processed further
	        ArrayList<AbstractDataProcessor> processorsToRemove = new ArrayList<AbstractDataProcessor>();
	
	        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
	        {
	            if (Singletons.Config.GetInstanceIDsToExclude().size() > 0)
	            {
	            	_processorInstancesMap.get(processor).RemoveInstances(Singletons.Config.GetInstanceIDsToExclude());
	            	Singletons.Log.Debug(_processorInstancesMap.get(processor).Size() + " instances remaining after excluding instances based on config");
	            }
	
	            // Look for any data point that contains class information
	            ExtractDependentVariableValues(_processorInstancesMap.get(processor));
	
	            // If there is no data to process, indicate that it must be removed
	            if (_processorInstancesMap.get(processor).Size() == 0 || _processorInstancesMap.get(processor).GetNumDataPoints() == 0)
	                processorsToRemove.add(processor);
	
	            independentVariableDataInstanceIDs.addAll(_processorInstancesMap.get(processor).GetIDs());
	        }
	
	        Singletons.Log.Debug(DependentVariableInstances.size() + " dependent variable instances");
	
	        // Remove any data processors that have no data
	        for (AbstractDataProcessor processor : processorsToRemove)
	        {
	            Singletons.ProcessorVault.IndependentVariableDataProcessors.remove(processor);
	            Singletons.ProcessorVault.AllDataProcessors.remove(processor);
	        }
	        
	        Singletons.Log.Debug("Reconciling dependent variable values");
	
	        // Finds any instances for which there is class information but no independent variables and vice versa
	        ArrayList<String> independentVariableInstancesWithNoDependentVariable = ListUtilities.GetDifference(new ArrayList<String>(independentVariableDataInstanceIDs), new ArrayList<String>(DependentVariableInstances.keySet()));
	        ArrayList<String> dependentVariableInstancesWithNoIndependentVariable = ListUtilities.GetDifference(new ArrayList<String>(DependentVariableInstances.keySet()), new ArrayList<String>(independentVariableDataInstanceIDs));
	
			// Remove the instances identified above
	        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
	            if (_processorInstancesMap.containsKey(processor))
	                _processorInstancesMap.get(processor).RemoveInstances(independentVariableInstancesWithNoDependentVariable);
	        
	        for (String instanceID : dependentVariableInstancesWithNoIndependentVariable)
	            DependentVariableInstances.remove(instanceID);
    	}
    }

	private boolean HasRandomProcessor()
	{
		boolean hasRandomProcessor = false;
		
		for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
			if (processor instanceof RandomDataProcessor)
			{
				hasRandomProcessor = true;
				break;
			}
		
		return hasRandomProcessor;
	}
	
	private boolean HasAggregateProcessor()
	{
		boolean hasAggregateProcessor = false;
		
		for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
			if (processor instanceof AggregateDataProcessor)
			{
				hasAggregateProcessor = true;
				break;
			}
		
		return hasAggregateProcessor;
	}

    /** Retrieves a list of data instances that can be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @return Collection of data instances that can be used in machine-learning analyses
     * @throws Exception
     */
    public DataInstanceCollection GetInstancesForAnalysis(AbstractDataProcessor processor) throws Exception
    {
        return _processorInstancesMap.get(processor);
    }

    /** Retrieves a list of data instances that can be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @param instanceIDs List of data instance IDs (any instance ID not in this list will be ignored)
     * @param dataPoints List of data points (any data point not in this list will be ignored)
     * @return Collection of data instances that can be used in machine-learning analyses
     * @throws Exception
     */
    public DataInstanceCollection GetInstancesForAnalysis(AbstractDataProcessor processor, ArrayList<String> instanceIDs) throws Exception
    {
        if (!_processorInstancesMap.containsKey(processor))
            return null;

        return _processorInstancesMap.get(processor).Get(instanceIDs);
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
    
    /** This method looks in a data instance collection a data point that contains class information. If found, this information is extracted and stored separately.
    *
    * @param dataInstances Data instances that may contain class information
    * @throws Exception
    */
    private void ExtractDependentVariableValues(DataInstanceCollection dataInstances) throws Exception
    {
        String dependentVariableName = Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName;
        
        if (!dataInstances.HasDataPoint(dependentVariableName))
        	return;

        for (String instanceID : dataInstances)
        {
            String value = dataInstances.GetDataPointValue(instanceID, dependentVariableName);
           
            if (DataTypeUtilities.IsNumeric(value))
            	value = "Class" + value;
           
		 	DependentVariableInstances.put(instanceID, value);
        }

        dataInstances.RemoveDataPointName(dependentVariableName);
       
        Singletons.InstanceVault.DependentVariableOptions = ListUtilities.SortStringList(ListUtilities.GetUniqueValues(new ArrayList<String>(DependentVariableInstances.values())));
    }

    /** This method return the raw dependent-variable value for a given data instance ID
     *
     * @param instanceID Data instance ID
     * @return Raw dependent-variable instances
     * @throws Exception
     */
    public String GetDependentVariableValue(String instanceID) throws Exception
    {
        return DependentVariableInstances.get(instanceID);
    }
    
    public HashMap<String, String> GetDependentVariableValues(ArrayList<String> instanceIDs) throws Exception
    {
    	HashMap<String, String> values = new HashMap<String, String>();
    	
    	for (String instanceID : instanceIDs)
    		values.put(instanceID, DependentVariableInstances.get(instanceID));
    	
    	return values;
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
        if (reset)
        {
            Singletons.Log.Debug("Getting cross-validation assignments");
            _cvAssignments = new CrossValidationAssignments(Singletons.Config.GetNumOuterCrossValidationFolds(), DependentVariableInstances, false).AssignFolds();
        }

        return _cvAssignments;
    }
}
