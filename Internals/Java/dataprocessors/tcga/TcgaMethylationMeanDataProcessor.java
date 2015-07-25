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
import mlflex.core.Singletons;
import mlflex.summarization.MeanSummarizer;
import mlflex.transformation.NullTransformer;
import mlflex.helper.BigFileReader;
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.util.ArrayList;

/** This data processor is designed to parse DNA methylation data from TCGA formatted files; specifically, it parses files in the "Level 2" format, which contains probe-level values. It calculates the mean across all probes associated with a given gene and uses that as gene-level value. The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaMethylationMeanDataProcessor extends AbstractTcgaDataProcessor
{
    @Override
    protected void ParseInputData() throws Exception
    {
        for (String filePath : GetRawDataFilePaths())
            ProcessInputFile(filePath);
    }

    private ArrayList<String> GetRawDataFilePaths() throws Exception
    {
        ArrayList<String> filePaths = new ArrayList<String>();

        filePaths.add(GetInputDataDir() + "JHU_USC__IlluminaDNAMethylation_OMA002_CPI/Level_2/jhu-usc.edu__IlluminaDNAMethylation_OMA002_CPI__beta-value.txt");
        filePaths.add(GetInputDataDir() + "JHU_USC__IlluminaDNAMethylation_OMA003_CPI/Level_2/jhu-usc.edu__IlluminaDNAMethylation_OMA003_CPI__beta-value.txt");

        return filePaths;
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    private void ProcessInputFile(String betaFilePath) throws Exception
    {
        Singletons.Log.Info("Processing input file " + betaFilePath);

        BigFileReader betaFileReader = new BigFileReader(betaFilePath);

        ArrayList<String> header = ListUtilities.CreateStringList(betaFileReader.ReadLine().split("\t"));
        header.remove(0);

        betaFileReader.ReadLine();

        for (String betaLine : betaFileReader)
        {
            ArrayList<String> betaLineItems = ListUtilities.CreateStringList(betaLine.trim().split("\t"));

            String probe = betaLineItems.remove(0);
            String gene = new TcgaMethylationMetadataProcessor().GetMetadataValue(probe);

            if (gene == null || gene.length() == 0 || gene.equals("."))
                continue;

            ArrayList<String> sampleIDs = new ArrayList<String>(header);

            while (betaLineItems.size() > 0)
            {
                String patientID = FormatColumnName(sampleIDs.remove(0));
                String beta = betaLineItems.remove(0);

                if (!beta.equals("null") && !beta.equals("N/A"))
                    SaveRawDataPoint(gene, patientID, beta);
            }
        }
    }

    @Override
    public DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer());
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("MGMT"));
        // Weller2009,Hegi2005a,Krex2007
    }
}