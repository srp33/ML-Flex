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

package mlflex.summarization;

import java.util.*;

/** When multiple values for a given data point have been recorded, this class supports generic functionality for dealing with those multiple values. This functionality may be most often used for bioinformatics/genomics data sets for which technical replaces are sometimes obtained. Classes that inherit from this class implement custom functionality for handling multiple values per data point.
 * @author Stephen Piccolo
 */
public abstract class AbstractSummarizer
{
    /** Methods inheriting from this class are designed to determine what to do when multiple values per data point have been recorded.
     *
     * @param values Data values
     * @return Summarized single value
     * @throws Exception
     */
    public abstract String Summarize(ArrayList<String> values) throws Exception;
}
