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

package mlflex.dataprocessors;

import mlflex.core.DataInstanceCollection;
import mlflex.core.DataValues;
import mlflex.core.Settings;
import mlflex.core.Singletons;

import java.util.HashMap;

/** This abstract class coordinates all tasks for processing, storing, and describing metadata. Custom classes can inherit from this class in order to process new metadata.
 * @author Stephen Piccolo
 */
public abstract class AbstractMetadataProcessor
{
    /** This abstract method allows implementing classes provide a mapping between names and values of data points in a generic way. This class stores these names and values.
      * @return Key/value mapping
     * @throws Exception
     */
    protected abstract HashMap<String, String> GetSourceDataMap() throws Exception;

    /** This method can be used to indicate whether a given key/value pair should be retained. By default, all key/value pairs are retained.
     *
     * @param key Key
     * @param value Value
     * @return Whether the key/value pair should be retained
     * @throws Exception
     */
    protected boolean Keep(String key, String value) throws Exception
    {
        return true;
    }

    /** This method provides a description of the data provided by this processor. By default this description is the beginning of the class name.
     *
     * @return Description of the data
     */
    public String GetDescription()
    {
        return this.getClass().getSimpleName().replace("MetadataProcessor", "");
    }

    /** This method performs the work of processing and saving raw data in the ML-Flex format.
     *
     * @throws Exception
     */
    public Boolean Save() throws Exception
    {
        DataValues data = new DataValues(GetDescription());
        data.AddDataPoints(GetSourceDataMap());

        for (String dataPointName : data.GetDataPointNames())
        {
            String value = data.GetDataPointValue(dataPointName);
            if (!Keep(dataPointName, value))
                data.RemoveDataPoint(dataPointName);
        }

        new DataInstanceCollection(data).SerializeToFile(GetDataFilePath()); // Save to file
        DataInstanceCollection.DeserializeFromFile(GetDataFilePath()); // Make sure it worked

        return Boolean.TRUE;
    }

    private DataValues _savedData = null;
    /** This method retrieves metadata that have already been processed and stored by ML-Flex.
     *
     * @return Metadata data values
     * @throws Exception
     */
    protected DataValues GetSavedData() throws Exception
    {
        if (_savedData == null)
            _savedData = DataInstanceCollection.DeserializeFromFile(GetDataFilePath()).Get(0);

        return _savedData;
    }

    /** This method supports retrieving a single metadata value that has already been processed and stored by ML-Flex.
     *
     * @param key Key
     * @return Value
     * @throws Exception
     */
    public String GetMetadataValue(String key) throws Exception
    {
        if (GetSavedData().HasDataPoint(key))
            return GetSavedData().GetDataPointValue(key);

        Singletons.Log.Info("A metadata value with key " + key + " does not exist.");
        return null;
    }

    /** Indicates where on the file system the serialized version of this data should be stored.
     *
     * @return File system location where serialized data are stored
     */
    public String GetDataFilePath()
    {
        return Settings.DATA_DIR + GetDescription() + ".metadata";
    }
}
