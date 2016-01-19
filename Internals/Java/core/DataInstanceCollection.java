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

import mlflex.helper.*;
import mlflex.parallelization.MultiThreadedTaskHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.TreeMap;

/** This class is designed to store all data for a set of data instances. It provides methods that make it easier to create, retrieve, update, and delete data values for these instances.
 * @author Stephen Piccolo
 */
public class DataInstanceCollection implements Iterable<String>
{
    /** This value is placed at the end of a file that contains a serialized version of this object. It's used to verify that the entire file was stored properly. */
    public static String END_OF_FILE_MARKER = "[EOF]";
    private static String COMMA_REPLACE_STRING = "_comma_";
    
    private HashMap<String, HashMap<Integer, String>> _instances;
    private HashMap<String, Integer> _valueIndexMap;
    private int _intRefCount;

    /** Default constructor */
    public DataInstanceCollection()
    {
        _instances = new HashMap<String, HashMap<Integer, String>>();
        _valueIndexMap  = new HashMap<String, Integer>();
        _intRefCount = Integer.MIN_VALUE;
    }

    /** Adds a data value for a given instance to this collection.
     *
     * @param dataPointName Data point name
     * @param instanceID Data instance ID
     * @param value Data value
     */
    public void Add(String dataPointName, String instanceID, String value)
    {
    	if (MiscUtilities.IsMissing(value))
    		return;
    	
    	dataPointName = MiscUtilities.FormatName(dataPointName);
    	
    	Integer intDataPointName = GetIntRef(dataPointName);

    	if (!_instances.containsKey(instanceID))
    		_instances.put(instanceID, new HashMap<Integer, String>());

    	_instances.get(instanceID).put(intDataPointName, value);
    }
    
    /** For a given data point, this method converts values to zero or one, depending on whether they coincide with the specified value.
     *
     * @param dataPointName Data point name
     * @param oneOption Value that corresponds to a "1" binary value (other values will be converted to "0"
     */
    public DataInstanceCollection BinarizeDataPoint(String dataPointName, String oneOption)
    {
        for (String instanceID : this)
        	Add(dataPointName, instanceID, ConvertToBinary(GetDataPointValue(instanceID, GetIntRef(dataPointName)), oneOption));
        
        return this;
    }
    
    /** Converts a data point to a binary value. If the value is the same as the "one option" value, it is marked as 1; otherwise, it is marked as 0.
    *
    * @param value Raw data point value
    * @param oneOption One option
    */
   public String ConvertToBinary(String value, String oneOption)
   {
       if (MiscUtilities.IsMissing(value))
           return Settings.MISSING_VALUE_STRING;
       else
           return value.equals(oneOption) ? "1" : "0";
   }

    /** Indicates whether this collection contains the specified data instance.
     *
     * @param instanceID Query instance ID
     * @return Whether the collection contains the instance
     */
    public boolean Contains(String instanceID)
    {
        return _instances.containsKey(instanceID);
    }

//    /** Gets the data instance for the specified data instance ID.
//     *
//     * @param id Query data instance ID
//     * @return Data instance for the specified data instance ID
//     */
//    public HashMap<String, String> Get(String instanceID)
//    {
//    	HashMap<Integer, String> intInstance = _instances.get(instanceID);
//    	HashMap<String, String> instance = new HashMap<String, String>();
//
//    	if (intInstance != null)
//    	{
//    		for (String dataPointName : _dataPointNames)
//    			instance.put(dataPointName, intInstance.get(GetIntRef(dataPointName)));
//    		
//    		return instance;
//    	}
//
//        return null;
//    }

    /** Gets a collection of data instances that match the specified data instance IDs.
    *
    * @param ids Query data instance IDs
    * @return Collection of data instances for specified data instance IDs
    */
   public DataInstanceCollection Get(ArrayList<String> instanceIDs)
   {
       DataInstanceCollection result = new DataInstanceCollection();
       
       result._valueIndexMap = _valueIndexMap;
       
       for (String instanceID : instanceIDs)
			result._instances.put(instanceID, _instances.get(instanceID));

       return result;
   }
    
    /** Gets a list of all data point names across all data instances in the collection.
    *
    * @return List of all data point names
    */
   public ArrayList<String> GetDataPointNames()
   {
       return ListUtilities.SortStringList(new ArrayList<String>(_valueIndexMap.keySet()));
   }

   /** Gets the data point value for specified instance and data point.
   *
   * @param instanceID Instance ID
   * @param name Data point name
   * @return Data point value
   */
   private String GetDataPointValue(String instanceID, Integer intDataPointName)
   {
	   HashMap<Integer, String> instance = _instances.get(instanceID);
	   
	   if (instance == null)
		   return Settings.MISSING_VALUE_STRING;
	   
	   String value = instance.get(intDataPointName);
	   
	   if (value == null)
		   return Settings.MISSING_VALUE_STRING;
	   
	   return value;
   }
   
   public String GetDataPointValue(String instanceID, String dataPointName)
   {
	   return GetDataPointValue(instanceID, GetIntRef(dataPointName));
   }

	/** Gets the data point values across all data instances for the specified data point.
	 *
	 * @param dataPointName Query data point name
	 * @return Data values across all data instances
	 */
	public HashMap<String, String> GetDataPointValues(String dataPointName)
	{
	    HashMap<String, String> values = new HashMap<String, String>();
	
	    for (String instanceID : _instances.keySet())
	        values.put(instanceID, GetDataPointValue(instanceID, dataPointName));
	
	    return values;
	}
    
    /** Gets the data point values across all data instances for the specified data point.
    *
    * @param dataPointName Query data point name
    * @return Data values across all data instances
    */
    public ArrayList<String> GetDataPointValues(String instanceID, ArrayList<String> dataPointNames)
    {
       ArrayList<String> values = new ArrayList<String>();

       for (String dataPointName : dataPointNames)
           values.add(GetDataPointValue(instanceID, dataPointName));

       return values;
    }

    /** Gets a list of data instance IDs for the instances in this collection.
     *
     * @return List of all data instance IDs in this collection
     */
    public ArrayList<String> GetIDs()
    {
        return ListUtilities.SortStringList(new ArrayList<String>(_instances.keySet()));
    }
    
    private Integer GetIntRef(String key)
    {
    	Integer intKey = _valueIndexMap.get(key);

    	if (intKey == null)
    	{
    		intKey = new Integer(_intRefCount);
    		_intRefCount++;

    		_valueIndexMap.put(key, intKey);
    	}
    	
    	return intKey;
    }

    /** Indicates the number of data point names across all data instances in this collection.
     *
     * @return Number of data point names across all data instances in this collection
     */
    public int GetNumDataPoints()
    {
        return _valueIndexMap.size();
    }

    /** Indicates the proportion of missing values across all data instances in this collection.
     *
     * @return Proportion of missing values.
     */
    public double GetProportionMissingValues()
    {
        double numNotMissing = 0.0;

        for (String instanceID : _instances.keySet())
        	for (String dataPointName : _valueIndexMap.keySet())
        	{
        		String value = GetDataPointValue(instanceID, dataPointName);
        		
        		if (!MiscUtilities.IsMissing(value))
                    numNotMissing++;
        	}

        double proportionMissing = 1 - (numNotMissing / ((double) Size() * (double) GetNumDataPoints()));
        return MathUtilities.Round(proportionMissing, 3);
    }

    /** Identifies all unique values for the specified data point, across all data instances in the collection. Null and missing values are ignored.
     *
     * @param dataPointName Query data point name
     * @return List of all unique values for the specified data point
     */
    public ArrayList<String> GetUniqueValues(String dataPointName)
    {
        HashSet<String> values = new HashSet<String>();

        for (String instanceID : _instances.keySet())
        {
            String value = GetDataPointValue(instanceID, dataPointName);

            if (!MiscUtilities.IsMissing(value))
            	values.add(value.intern());
        }

        return new ArrayList<String>(values);
    }
    
	public boolean HasDataPoint(String dataPointName)
	{
		return _valueIndexMap.containsKey(dataPointName);
	}
    
    public boolean HasDataPoint(String instanceID, String dataPointName)
    {
    	if (!Contains(instanceID))
    		return false;
    	
    	String value = GetDataPointValue(instanceID, dataPointName);
    	
		return value != null && !value.equals(Settings.MISSING_VALUE_STRING);
    }

    /** Removes the specified data point from all instances in the collection.
     *
     * @param dataPointName Data point to be removed
     */
    public void RemoveDataPointName(String dataPointName)
    {
    	Integer intDataPointName = GetIntRef(dataPointName);
    	
        for (String instanceID : this)
        {
        	if (_instances.containsKey(instanceID))
                _instances.get(instanceID).remove(intDataPointName);
        }
        
        _valueIndexMap.remove(dataPointName);
    }

    /** Removes data instances that are in the specified list.
     *
     * @param ids List of data instance IDs to be removed
     */
    public void RemoveInstances(ArrayList<String> ids)
    {
        for (String id : ids)
            RemoveInstance(id);
    }

    /** Removes the specified data instance.
     *
     * @param instanceID Data instance ID
     */
    public void RemoveInstance(String instanceID)
    {
    	_instances.remove(instanceID);
    }

    /** Saves this collection to a text file in a tab-delimited format.
     *
     * @param outputDirectory Absolute path to the directory where the file will be saved
     * @param fileNamePrefix File name prefix
     * @return Absolute path to the saved file
     * @throws Exception
     */
    public String SaveToFile(String outputDirectory, String fileNamePrefix) throws Exception
    {
        AnalysisFileCreator creator = new AnalysisFileCreator(outputDirectory, fileNamePrefix, this, null, true, GetDataPointNames());
        return creator.CreateTabDelimitedFile().GetTabDelimitedFilePath();
    }

    /** Indicates the number of data instances in this collection.
     *
     * @return Number of data instances in this collection
     */
    public int Size()
    {
        return _instances.size();
    }

    public Iterator<String> iterator()
    {
        return GetIDs().iterator();
    }

//    @Override
//    public String toString()
//    {
//        StringBuilder builder = new StringBuilder();
//
//        for (String instanceID : this)
//            builder.append(instanceToString(instanceID) + "\n");
//
//        return builder.toString();
//    }
    
    /** Creates a String representation of this object in a format that can be used for debugging purposes.
    *
    * @return Short String representation of this object
    */
   public String toShortString()
   {
       StringBuilder builder = new StringBuilder();

       ArrayList<String> instanceIDs = GetIDs();
       
       for (int i = 0; i<3; i++)
       {
       		String instanceID = instanceIDs.get(i);
       		builder.append(instanceToString(instanceID) + "\n");
       }

       return builder.toString();
   }
    
    private String instanceToString(String instanceID)
    {
        StringBuilder output = new StringBuilder();
        output.append(instanceID + ":");

        for (String dataPointName : GetDataPointNames())
        {
            String dataPointValue = GetDataPointValue(instanceID, dataPointName);
            
			output.append(dataPointName + "=" + dataPointValue.replace(",", COMMA_REPLACE_STRING) + ",");
        }

        String str = output.toString();
        str = str.substring(0, str.length()-1);
        return str;
    }
}
