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

import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.helper.BigFileReader;
import mlflex.helper.ListUtilities;

import java.util.HashMap;

/** This data processor is designed to parse metadata information to support parsing of mRNA expression data in TCGA; specifically, it supports a conversion of probeset identifiers to gene symbols. The .csv file can be downloaded from Affymetrix.com. The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code can be tweaked to work with different Affymetrix arrays if needed.
 * @author Stephen Piccolo
 */
public class TcgaMrnaMetadataProcessor extends AbstractMetadataProcessor
{
    @Override
    protected HashMap<String, String> GetSourceDataMap() throws Exception
    {
        BigFileReader fileReader = new BigFileReader("InputData/Metadata/HG-U133A.na30.annot.csv");

        // Remove all the header lines
        fileReader.ReadLines(28);

        HashMap<String, String> dataMap = new HashMap<String, String>();

        for (String line : fileReader)
        {
            String[] lineItems = line.split("\",\"");
            String probeID = lineItems[0].replaceAll("\"", "");
            String gene = lineItems[14].replaceAll("\"", "").replaceAll(" /// ", "_");

            dataMap.put(probeID, gene);
        }

        return dataMap;
    }

    @Override
    protected boolean Keep(String key, String value) throws Exception
    {
        return !ListUtilities.CreateStringList("", "---").contains(value);// && GetGoGenes().contains(value);
    }
}