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

import java.util.*;

/** This class contains helper methods for dealing with Map objects.
 * @author Stephen Piccolo
 */
public class MapUtilities
{
    /** For a list of String values, this method calculates the frequency at which each unique value occurred.
     *
     * @param values List of values to evaluate
     * @return Map between the values and their frequency of occurrence
     */
    public static HashMap<String, Integer> GetFrequencyMap(ArrayList<String> values)
    {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        for (String unique : ListUtilities.GetUniqueValues(values))
        {
            int count = 0;
            for (String value : new ArrayList<String>(values))
                if (value != null && value.equals(unique))
                    count++;
            map.put(unique, count);
        }

        return map;
    }

    /** For a HashMap that has String values as keys and integers as values, this method calculates how many keys have values greater than or equal to the test value.
     *
     * @param map ap with String values as keys and integers as values
     * @param testValue Test value
     * @return Number of keys that have values greater than the specified test value
     */
    public static int GetNumKeysGreaterThanOrEqualTo(HashMap<String, Integer> map, int testValue)
    {
        int count = 0;

        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            int key = Integer.parseInt(entry.getKey());
            if (key >= testValue)
                count += entry.getValue();
        }

        return count;
    }
}
