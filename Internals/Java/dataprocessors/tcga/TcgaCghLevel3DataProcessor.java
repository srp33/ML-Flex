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
import mlflex.parallelization.MultiThreadedTaskHandler;
import mlflex.summarization.MeanSummarizer;
import mlflex.transformation.NullTransformer;
import mlflex.helper.DataTypeUtilities;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/** This data processor parses gene-level data for Agilent CGH arrays for data from The Cancer Genome Atlas. This data can be downloaded from The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaCghLevel3DataProcessor extends AbstractTcgaDataProcessor
{
    private final static String INPUT_FILE = "mskcc.org__HG-CGH-244A__copy_number_analysis.txt";

    @Override
    public DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer(), "NA");
    }

    @Override
    protected void ParseInputData() throws Exception
    {
        final GenePositionMetadataProcessor metadataProcessor = new GenePositionMetadataProcessor();

        final ArrayList<String> fileLines = FileUtilities.ReadLinesFromFile(GetInputDataDir() + INPUT_FILE);
        fileLines.remove(0);

        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler("parse raw data for " + GetDescription());

        for (final String line : fileLines)
        {
            taskHandler.Add(new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    String[] lineItems = line.split("\t");
                    String patientID = FormatColumnName(lineItems[0]);
                    String chromosome = lineItems[1];
                    int startPosition = Integer.parseInt(lineItems[2]);
                    int stopPosition = Integer.parseInt(lineItems[3]);
                    String value = lineItems[5];

                    if (DataTypeUtilities.IsDouble(value))
                        for (String geneName : metadataProcessor.GetGenesMatchingPosition(chromosome, startPosition, stopPosition))
                            SaveRawDataPoint(geneName, patientID, value);

                    return null;
                }
            });
        }

        taskHandler.Execute();
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return ListUtilities.CreateStringList("MDM2", "EGFR", "PTEN", "CDKN2A");
        //CDK4 (12q): Ruano2009
        //MDM2 (12q): Houillier2006, Schiebe2000
        //7: Korshunov2005
        //EGFR (7p): Houillier2006,Korshunov2005 (only for patients < 50 years), Shinojima2003, Smith2001 (only for older patients)
        //6q, 10p, 10q, 19p, 19q, 20q (Burton2002)
        //p16/CDKN2A deletion (9p): Rasheed2002 (also included AA), Korshunov2005
        //9: Korshunov2005
        //PTEN (10q23): Sano1999, Korshunov2005
        //SKP2 (5p): Saigusa
        //1p: Homma2006
        //10q: Homma2006
        //19: Korshunov2005
    }
}