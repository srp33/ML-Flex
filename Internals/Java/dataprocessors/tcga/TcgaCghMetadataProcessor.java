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

import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.helper.BigFileReader;

import java.util.HashMap;

/** This class supports processing of metadata that describes probes used in Agilent CGH arrays. The metadata describes chromosome positions and gene locations. This metadata file can be downloaded from http://tcga-data.nci.nih.gov/tcga/tcgaPlatformDesign.jsp.
 * @author Stephen Piccolo
 */
public class TcgaCghMetadataProcessor extends AbstractMetadataProcessor
{
    @Override
    protected HashMap<String, String> GetSourceDataMap() throws Exception
    {
        BigFileReader fileReader = new BigFileReader("InputData/Metadata/MSK_CGH.txt");
        fileReader.ReadLines(13);

        HashMap<String, String> dataMap = new HashMap<String, String>();

        for (String line : fileReader)
        {
            String[] lineItems = line.split("\t");
            String probeID = lineItems[8];
            String gene = lineItems[9];

            if (KeepGene(gene))
                dataMap.put(probeID, gene);
        }

        return dataMap;
    }

    @Override
    protected boolean Keep(String key, String value) throws Exception
    {
        return KeepGene(value);
    }

    /** Indicates whether to keep a given gene. Some gene values are ignored because they indicate missing or ambiguous values.
     *
     * @param gene Gene symbol
     * @return Whether to keep the gene
     */
    public static boolean KeepGene(String gene)
    {
        return gene != null && !gene.startsWith("chr") && !gene.contains(":") && !gene.equals("---");
    }
}