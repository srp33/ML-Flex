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

/** This class parses cytogenetic band locations from a UCSC metadata file. This file can be accessed at http://hgdownload.cse.ucsc.edu/downloads.html. Knowing the locations of the cytogenetic bands makes it possible to associate genomic data with specific locations on chromosomes.
 * @author Stephen Piccolo
 */
public class ChromosomeBandMetadataProcessor extends AbstractMetadataProcessor
{
    @Override
    protected HashMap<String, String> GetSourceDataMap() throws Exception
    {
        Singletons.Log.Info("Parsing chromosome band positions");
        HashMap<String, String> positionMap = new HashMap<String, String>();
        for (ArrayList<String> row : FileUtilities.ParseDelimitedFile("InputData/Metadata/cytoBand.txt"))
        {
            String chromosome = row.get(0);
            String startPosition = row.get(1);
            String endPosition = row.get(2);

            String band = row.get(3);
            if (band.contains("."))
                band = band.substring(0, band.indexOf("."));

            positionMap.put(chromosome + band, FormatPosition(startPosition, endPosition));
        }

        return positionMap;
    }

    /** This method identifies chromosomal bands that match the specified start and stop positions.
     *
     * @param queryStartPosition Query start position
     * @param queryStopPosition Query stop position
     * @return Matching chromosomal bands
     * @throws Exception
     */
    public ArrayList<String> GetBandsMatchingPosition(int queryStartPosition, int queryStopPosition) throws Exception
    {
        ArrayList<String> matches = new ArrayList<String>();

        for (String band : GetSavedData())
        {
            String[] positionInfo = ParsePosition(GetSavedData().GetDataPointValue(band));

            int startPosition = Integer.parseInt(positionInfo[0]);
            int stopPosition = Integer.parseInt(positionInfo[1]);

            if ((startPosition > queryStartPosition && stopPosition < queryStopPosition) ||
                    (startPosition < queryStartPosition && stopPosition < queryStopPosition) ||
                    (startPosition < queryStopPosition && stopPosition > queryStopPosition))
                {
                    matches.add(band);
                }
        }

        return matches;
    }

    private String FormatPosition(String startPosition, String endPosition)
    {
        return startPosition + "-" + endPosition;
    }

    private String[] ParsePosition(String rawPosition)
    {
        return rawPosition.trim().split("-");
    }
}