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

import mlflex.core.DataInstanceCollection;
import mlflex.helper.BigFileReader;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** This data processor is designed to parse information from the TCGA somatic-mutation data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaSomaticMutationDataProcessor extends AbstractTcgaDataProcessor
{
    @Override
    protected void ParseInputData() throws Exception
    {
        HashMap<String, ArrayList<String>> genePatientMutations = new HashMap<String, ArrayList<String>>();

        for (File file : FileUtilities.GetFilesInDirectory(GetInputDataDir(), "*level3.maf"))
        {
            BigFileReader reader = new BigFileReader(file);
            ArrayList<String> headerItems = ListUtilities.CreateStringList(reader.ReadLine().trim().split("\t"));

            // Loop through the lines in the file and extract relevant information
            for (String line : reader)
            {
                ArrayList<String> lineItems = ListUtilities.CreateStringList(line.trim().split("\t"));
                String gene = lineItems.get(headerItems.indexOf("Hugo_Symbol"));
                String patientID = FormatColumnName(lineItems.get(headerItems.indexOf("Tumor_Sample_Barcode")));
                String variantClassification = lineItems.get(headerItems.indexOf("Variant_Classification"));
                String validationStatus = lineItems.get(headerItems.indexOf("Validation_Status"));
                String mutationStatus = lineItems.get(headerItems.indexOf("Mutation_Status"));

                // Only focus on certain mutations
                if (!variantClassification.contains("Silent") && validationStatus.equals("Valid") && mutationStatus.equals("Somatic"))
                {
                    ArrayList<String> patientsWithAMutation = ListUtilities.CreateStringList(patientID);
                    if (genePatientMutations.containsKey(gene))
                        patientsWithAMutation.addAll(genePatientMutations.get(gene));
                    genePatientMutations.put(gene, new ArrayList<String>(new HashSet<String>(patientsWithAMutation)));
                }
            }
        }

        ArrayList<String> patientIDs = GetUniquePatientIDs(genePatientMutations);

        // Convert the mutations to binary values for simplicity
        for (String gene : genePatientMutations.keySet())
        {
            for (String patientID : patientIDs)
            {
                String status = "0";
                if (genePatientMutations.get(gene).contains(patientID))
                    status = "1";

                SaveRawDataPoint(gene, patientID, status);
            }
        }
    }

    @Override
    public void UpdateInstancesForAnalysis(DataInstanceCollection instances) throws Exception
    {
        ArrayList<String> dataPointsToRemove = new ArrayList<String>();

        // Remove data points that have less than two mutations across the samples
        for (String dataPoint : instances.GetDataPointNames())
        {
            int mutationCount = ListUtilities.GetNumMatches(instances.GetDataPointValues(dataPoint).GetAllValues(), "1");
            if (mutationCount < 2)
                dataPointsToRemove.add(dataPoint);
        }

        instances.RemoveDataPoints(dataPointsToRemove);
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    private ArrayList<String> GetUniquePatientIDs(HashMap<String, ArrayList<String>> map)
    {
        HashSet<String> patientIDs = new HashSet<String>();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet())
            patientIDs.addAll(entry.getValue());

        return new ArrayList<String>(patientIDs);
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("IDH1", "TP53"));
        //IDH1: Parsons, Bujko2010
        //TP53: Ohgaki2004 (but not independent of age), Schmidt2002
    }
}
