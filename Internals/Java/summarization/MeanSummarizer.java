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

import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;

import java.util.*;

/** When multiple values exist for a given data point, this summarizer uses the mean of the values as the summarized value.
 * @author Stephen Piccolo
 */
public class MeanSummarizer extends AbstractSummarizer
{
    @Override
    public String Summarize(ArrayList<String> values) throws Exception
    {
        return String.valueOf(MathUtilities.Mean(ListUtilities.CreateDoubleList(values)));
    }
}
