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

/** This data processor class is designed to parse text files in the ARFF format.
 * @author Stephen Piccolo
 */
public class ArffDataProcessor extends AbstractDataProcessor
{
    private String _filePath;
    private String _description;

    /** This constructor accepts a relative path to an ARFF file that will be parsed.
     * @param filePath Relative or absolute path where the file is located (under the InputData directory)
     */
    public ArffDataProcessor(String filePath)
    {
        _filePath = filePath;
        _description = FileUtilities.RemoveFileExtension(_filePath);
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
        int overallInstanceCount = 0;

        ArrayList<String> fileLines = FileUtilities.ReadLinesFromFile(_filePath, "%");
        ArrayList<String> metaRows = ListUtilities.GetValuesStartingWith(fileLines, "@");
        ArrayList<String> dataRows = ListUtilities.RemoveAll(fileLines, metaRows);
        metaRows = ListUtilities.Replace(metaRows, "\t", " ");

        if (dataRows.size() == 0)
            throw new Exception("No data rows could be identified in " + _filePath + ".");

        ArrayList<String> attributeNames = ParseAttributeNames(metaRows, _filePath);

        int idIndex = ListUtilities.ToLowerCase(attributeNames).indexOf("id");

        for (int i=0; i<dataRows.size(); i++)
        {
            overallInstanceCount++;

            ArrayList<String> dataRowItems = ListUtilities.CreateStringList(dataRows.get(i).trim().split(","));
            String instanceID = idIndex == -1 ? "Instance" + overallInstanceCount : dataRowItems.get(idIndex);

            for (int j=0; j<dataRowItems.size(); j++)
                if (j != idIndex)
                    SaveRawDataPoint(attributeNames.get(j), instanceID, dataRowItems.get(j));
        }
    }

    /** Parses attribute names from the metadata rows in an ARFF file.
     *
     * @param metaRows Metadata rows (those that start with @)
     * @param filePath Path where the ARFF file was stored
     * @return Attribute names
     * @throws Exception
     */
    public static ArrayList<String> ParseAttributeNames(ArrayList<String> metaRows, String filePath) throws Exception
    {
        ArrayList<String> attributeNames = new ArrayList<String>();

        for (String metaRow : metaRows)
        {
            ArrayList<String> metaRowItems = ListUtilities.CreateStringList(metaRow.split(" "));
            metaRowItems = ListUtilities.RemoveAll(metaRowItems, " ");

            String descriptor = metaRowItems.get(0).toLowerCase();

            if (!descriptor.equals("@attribute"))
                continue;

            String attributeName = metaRowItems.get(1).trim();

            if (attributeName.equals("class"))
                attributeName = "Class";

            attributeNames.add(attributeName);
        }

        if (attributeNames.size() == 0 || (attributeNames.size() == 1 && attributeNames.get(0).toLowerCase().equals("id")))
            throw new Exception("No attributes could be identified in " + filePath + ".");

        return attributeNames;
    }
}