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

package mlflex.dataprocessors.tcga;

import mlflex.core.DataValueMeta;
import mlflex.summarization.MeanSummarizer;
import mlflex.transformation.NullTransformer;
import mlflex.helper.BigFileReader;
import mlflex.helper.DataTypeUtilities;
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.io.File;
import java.util.ArrayList;

/** This data processor is designed to parse mRNA expression data from TCGA formatted files; specifically, it parses files in the "Level 3" format, which contains gene-level summarizations. The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaMrnaLevel3DataProcessor extends AbstractTcgaDataProcessor
{
    private final static String INPUT_FILE = "broad.mit.edu__HT_HG-U133A__gene_expression_analysis.txt";

    @Override
    public DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer());
    }
    
    @Override
    protected void ParseInputData() throws Exception
    {
        File file = new File(GetInputDataDir() + INPUT_FILE);
        BigFileReader reader = new BigFileReader(file);
        reader.ReadLine();

        for (String line : reader)
        {
            String[] lineItems = line.split("\t");
            String patientID = FormatColumnName(lineItems[0]);
            String gene = lineItems[1];
            String value = lineItems[2];

            if (DataTypeUtilities.IsDouble(value))
                SaveRawDataPoint(gene, patientID, value);
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
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("PDPN", "AQP1", "CHI3L1", "RTN1", "EMP3", "GPNMB", "IGFBP2", "OLIG2", "LGALS3"));
        //Source: Colman
        //  CHI3L1 = YLK40
    }
}