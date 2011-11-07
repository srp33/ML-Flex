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

import java.util.ArrayList;

/** When multiple values are specified for a single data point, this summarizer uses the maximum absolute value as the summarized value.
 * @author Stephen Piccolo
 */
public class MaxAbsoluteValueSummarizer extends AbstractSummarizer
{
    @Override
    public String Summarize(ArrayList<String> values) throws Exception
    {
        double maxAbsoluteValue = MathUtilities.Max(MathUtilities.GetAbsoluteValues(ListUtilities.CreateDoubleList(values)));

        for (String value : values)
        {
            double doubleValue = Double.parseDouble(value);

            if (Math.abs(doubleValue) == maxAbsoluteValue)
                return value;
        }

        return "0.0";
    }
}