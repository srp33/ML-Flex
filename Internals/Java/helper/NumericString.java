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

/** This class was designed with the purpose of allowing strings to be sorted properly when they contain numeric values. For example, suppose you had three String objects that contained the values "1", "2", and "10". If they were sorted as strings, they would be sorted as "1", "10", "2," which is often not the desired behavior. If they are sorted using this class, they will be sorted as "1", "2", "10."
 * @author Stephen Piccolo
 */
public class NumericString implements Comparable
{
    private String _rawValue;

    /** Constructor
     *
     * @param rawValue Raw string value that contains a number
     */
    public NumericString(String rawValue)
    {
        _rawValue = rawValue;
    }

    private static int CompareTwoVals(String xVal, String yVal)
    {
        if (DataTypeUtilities.IsDouble(xVal) && DataTypeUtilities.IsDouble(yVal))
            return new Double(Double.parseDouble(xVal)).compareTo(Double.parseDouble(yVal));
        if (!DataTypeUtilities.IsDouble(xVal) && !DataTypeUtilities.IsDouble(yVal))
            return xVal.compareTo(yVal);
        if (DataTypeUtilities.IsDouble(xVal))
            return -1;
        return 1;
    }

    private ArrayList<String> GetValueList()
    {
        ArrayList<String> valueList = new ArrayList<String>();
        String tempVal = "";

        for (int i = 0; i < _rawValue.length(); i++)
        {
            String val = _rawValue.substring(i, i + 1);

            if (DataTypeUtilities.IsDouble(val))
                tempVal += val;
            else
            {
                if (!tempVal.equals(""))
                {
                    valueList.add(tempVal);
                    tempVal = "";
                }

                valueList.add(val);
            }
        }

        if (!tempVal.equals(""))
            valueList.add(tempVal);

        return valueList;
    }

    public int compareTo(Object obj)
    {
        if (obj == null)
            return -1;

        NumericString compareObj = (NumericString) obj;

        if (this._rawValue == null) return -1;
        if (compareObj._rawValue == null) return 1;
        if (this._rawValue.equals(compareObj._rawValue)) return 0;

        ArrayList<String> xList = this.GetValueList();
        ArrayList<String> yList = compareObj.GetValueList();

        for (int i = 0; i < xList.size() && i < yList.size(); i++)
        {
            int compareResult = CompareTwoVals(xList.get(i), yList.get(i));

            if (compareResult != 0)
                return compareResult;
        }

        return new Integer(xList.size()).compareTo(yList.size());
    }

    @Override
    public String toString()
    {
        return _rawValue;
    }
}
