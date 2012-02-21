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

/** This class contains helper methods for performing mathematical operations.
 * @author Stephen Piccolo
 */
public class MathUtilities
{
    /** This method finds the absolute value for each of a list of values.
     *
     * @param values Values for which the absolute value should be found
     * @return Absolute values
     */
    public static ArrayList<Double> GetAbsoluteValues(ArrayList<Double> values)
    {
        ArrayList<Double> absoluteValues = new ArrayList<Double>();

        for (double value : values)
            absoluteValues.add(Math.abs(value));

        return absoluteValues;
    }

    /** Divides one number by another. If the denominator is zero, it returns Double.NaN (not a number).
     *
     * @param numerator Numerator value
     * @param denominator Denominator value
     * @return Divided number
     */
    public static double SmartDivide(double numerator, double denominator)
    {
        if (denominator == 0.0)
            return Double.NaN;

        return numerator / denominator;
    }

//    public static int GetPermutationRank(ArrayList<Double> permutedValues, double actualValue)
//    {
//        int rank = 0;
//
//        for (Double permutedValue : permutedValues)
//            if (permutedValue < actualValue)
//                rank++;
//
//        return rank;
//    }
//
//    public static ArrayList<Integer> Ranks(ArrayList<Double> values)
//    {
//        ArrayList<Double> sortedValues = new ArrayList<Double>(values);
//        Collections.sort(sortedValues);
//
//        ArrayList<Integer> ranks = new ArrayList<Integer>();
//
//        for (int i = 0; i < values.size(); i++)
//            ranks.add(sortedValues.indexOf(values.get(i)));
//
//        return ranks;
//    }

    /** Identifies the quartiles in a list of numbers.
     *
     * @param values Values to be tested
     * @return An array with the lower quartile, median, and upper quartile
     * @throws Exception
     */
    public static double[] Quartiles(ArrayList<Double> values) throws Exception
    {
        if (values.size() < 3)
            throw new Exception("This method is not designed to handle lists with fewer than 3 elements.");

        double median = Median(values);

        ArrayList<Double> lowerHalf = ListUtilities.LessThan(values, median, true);
        ArrayList<Double> upperHalf = ListUtilities.GreaterThan(values, median, true);

        return new double[]{Median(lowerHalf), median, Median(upperHalf)};
    }

//    public static double InterQuartileRange(ArrayList<Double> values) throws Exception
//    {
//        double[] quartiles = Quartiles(values);
//        return quartiles[2] - quartiles[0];
//    }

    /** Indicates whether an integer is odd.
     *
     * @param number Integer value to test
     * @return Whether or not the value is odd
     */
    public static boolean IsOdd(int number)
    {
        return number % 2 == 1;
    }

    /** Returns the log2 value of a number.
     *
     * @param number Numeric value to be transformed
     * @return Transformed value
     */
    public static double Log2(double number)
    {
        return Math.log(number) / Math.log(2);
    }

    /** Returns the log2 value of a list of numbers.
     *
     * @param numbers Numeric values to be transformed
     * @return List of transformed values
     */
    public static ArrayList<Double> Log2(ArrayList<Double> numbers)
    {
        ArrayList<Double> results = new ArrayList<Double>();

        for (double number : numbers)
            results.add(Log2(number));

        return results;
    }

    /** Identifies the maximum numeric value in a list.
     *
     * @param values Numeric values to test
     * @return Maximum value
     * @throws Exception
     */
    public static double Max(ArrayList<Double> values) throws Exception
    {
        ArrayList<Double> values2 = new ArrayList<Double>();
        for (Double value : values)
            if (!value.equals(Double.NaN))
                values2.add(value);

        if (values2.size() == 0)
            throw new Exception("The list was empty, so Max could not be determined.");

        int indexOfMax = 0;

        for (int i = 1; i < values2.size(); i++)
        {
            Double value = values2.get(i);

            if (value > values2.get(indexOfMax))
                indexOfMax = i;
        }

        return values2.get(indexOfMax);
    }

    /** Calculates the mean (average) value from a list of numeric values.
     *
     * @param values List of numeric values
     * @return Mean (average) value
     * @throws Exception
     */
    public static double Mean(ArrayList<Double> values) throws Exception
    {
        if (values.size() == 0)
            throw new Exception("Cannot determine mean on an empty list.");

        return Sum(values) / (double) values.size();
    }

    /** Calculates the median value from a list of numeric values.
     *
     * @param values List of numeric values
     * @return Median value
     * @throws Exception
     */
    public static double Median(ArrayList<Double> values)
    {
        Collections.sort(values);

        if (values.size() % 2 == 1)
            return values.get((values.size() + 1) / 2 - 1);
        else
        {
            double lower = values.get(values.size() / 2 - 1);
            double upper = values.get(values.size() / 2);

            return (lower + upper) / 2.0;
        }
    }

    /** Calculates the minimum value from a list of numeric values.
     *
     * @param values List of numeric values
     * @return Minimum value
     * @throws Exception
     */
    public static double Min(ArrayList<Double> values) throws Exception
    {
        if (values.size() == 0)
            throw new Exception("The list was empty, so Min could not be determined.");

        int indexOfMin = 0;

        for (int i = 1; i < values.size(); i++)
        {
            Double value = values.get(i);

            if (value < values.get(indexOfMin))
                indexOfMin = i;
        }

        return values.get(indexOfMin);
    }

    /** Gets the minimum of two integer values.
     *
     * @param x First value
     * @param y Second value
     * @return Minimum value
     */
    public static int Min(int x, int y)
    {
        if (x < y)
            return x;
        return y;
    }

    /** Rounds each of a list of numeric values to the specified number of decimal places.
     *
     * @param numbers List of numeric values to round
     * @param decimalPlaces Number of decimal places to use
     * @return List of rounded values
     */
    public static ArrayList<Double> Round(ArrayList<Double> numbers, int decimalPlaces)
    {
        for (int i = 0; i < numbers.size(); i++)
            numbers.set(i, Round(numbers.get(i), decimalPlaces));
        return numbers;
    }

    /** Rounds a numeric value to the specified number of decimal places.
     *
     * @param number Numeric value to round
     * @param decimalPlaces Number of decimal places to use
     * @return Rounded value
     */
    public static double Round(double number, int decimalPlaces)
    {
        double modifier = Math.pow(10.0, decimalPlaces);
        return Math.round(number * modifier) / modifier;
    }

    /** Calculates the sum of a list of numeric values.
     *
     * @param values List of numeric values
     * @return Summed value
     */
    public static double Sum(ArrayList<Double> values)
    {
        double sum = 0.0;

        for (double value : values)
            sum += value;

        return sum;
    }

    /** Calculates the sum of a list of boolean values. When the value is true, it is counted as 1.0; otherwise, it is calculated as 0.0.
     *
     * @param values List of boolean values
     * @return Summed value
     */
    public static double SumBooleans(ArrayList<Boolean> values)
    {
        double sum = 0.0;

        for (Boolean value : values)
            if (value)
                sum += 1.0;

        return sum;
    }

    /** Calculates the 2-sample, two-tailed, t-test statistic for two arrays of numeric values. This is a convenience method for calling into the Apache Commons math library.
     *
     * @param array1 First array of values
     * @param array2 Second array of values
     * @return T-test statistic p-value
     * @throws Exception
     */
    public static double TTest(double[] array1, double[] array2) throws Exception
    {
        return org.apache.commons.math.stat.inference.TestUtils.tTest(array1, array2);
    }

    /** Calculates the 2-sample, two-tailed, t-test statistic for two lists of numeric values. This is a convenience method for calling into the Apache Commons math library.
     *
     * @param list1 First array of values
     * @param list2 Second array of values
     * @return T-test statistic p-value
     * @throws Exception
     */
    public static double TTest(ArrayList<Double> list1, ArrayList<Double> list2) throws Exception
    {
        return TTest(ListUtilities.ConvertToDoubleArray(list1), ListUtilities.ConvertToDoubleArray(list2));
    }

    /** Adds the specified value to each element in the list.
     * @param list List of integer values
     * @param amountToAdd Amount to add to each element
     * return New list with modified values
     */
    public static ArrayList<Integer> Add(ArrayList<Integer> list, int amountToAdd)
    {
        ArrayList<Integer> newList = new ArrayList<Integer>();

        for (int i : list)
            newList.add(i + amountToAdd);

        return newList;
    }
}
