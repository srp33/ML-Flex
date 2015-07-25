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

package mlflex.helper;

import mlflex.core.NameValuePair;
import mlflex.core.Settings;
import mlflex.core.Singletons;

import java.util.ArrayList;

/** This class helps with saving and retrieving scalar values to/from text files. These files are used for reporting results, etc. */
public class ResultsFileUtilities
{
    /** Saves a column to a text file in a table/matrix format with columns and rows. If the file does not already exist, row names are also added to the file. This method assumes that if a file already exists that the name-value pairs will be in the same order as what is in the file.
     *
     *
     * @param nameValuePairs Name-value pairs that will be saved
     * @param outFilePath Absolute file path to where the results will be saved
     * @param headerComment Descriptive comment that will be placed at the top of the file
     * @throws Exception
     */
    public static void AppendMatrixColumn(ArrayList<NameValuePair> nameValuePairs, String outFilePath, String headerComment) throws Exception
    {
        ArrayList<ArrayList<String>> fileRows = new ArrayList<ArrayList<String>>();

        if (FileUtilities.FileExists(outFilePath))
            fileRows = FileUtilities.ParseDelimitedFile(outFilePath); // File exists so add to it
        else // File doesn't exists so build it from scratch
            for (int i=0; i<nameValuePairs.size(); i++)
                fileRows.add(ListUtilities.CreateStringList(nameValuePairs.get(i).Name));

        // Add new values to the file
        for (int i=0; i<nameValuePairs.size(); i++)
            fileRows.get(i).add(nameValuePairs.get(i).Value);

        // Save the file
        FileUtilities.WriteLinesToFile(outFilePath, fileRows, headerComment);
    }

    /** Adds name-value pairs as rows to a matrix file.
     *
     * @param nameValuePairs List of name-value pairs
     * @param outFilePath Path to where the data will be saved
     * @throws Exception
     */
    public static void AppendMatrixRows(ArrayList<NameValuePair> nameValuePairs, String outFilePath) throws Exception
    {
        StringBuffer out = new StringBuffer();

        for (NameValuePair pair : nameValuePairs)
            out.append(pair.Name + "\t" + pair.Value + "\n");

        FileUtilities.AppendTextToFile(outFilePath, out.toString());
    }

    /** Takes multiple existing matrix files and merges them column-wise into a single matrix file.
     *  @param headerComment Descriptive comment that will be placed at the top of the file
     *  @param inFilePaths A list of input files that will be merged
     *  @param outFilePath Where the output will be stored
     *  @param averageRepeatValues Whether to average all values in a row
     *  @param columnNames Column names that will be added to the header
     */

    public static void CombineMatrixFiles(String headerComment, ArrayList<String> inFilePaths, String outFilePath, boolean averageRepeatValues, ArrayList<String> columnNames) throws Exception
    {
        ArrayList<ArrayList<String>> outFileRows = new ArrayList<ArrayList<String>>();

        for (String inFilePath : inFilePaths)
        {
            // Get the data from an existing file
            ArrayList<ArrayList<String>> inFileRows = FileUtilities.ParseDelimitedFile(inFilePath);
            inFileRows.remove(0);

            // Build a new file if the output file doesn't already exist
            if (outFileRows.size() == 0)
                for (ArrayList<String> inFileRow : inFileRows)
                    outFileRows.add(ListUtilities.Subset(inFileRow, 0, 1));

            // Where applicable, average the values in each row
            if (averageRepeatValues)
                inFileRows = AverageRepeatValues(inFileRows);

            // Add the final values to the output
            for (int i=0; i<inFileRows.size(); i++)
                outFileRows.get(i).addAll(ListUtilities.Subset(inFileRows.get(i), 1));
        }

        // Add column names to the header where applicable
        if (columnNames != null && columnNames.size() > 0)
            outFileRows.add(0, columnNames);

        // Save the file
        FileUtilities.WriteLinesToFile(outFilePath, outFileRows, headerComment);
    }

    /** Averages all values in a row. Sometimes this is desirable in certain reports.
     *
     * @param rows A list of data rows
     * @return Averaged values
     * @throws Exception
     */
    private static ArrayList<ArrayList<String>> AverageRepeatValues(ArrayList<ArrayList<String>> rows) throws Exception
    {
        ArrayList<ArrayList<String>> modRows = new ArrayList<ArrayList<String>>();

        for (ArrayList<String> row : rows)
        {
            ArrayList<String> modRow = ListUtilities.CreateStringList(row.get(0));
            double mean = MathUtilities.Round(MathUtilities.Mean(ListUtilities.CreateDoubleList(ListUtilities.Subset(row, 1))), Settings.RESULTS_NUM_DECIMAL_PLACES);

            modRow.add(DataTypeUtilities.ConvertDoubleToString(mean));
            modRows.add(modRow);
        }

        return modRows;
    }

    /** When an experiment is conducted over multiple iterations, this method retrieves the paths to files specific to each iteration.
     *
     * @param filePathPattern The file pattern to look for
     * @return All matching file paths
     * @throws Exception
     */
    public static ArrayList<String> GetAllIterationFilePaths(String filePathPattern) throws Exception
    {
        ArrayList<String> filePaths = new ArrayList<String>();

        for (int iteration : Singletons.Config.GetIterations())
            filePaths.add(filePathPattern.replace("Iteration" + Singletons.Iteration, "Iteration" + iteration));

        return filePaths;
    }
}
