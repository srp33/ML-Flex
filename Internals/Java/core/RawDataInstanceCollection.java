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

import java.util.ArrayList;

/** This class is used to store raw data. It allows multiple values per data point and is essentially a wrapper around DataInstanceCollection.
 * @author Stephen Piccolo
 */
public class RawDataInstanceCollection
{
    private DataInstanceCollection _instanceCollection = new DataInstanceCollection();

    /** Adds a data valueToAdd for a given instance to this collection. It takes care of handling multiple values per data point.
     *
     * @param dataPointName Data point name
     * @param instanceID Data instance ID
     * @param valueToAdd Data value to add
     * @return This instance
     */
    public RawDataInstanceCollection Add(String dataPointName, String instanceID, String valueToAdd)
    {
        DataValues instance = new DataValues(instanceID);

        if (_instanceCollection.Contains(instanceID))
            instance = _instanceCollection.Get(instanceID);

        String dataPointValue = valueToAdd;

        if (instance.HasDataPoint(dataPointName))
            dataPointValue = instance.GetDataPointValue(dataPointName) + ";" + valueToAdd;

        instance.AddDataPoint(dataPointName, dataPointValue);

        _instanceCollection.UpdateInstance(instance);

        return this;
    }

    /** Transforms and summarizes all raw data values.
     *
     * @return Transformed, summarized data instance collection
     * @throws Exception
     */
    public DataInstanceCollection GetTransformedSummarizedCollection(AbstractDataProcessor processor) throws Exception
    {
        Singletons.Log.Debug(_instanceCollection.Size() + " raw data instances");
        Singletons.Log.Debug(_instanceCollection.GetNumDataPoints() + " raw data points");

        DataInstanceCollection newCollection = new DataInstanceCollection();

        for (String dataPointName : _instanceCollection.GetDataPointNames())
            for (String instanceID : _instanceCollection.GetIDs())
                newCollection.Add(dataPointName, instanceID, GetTransformedSummarizedValue(dataPointName, instanceID, processor.GetDataPointMeta(dataPointName)));

        Singletons.Log.Debug(_instanceCollection.Size() + " transformed/summarized data instances");
        Singletons.Log.Debug(_instanceCollection.GetNumDataPoints() + " transformed/summarized data points");

        return newCollection;
    }

    /** This methods takes raw data and transforms it then summarizes it.
     *
     * @param dataPointName Data point name
     * @param instanceID Data instance ID
     * @param meta Metadata about the data point
     * @return Transformed and summarized value
     * @throws Exception
     */
    private String GetTransformedSummarizedValue(String dataPointName, String instanceID, DataValueMeta meta) throws Exception
    {
        if (!_instanceCollection.Contains(instanceID))
            return Settings.MISSING_VALUE_STRING;

        ArrayList<String> values = ListUtilities.CreateStringList(_instanceCollection.Get(instanceID).GetDataPointValue(dataPointName).split(";"));

        ArrayList<String> transformedValues = new ArrayList<String>();
        for (String value : values)
            if (!meta.IsNullValue(value))
                transformedValues.add(meta.Transformer.TransformValue(value));

        if (transformedValues.size() == 0)
            return Settings.MISSING_VALUE_STRING;

        if (transformedValues.size() == 1)
            return transformedValues.get(0);

        return meta.Summarizer.Summarize(transformedValues);
    }
}
