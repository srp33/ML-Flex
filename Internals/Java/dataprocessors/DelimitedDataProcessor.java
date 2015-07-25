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

import mlflex.core.Settings;
import mlflex.helper.FileUtilities;

import java.util.ArrayList;

/** This class enables the user to import data directly from delimited (for example, by tabs or commas) text files. This class ignores the final column in the file (which it assumes contains the dependent variable values). The default delimiter is a tab.
 * @author Stephen Piccolo
 */
public class DelimitedDataProcessor extends AbstractDataProcessor
{
    private String _filePath;
    private String _delimiter;
    private String _commentChar;
    private String _missingValueCharacter;
    private String _description;

    /** This constructor requires the user to specify a description that will be used and a file path indicating where the data file is stored.
     * @param filePath Relative or absolute path where the delimited file is stored (path is relative to the InputData directory)
     */
    public DelimitedDataProcessor(String filePath)
    {
        this(filePath, "\t", "#", Settings.MISSING_VALUE_STRING);
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param filePath Relative or absolute path where the delimited file is stored (path is relative to the InputData directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     * @param commentChar Comment character (file lines starting with this character will be ignored)
     * @param missingValueCharacter Missing value character (if this character is encountered in the file, it will be considered missing)
     */
    public DelimitedDataProcessor(String filePath, String delimiter, String commentChar, String missingValueCharacter)
    {
        _filePath = filePath;
        _delimiter = delimiter;
        _commentChar = commentChar;
        _missingValueCharacter = missingValueCharacter;
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
        ArrayList<ArrayList<String>> fileLines = FileUtilities.ParseDelimitedFile(_filePath, _delimiter, _commentChar);
        ValidateFileContents(fileLines);

        ArrayList<String> instanceIDs = fileLines.remove(0);

        // Sometimes files don't include a tab or a column description for the row names
        if (instanceIDs.size() == fileLines.get(0).size())
            instanceIDs.remove(0);

        for (int i=0; i<fileLines.size(); i++)
        {
            ArrayList<String> rowValues = fileLines.get(i);
            String dataPointName = rowValues.remove(0);

            for (int j=0; j<rowValues.size(); j++)
            {
                String value = rowValues.get(j);

                if (!value.equals(_missingValueCharacter))
                    SaveRawDataPoint(dataPointName, instanceIDs.get(j), rowValues.get(j));
            }
        }
    }

    /** Checks to make sure a delimited file has a valid structure before it is parsed.
     *
     * @param fileLines Values in each file line
     * @throws Exception
     */
    private void ValidateFileContents(ArrayList<ArrayList<String>> fileLines) throws Exception
    {
        if (fileLines.size() <= 1)
            throw new Exception("The file located at " + _filePath + " has no data.");

        for (int i=1; i<fileLines.size(); i++)
            if (fileLines.get(i).size() != fileLines.get(0).size() && fileLines.get(i).size() != fileLines.get(0).size() + 1)
                throw new Exception("Line " + i + " (after any comment characters were removed) in " + _filePath + " does not have the same number of values as the number of instances.");
    }
}