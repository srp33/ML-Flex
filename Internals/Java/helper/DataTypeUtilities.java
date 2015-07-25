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

import mlflex.core.GeneralDataType;
import mlflex.core.Settings;

import java.util.ArrayList;

/** This class contains helper methods for determining whether String values can be converted to other data types. It also contains methods for performing those conversions.
 * @author Stephen Piccolo
 */
public class DataTypeUtilities
{
    /** Indicates whether a list of String objects only has binary values (0 or 1). Missing values are ignored.
     *
     * @param values List of String objects
     * @return Whether the list only has binary values
     */
    public static boolean HasOnlyBinary(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsBinary(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a String value is binary (contains either a 0 or 1).
     *
     * @param value Value to be tested
     * @return Whether the value is binary
     */
    public static boolean IsBinary(String value)
    {
        return value.equals("0") || value.equals("1");
    }

    /** Indicates whether a list of String objects contains only numeric values. Missing values are ignored.
     *
     * @param values List of values
     * @return Whether the list only contains numeric values
     */
    public static boolean HasOnlyNumeric(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsDouble(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a list of String objects contains only integer values. Missing values are ignored.
     *
     * @param values List of values
     * @return Whether the list only contains integer values
     */
    public static boolean HasOnlyIntegers(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsInteger(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a String value contains either "true" or "false."
     *
     * @param value Value to be tested.
     * @return Whether the value contains either "true" or "false."
     */
    public static boolean IsBoolean(String value)
    {
        return value.toLowerCase().equals("true") || value.toLowerCase().equals("false");
    }

    /** Indicates whether a String value can be converted to a double value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted to a double
     */
    public static boolean IsDouble(String value)
    {
        try
        {
            Double.parseDouble(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /** Indicates whether a String value can be converted to an integer value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted to an integer
     */
    public static boolean IsInteger(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

     /** Indicates whether a String value can be converted to either a double value or an integer value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted either to a double value or an integer value
     */
    public static boolean IsNumeric(String value)
    {
        return IsDouble(value) || IsInteger(value);
    }

    /** For a set of values, this gets the "general" data type of the values.
     *
     * @param values Values to be tested
     * @return General data type of the values
     */
    public static GeneralDataType GetGeneralDataType(ArrayList<String> values)
    {
        if (HasOnlyBinary(values))
            return GeneralDataType.Binary;

        if (HasOnlyNumeric(values))
            return GeneralDataType.Continuous;

        return GeneralDataType.Nominal;
    }

    /** Converts a double object to a string after rounding it.
     *
     * @param value Value to be converted
     * @return Converted and formatted value
     */
    public static String ConvertDoubleToString(double value)
    {
        if (Math.floor(value) == value)
            return String.valueOf(value);
        else
            return String.valueOf(MathUtilities.Round(value, Settings.RESULTS_NUM_DECIMAL_PLACES));
    }
}
