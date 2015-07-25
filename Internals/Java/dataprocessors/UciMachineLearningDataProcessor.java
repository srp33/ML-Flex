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

import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.util.ArrayList;

/** This class contains functionality for handling data that comes from the UCI machine learning repository. */
public class UciMachineLearningDataProcessor extends AbstractDataProcessor
{
    private String _filePath;
    private int _idIndex;
    private int _classIndex;
    private String _delimiter;
    private String _description;

    /** Constructor
     * @param filePath Relative or absolute path to file containing data
     * @param idIndex Index of column containing identifiers that identify each data instance (when none exists, this should be -1)
     * @param classIndex Index of column containing class information
     */
    public UciMachineLearningDataProcessor(String filePath, Integer idIndex, Integer classIndex)
    {
        this(filePath, idIndex, classIndex, ",");
    }

    /** Constructor
     * @param filePath Relative or absolute path to file containing data
     * @param idIndex Index of column containing identifiers that identify each data instance (when none exists, this should be -1)
     * @param classIndex Index of column containing class information
     * @param delimiter Character/string that is used in the file to delimit entries
     */
    public UciMachineLearningDataProcessor(String filePath, Integer idIndex, Integer classIndex, String delimiter)
    {
        _filePath = filePath;
        _idIndex = idIndex;
        _classIndex = classIndex;
        _delimiter = delimiter.equals("") ? " " : delimiter;
        _description = FileUtilities.RemoveFileExtension(filePath);
    }

    @Override
    public String GetDescription()
    {
        return _description;
    }

    @Override
    protected double GetProportionMissingPerInstanceOK()
    {
        return 0.99;
    }

    @Override
    protected double GetProportionMissingPerDataPointOK()
    {
        return 0.99;
    }

    @Override
    protected void ParseInputData() throws Exception
    {
        ArrayList<ArrayList<String>> data = FileUtilities.ParseDelimitedFile(_filePath);
        for (int i=0; i<data.size(); i++)
        {
            ArrayList<String> row = GetRow(data, i);
            String instanceID = GetID(i, row);

            for (int j=0; j<row.size(); j++)
            {
                if (j == _idIndex)
                    continue;

                if (j == _classIndex)
                    SaveRawDataPoint("Class", instanceID, row.get(j));
                else
                    SaveRawDataPoint("DataPoint" + j, instanceID, row.get(j));
            }
        }
    }

    private ArrayList<String> GetRow(ArrayList<ArrayList<String>> data, int i)
    {
        return ListUtilities.CreateStringList(data.get(i).get(0).split(_delimiter));
    }

    private String GetID(int i, ArrayList<String> rowValues)
    {
        return _idIndex == -1 ? "ID" + i : rowValues.get(_idIndex);
    }
}