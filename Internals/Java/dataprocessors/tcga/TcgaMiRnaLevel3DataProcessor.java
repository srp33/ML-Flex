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

package mlflex.dataprocessors.tcga;

import mlflex.core.DataValueMeta;
import mlflex.summarization.MeanSummarizer;
import mlflex.transformation.NullTransformer;
import mlflex.helper.*;

import java.io.File;
import java.util.ArrayList;

/** This data processor is designed to parse microRNA (miRNA) expression data from TCGA formatted files; specifically, it parses files in the "Level 3" format. The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaMiRnaLevel3DataProcessor extends AbstractTcgaDataProcessor
{
    private final static String INPUT_FILE_PATTERN = "unc.edu__H-miRNA*";

    @Override
    public DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer());
    }

    @Override
    protected void ParseInputData() throws Exception
    {
        for (File file : FileUtilities.GetFilesInDirectoryRecursively(GetInputDataDir(), INPUT_FILE_PATTERN))
        {
            BigFileReader reader = new BigFileReader(file);
            reader.ReadLine();

            for (String line : reader)
            {
                String[] lineItems = line.split("\t");
                String patientID = FormatColumnName(lineItems[0]);
                String miRNA = lineItems[1];
                String value = lineItems[2];

                if (DataTypeUtilities.IsDouble(value))
                    SaveRawDataPoint(miRNA, patientID, value);
            }
        }
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("hsa-miR-196a", "hsa-miR-196b"));
        // Guan2010
    }
}