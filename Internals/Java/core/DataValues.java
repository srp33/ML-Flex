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
import mlflex.helper.NumericString;
import mlflex.helper.MiscUtilities;

import java.util.*;

/** This class is designed to store key/value pairs and to provide convenience methods for dealing with key/value pairs.
 * @author Stephen Piccolo
 */
public class DataValues implements Iterable<String>, Comparable
{
    private String _id;
    private HashMap<String, String> _dataPoints = new HashMap<String, String>();
    private static String COMMA_REPLACE_STRING = "_comma_";

    /** Constructor.
     *
     * @param id Identifier describing the values in this object
     */
    public DataValues(String id)
    {
        SetID(id);
    }

    /** Adds a data point value.
     *
     * @param name Data point name
     * @param value Data point value
     * @return This instance after the data point has been added
     */
    public DataValues AddDataPoint(String name, String value)
    {
        _dataPoints.put(name, value);
        return this;
    }

    /** Adds data points from an existing instance to this instance.
     *
     * @param instance Data instance to be added
     */
    public void AddDataPoints(DataValues instance)
    {
        AddDataPoints(instance._dataPoints);
    }

    /** Adds data points from a map that contains key/value pairs.
     *
     * @param dataPoints Data point map
     */
    public void AddDataPoints(AbstractMap<String, String> dataPoints)
    {
        for (String key : dataPoints.keySet())
            AddDataPoint(key, dataPoints.get(key));
    }

    /** Adds a binary data point to this instance. If the value is the same as the "one option" value, it is marked as 1; otherwise, it is marked as 0.
     *
     * @param name Data point name
     * @param value Raw data point value
     * @param oneOption One option
     */
    public void AddBinaryDataPoint(String name, String value, String oneOption)
    {
        if (MiscUtilities.IsMissing(value))
            AddDataPoint(name, Settings.MISSING_VALUE_STRING);
        else
            AddDataPoint(name, value.equals(oneOption) ? "1" : "0");
    }

    /** Adds a binary data point to this instance. Multiple values can be specified, and if any of them are the same as the "one option" value, the data point is marked as 1; otherwise, it is marked as 0.
     *
     * @param name Data point name
     * @param values Data point values
     * @param oneOption One option
     */
    public void AddBinaryDataPoint(String name, ArrayList<String> values, String oneOption)
    {
        if (values == null)
            AddDataPoint(name, Settings.MISSING_VALUE_STRING);

        AddDataPoint(name, values.contains(oneOption) ? "1" : "0");
    }

    /** Removes all data point values.
     *
     * @return This instance without any data point values specified
     */
    public DataValues ClearDataPoints()
    {
        _dataPoints.clear();
        return this;
    }

    /** Creates a deep copy of this instance.
     *
     * @return Deep copy of this instance
     */
    public DataValues Clone()
    {
        DataValues newObj = CopyStructure();
        newObj._dataPoints = new HashMap<String, String>(_dataPoints);

        return newObj;
    }

    /** Creates a shallow copy of this instance (the data names and values are not copied).
     *
     * @return Shallow copy of this instance
     */
    public DataValues CopyStructure()
    {
        DataValues newObj = new DataValues(this.GetID());
        return newObj;
    }

    /** Formats all data point names.
     *
     */
    public void FormatDataPointNames()
    {
        for (String dataPointName : GetDataPointNames())
            UpdateDataPointName(dataPointName, MiscUtilities.FormatName(dataPointName));
    }

    /** Indicates whether this instance has a data point with the specified name.
     *
     * @param dataPointName Data point name
     * @return Whether this instance has a data point with the specified name
     */
    public boolean HasDataPoint(String dataPointName)
    {
        return _dataPoints.containsKey(dataPointName);
    }

    /** Indicates whether this instance has only missing values.
     *
     * @return Whether this instance has only missing values
     */
    public boolean HasOnlyMissingValues()
    {
        for (String dataPoint : this)
            if (!GetDataPointValue(dataPoint).equals(Settings.MISSING_VALUE_STRING))
                return false;

        return true;
    }

    /** Indicates the number of data points in this instance.
     *
     * @return Number of data points in this instance
     */
    public int GetNumDataPoints()
    {
        return GetDataPointNames().size();
    }

    /** Gets the data point value at the specified index.
     *
     * @param index Index
     * @return Data point value
     */
    public String GetDataPointValue(int index)
    {
        return GetAllValues().get(index);
    }

    /** Gets a list of all data point values.
     *
     * @return List of all data point values
     */
    public ArrayList<String> GetAllValues()
    {
        return new ArrayList<String>(_dataPoints.values());
    }

    /** Gets a list of all data point names in this instance.
     *
     * @return List of all data point names
     */
    public ArrayList<String> GetDataPointNames()
    {
        return new ArrayList<String>(_dataPoints.keySet());
    }

    /** Gets the data point value for specified data point.
     *
     * @param name Data point name
     * @return Data point value
     */
    public String GetDataPointValue(String name)
    {
        String value = _dataPoints.get(name);
        return value == null ? Settings.MISSING_VALUE_STRING : value;
    }

    /** Gets the data point values corresponding to the specified data point names.
     *
     * @param names Data point names
     * @return Data point values
     */
    public ArrayList<String> GetDataPointValues(ArrayList<String> names)
    {
        ArrayList<String> values = new ArrayList<String>();

        for (String name : names)
            values.add(GetDataPointValue(name));

        return values;
    }

    /** Gets the ID associated with this instance.
     *
     * @return ID
     */
    public String GetID()
    {
        return _id;
    }

    /** Indicates the number of non-missing values in this data instance.
     *
     * @return Number of non-missing values
     */
    public int GetNumNotMissingValues()
    {
        int notMissing = 0;

        for (String value : _dataPoints.values())
            if (value != null && !value.equals(Settings.MISSING_VALUE_STRING))
                notMissing++;

        return notMissing;
    }

    /** Removes the data point value with the specified name.
     *
     * @param name Data point name
     */
    public void RemoveDataPoint(String name)
    {
        _dataPoints.remove(name);
    }

    /** Replaces any missing values with the specified replacement value.
     *
     * @param newValue Replacement value
     * @return This instance after replacement has occurred
     */
    public DataValues ReplaceMissingValues(String newValue)
    {
        for (String dataPointName : this)
        {
            String value = GetDataPointValue(dataPointName);
            if (value.equals(Settings.MISSING_VALUE_STRING))
                value = newValue;
            UpdateDataPoint(dataPointName, value);
        }

        return this;
    }

    /** Changes the ID value for this instance.
     *
     * @param id New ID
     */
    public void SetID(String id)
    {
        _id = id;
    }

    /** Indicates the number of data points in this instance.
     *
     * @return Number of data points in this instance
     */
    public int Size()
    {
        return _dataPoints.size();
    }

    /** Updates a data point value
     *
     * @param name Data point name
     * @param value New data point value
     * @return This instance after it has been updated
     */
    public DataValues UpdateDataPoint(String name, String value)
    {
        return AddDataPoint(name, value);
    }

    /** Updates a data point name
     *
     * @param fromDataPointName Current data point name
     * @param toDataPointName New data point name
     */
    public void UpdateDataPointName(String fromDataPointName, String toDataPointName)
    {
        String value = _dataPoints.get(fromDataPointName);

        if (value == null)
            return;

        _dataPoints.remove(fromDataPointName);
        _dataPoints.put(toDataPointName, value);
    }

    public Iterator<String> iterator()
    {
        return _dataPoints.keySet().iterator();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof DataValues))
            return false;
        DataValues compareObj = (DataValues) obj;
        return compareObj.GetID().equals(this.GetID());
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 29 * hash + (this._id != null ? this._id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();
        output.append(GetID() + ":");

        for (String dataPointName : GetDataPointNames())
            output.append(dataPointName + "=" + GetDataPointValue(dataPointName).replace(",", COMMA_REPLACE_STRING) + ",");

        String str = output.toString();
        str = str.substring(0, str.length()-1);
        return str;
    }

    /** This method is used to generate a short representation of the data values, which can be used for debugging purposes.
     *
     * @return Short String representation of the data values
     */
    public String toShortString()
    {
        StringBuilder output = new StringBuilder();
        output.append(GetID() + ":");

        ArrayList<String> dataPointsNames = GetDataPointNames();
        if (dataPointsNames.size() > 5)
            dataPointsNames = ListUtilities.Subset(dataPointsNames, 0, 5);

        for (String dataPointName : dataPointsNames)
            output.append(dataPointName + "=" + GetDataPointValue(dataPointName) + ",");

        String str = output.toString();
        str = str.substring(0, str.length()-1);
        return str;
    }

    /** Converts a String representation of this object to an instance of this class.
     *
     * @param text String representation
     * @return Data instance
     */
    public static DataValues FromString(String text)
    {
        if (text.equals(DataInstanceCollection.END_OF_FILE_MARKER))
            return new DataValues(DataInstanceCollection.END_OF_FILE_MARKER);

        String[] parts = text.split(":");
        DataValues instance = new DataValues(parts[0]);

        for (String pair : ListUtilities.CreateStringList(parts[1].split(",")))
        {
            String[] pairParts = pair.split("=");
            instance.AddDataPoint(pairParts[0], pairParts[1].replace(COMMA_REPLACE_STRING, ","));
        }

        return instance;
    }

    public int compareTo(Object obj)
    {
        DataValues compareObj = (DataValues) obj;

        NumericString thisID = new NumericString(this.GetID());
        NumericString compareID = new NumericString(compareObj.GetID());

        return thisID.compareTo(compareID);
    }
}
