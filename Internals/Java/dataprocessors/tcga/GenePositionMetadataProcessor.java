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

import mlflex.core.Singletons;
import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.helper.FileUtilities;

import java.util.ArrayList;
import java.util.HashMap;

/** This class parses gene locations from a UCSC metadata file. This file can be accessed at http://hgdownload.cse.ucsc.edu/downloads.html. Knowing the locations of the genes makes it possible to associate genomic data with specific genes.
 * @author Stephen Piccolo
 */
public class GenePositionMetadataProcessor extends AbstractMetadataProcessor
{
    @Override
    protected HashMap<String, String> GetSourceDataMap() throws Exception
    {
        Singletons.Log.Info("Parsing known gene positions");
        HashMap<String, String> positionMap = new HashMap<String, String>();
        for (ArrayList<String> row : FileUtilities.ParseDelimitedFile("InputData/Metadata/knownGene.txt"))
        {
            String ucscID = row.get(0);
            String chromosome = row.get(1).replace("chr", "");
            String startPosition = row.get(3);
            String endPosition = row.get(4);

            positionMap.put(ucscID, FormatPosition(chromosome, startPosition, endPosition));
        }

        Singletons.Log.Info("Parsing gene names");
        ArrayList<ArrayList<String>> hgnc = FileUtilities.ParseDelimitedFile("InputData/Metadata/hgnc.txt");
        hgnc.remove(0);

        HashMap<String, String> genePositionList = new HashMap<String, String>();

        for (ArrayList<String> row : hgnc)
        {
            if (row.size() < 3)
                continue;

            String geneName = row.get(0);
            String ucscID = row.get(2);
            String position = positionMap.get(ucscID);

            if (position != null)
                genePositionList.put(geneName, position);
        }

        return genePositionList;
    }

    /** Searches the metadata values for a gene matching the query positions.
     *
     * @param queryChromosome Chromosome that is being searched for
     * @param queryStartPosition Query start position
     * @param queryStopPosition Query stop position
     * @return Genes that correspond with the position
     * @throws Exception
     */
    public ArrayList<String> GetGenesMatchingPosition(String queryChromosome, int queryStartPosition, int queryStopPosition) throws Exception
    {
        ArrayList<String> matches = new ArrayList<String>();

        for (String gene : GetSavedData())
        {
            String[] positionInfo = ParsePosition(GetSavedData().GetDataPointValue(gene));

            String chromosome = positionInfo[0];
            int startPosition = Integer.parseInt(positionInfo[1]);
            int stopPosition = Integer.parseInt(positionInfo[2]);

            if (chromosome.equals(queryChromosome))
            {
                if ((startPosition > queryStartPosition && stopPosition < queryStopPosition) ||
                    (startPosition < queryStartPosition && stopPosition < queryStopPosition) ||
                    (startPosition < queryStopPosition && stopPosition > queryStopPosition))
                {
                    matches.add(gene);
                }
            }
        }

        return matches;
    }

    private String FormatPosition(String chromosome, String startPosition, String endPosition)
    {
        return chromosome + "-" + startPosition + "-" + endPosition;
    }

    private String[] ParsePosition(String rawPosition)
    {
        return rawPosition.trim().split("-");
    }
}