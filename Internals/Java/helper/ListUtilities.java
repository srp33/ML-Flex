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

/** This class contains helper methods for dealing with Java collections and lists. Many of these implement functionality that circumvents some of the awkwardness and verbostity of dealing with lists in Java.
 * @author Stephen Piccolo
 */
public class ListUtilities
{
    public static final ArrayList<String> ALPHABET = ListUtilities.CreateStringList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    /** Appends a String value to the end of an existing String array (warning: can be CPU intensive because a new array object is recreated each time).
     *
     * @param array String array object
     * @param newValue Value to be added
     * @return New String array object
     */
    public static String[] AppendItemToArray(String[] array, String newValue)
    {
        ArrayList<String> list = CreateStringList(array);
        list.add(array.length, newValue);
        return ConvertToStringArray(list);
    }

    /** Appends a String value to the end of an existing String array (warning: can be CPU intensive because a new array object is recreated each time) at a given index position.
     *
     * @param array String array object
     * @param newValue Value to be added
     * @param position Index position to where it should be added
     * @return New String array object
     */
    public static String[] AddItemToArray(String[] array, String newValue, int position)
    {
        ArrayList<String> list = CreateStringList(array);
        list.add(position, newValue);
        return ConvertToStringArray(list);
    }

    public static ArrayList<String> AppendStringToListItems(ArrayList<String> list, String suffix)
    {
        ArrayList<String> newList = new ArrayList<String>();

        for (String item : list)
            newList.add(item + suffix);

        return newList;
    }

    /** Creates a list of integers from an array of integers.
     *
     * @param values Integer values
     * @return List of integers
     */
    public static ArrayList<Integer> CreateIntegerList(Integer... values)
    {
        ArrayList<Integer> results = new ArrayList<Integer>();
        if (values.length > 0)
            Collections.addAll(results, values);

        return results;
    }

    /** Converts a list of Strings that contain integer-compatible values to a list of Integers.
     *
     * @param values List of String values
     * @return List of Integer values
     */
    public static ArrayList<Integer> CreateIntegerList(ArrayList<String> values)
    {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (String value : values)
            list.add(Integer.parseInt(value));

        return list;
    }

    /** Creates a list of integers starting at one value and ending at another value (increment step is one).
     *
     * @param startNumber Start number
     * @param endNumber End number
     * @return List of Integer values
     */
    public static ArrayList<Integer> CreateIntegerSequenceList(int startNumber, int endNumber)
    {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i=startNumber; i<=endNumber; i++)
            list.add(i);

        return list;
    }

    /** Creates a list of objects from an array of objects.
     *
     * @param values Array of objects
     * @return List of objects
     */
    public static ArrayList CreateObjectList(Object... values)
    {
        ArrayList results = new ArrayList();
        Collections.addAll(results, values);
        return results;
    }

    /** Converts a list of Double objects to a list of String objects. If a Double object is Double.NaN, it is specified as "NA."
     *
     * @param values List of double values
     * @return Converted list
     */
    public static ArrayList<String> CreateStringListFromDoubleList(ArrayList<Double> values)
    {
        ArrayList<String> stringList = new ArrayList<String>();

        for (Double value : values)
            if (value.equals(Double.NaN))
                stringList.add("NA");
            else
                stringList.add(String.valueOf(value));
        
        return stringList;
    }

    /** Converts a list of Integer objects to a list of String objects. If an Integer object is Integer.MIN_VALUE, it is specified as "NA."
     *
     * @param values List of Integer values
     * @return Converted list
     */
    public static ArrayList<String> CreateStringListFromIntegerList(ArrayList<Integer> values)
    {
        ArrayList<String> stringList = new ArrayList<String>();

        for (Integer value : values)
            if (value.equals(Integer.MIN_VALUE))
                stringList.add("NA");
            else
                stringList.add(String.valueOf(value));

        return stringList;
    }

    /** Converts an array of objects to a list of String objects.
     *
     * @param values Array of objects
     * @return List of String objects
     */
    public static ArrayList<String> CreateStringList(Object... values)
    {
        return CreateStringList(CreateObjectList(values));
    }

    /** Converts an array of String objects to a list of String objects.
     *
     * @param values Array of String objects
     * @return List of String objects
     */
    public static ArrayList<String> CreateStringList(String... values)
    {
        ArrayList<String> results = new ArrayList<String>();
        if (values.length > 0)
            Collections.addAll(results, values);
        return results;
    }

    /** This method iterates over multiple lists of String objects and creates a single list of String objects containing the contents of all.
     *
     * @param lists Array of String lists
     * @return Combined list
     */
    public static ArrayList<String> CreateStringList(ArrayList<String>... lists)
    {
        ArrayList<String> all = new ArrayList<String>();

        for (ArrayList<String> list : lists)
            all.addAll(list);

        return new ArrayList<String>(all);
    }

    /** Converts a generic collection of objects into a list of String objects.
     *
     * @param values Collection of objects
     * @return List of String objects
     */
    public static ArrayList<String> CreateStringList(Collection values)
    {
        ArrayList<String> results = new ArrayList<String>();

        for (Object value : values)
            results.add(String.valueOf(value));

        return results;
    }

    /** Converts a list of objects to a list of boolean objects.
     *
     * @param list List of objects
     * @return List of boolean objects
     * @throws Exception
     */
    public static ArrayList<Boolean> CreateBooleanList(ArrayList list) throws Exception
    {
        ArrayList<Boolean> booleans = new ArrayList<Boolean>();

        for (Object x : list)
            booleans.add((Boolean)x);

        return booleans;
    }

    /** Creates a list of Boolean objects from an array of boolean values.
     *
     * @param values An array of boolean values
     * @return List of boolean objects
     * @throws Exception
     */
    public static ArrayList<Boolean> CreateBooleanList(boolean ... values) throws Exception
    {
        ArrayList<Boolean> booleans = new ArrayList<Boolean>();

        for (boolean value : values)
            booleans.add(value);

        return booleans;
    }

    /** Creates a list of double objects from an array of double objects.
     *
     * @param values Array of double objects
     * @return List of double objects
     */
    public static ArrayList<Double> CreateDoubleList(double... values)
    {
        ArrayList<Double> results = new ArrayList<Double>();

        for (double d : values)
            results.add(d);
        return results;
    }

    /** Converts a list of double-compatible String objects to a list of double objects.
     *
     * @param values List of String values
     * @return List of double values
     */
    public static ArrayList<Double> CreateDoubleList(ArrayList<String> values)
    {
        ArrayList<Double> doubles = new ArrayList<Double>();

        for (String value : values)
            if (DataTypeUtilities.IsDouble(value))
                doubles.add(Double.parseDouble(value));

        return doubles;
    }

    /** Creates a list of double objects.
     *
     * @param value Value to add to list
     * @param repeatTimes Number of times to add value to list
     * @return List of double values
     */
    public static ArrayList<Double> CreateDoubleList(double value, int repeatTimes)
    {
        ArrayList<Double> list = new ArrayList<Double>();

        for (int i=0; i<repeatTimes; i++)
            list.add(value);

        return list;
    }

    /** Creates a list of String objects.
     *
     * @param value Value to add to list
     * @param numRepetitions Number of times to add value to list
     * @return List of String values
     */
    public static ArrayList<String> CreateStringList(String value, int numRepetitions)
    {
        ArrayList<String> list = new ArrayList<String>();

        for (int i = 0; i < numRepetitions; i++)
            list.add(value);

        return list;
    }

    /** Converts a list of String objects to an array of String objects.
     *
     * @param collection List of String objects
     * @return Array of String objects
     */
    public static String[] ConvertToStringArray(ArrayList<String> collection)
    {
        return collection.toArray(new String[0]);
    }

    /** Converts a list of double objects to an array of Double objects.
     *
     * @param collection List of double objects
     * @return Array of double objects
     */
    public static double[] ConvertToDoubleArray(ArrayList<Double> collection)
    {
        Double[] rawArray = collection.toArray(new Double[0]);

        double[] result = new double[rawArray.length];

        for (int i = 0; i < rawArray.length; i++)
            result[i] = rawArray[i];

        return result;
    }

    /** Identifies all unique String values in a list.
     *
     * @param values List of String values
     * @return Unique values
     */
    public static ArrayList<String> GetUniqueValues(ArrayList<String> values)
    {
        return new ArrayList<String>(new HashSet<String>(values));
    }

    /** Returns a random subset of values from the specified list.
     *
     * @param list List to be examined
     * @param numValues
     * @param random Random number generator
     * @return Random subset of values
     */
    public static ArrayList<String> GetRandomSubset(ArrayList<String> list, int numValues, Random random)
    {
        return Subset(Shuffle(list, random), 0, MathUtilities.Min(numValues, list.size()));
    }

    /** Convenience method to remove all instances of a given String value from a list.
     *
     * @param list List of String values
     * @param itemToRemove Item to be removed
     * @return New list with items removed
     */
    public static ArrayList<String> RemoveAll(ArrayList<String> list, String itemToRemove)
    {
        return RemoveAll(list, ListUtilities.CreateStringList(itemToRemove));
    }

    /** Convenience method to remove all instances of given String values from a list.
     *
     * @param list List of String values
     * @param itemsToRemove Items to be removed
     * @return New list with items removed
     */
    public static ArrayList<String> RemoveAll(ArrayList<String> list, ArrayList<String> itemsToRemove)
    {
        if (itemsToRemove.size() == 0)
            return list;

        ArrayList<String> newList = new ArrayList<String>(list);
        newList.removeAll(itemsToRemove);

        return newList;
    }

    /** This method returns unique String values that are in the first list but not in the second list (set difference).
     *
     * @param list1 Primary list
     * @param list2 Secondary list to be subtracted
     * @return New list with remaining values
     */
    public static ArrayList<String> GetDifference(ArrayList<String> list1, ArrayList<String> list2)
    {
        if (list1.size() == 0)
            return new ArrayList<String>();
        if (list2.size() == 0)
            return list1;

        HashSet<String> set1 = new HashSet<String>(list1);
        HashSet<String> set2 = new HashSet<String>(list2);

        set1.removeAll(set2);

        return new ArrayList<String>(set1);
    }

    /** Identifies all values in a String list that end with a given String value.
     *
     * @param values List of values to be tested
     * @param pattern String value to be matched
     * @return List subset
     */
    public static ArrayList<String> GetValuesEndingWith(ArrayList<String> values, String pattern)
    {
        ArrayList<String> results = new ArrayList<String>();

        for (String value : values)
            if (value.endsWith(pattern))
                results.add(value);

        return results;
    }

    /** Identifies all values in a String list that start with a given String value.
     *
     * @param values List of values to be tested
     * @param pattern String value to be matched
     * @return List subset
     */
    public static ArrayList<String> GetValuesStartingWith(ArrayList<String> values, String pattern)
    {
        ArrayList<String> results = new ArrayList<String>();

        for (String value : values)
            if (value.startsWith(pattern))
                results.add(value);

        return results;
    }

    /** Gets a subset of a String array, starting with the specified index position.
     *
     * @param array String array
     * @param startIndex Start index position
     * @return Array subset
     */
    public static String[] Subset(String[] array, int startIndex)
    {
        return Subset(array, startIndex, array.length);
    }

    /** Gets a subset of a String array, starting with the specified index position.
     *
     * @param array String array
     * @param startIndex Start index position
     * @param endIndex End index position
     * @return Array subset
     */
    public static String[] Subset(String[] array, int startIndex, int endIndex)
    {
        return ConvertToStringArray(ListUtilities.Subset(ListUtilities.CreateStringList(array), startIndex, endIndex));
    }

    /** Gets a subset of a String list, starting with the specified index position.
     *
     * @param list String list
     * @param startIndex Start index position
     * @return List subset
     */
    public static ArrayList<String> Subset(ArrayList<String> list, int startIndex)
    {
        return Subset(list, startIndex, list.size());
    }

    /** Gets a subset of a String list, starting with the specified index position.
     *
     * @param list String list
     * @param startIndex Start index position
     * @param endIndex End index position
     * @return List subset
     */
    public static ArrayList<String> Subset(ArrayList<String> list, int startIndex, int endIndex)
    {
        ArrayList result = new ArrayList();

        for (int i = startIndex; i < endIndex; i++)
            result.add(list.get(i));

        return result;
    }

    /** Gets a subset of a String list for a given set of indices.
     *
     * @param values List of String values
     * @param indices Indices at which values should be obtained
     * @return List subset
     */
    public static ArrayList<String> Subset(ArrayList<String> values, ArrayList<Integer> indices)
    {
        ArrayList<String> subset = new ArrayList<String>();

        for (int index : indices)
            subset.add(values.get(index));

        return subset;
    }

    /** Finds the intersection between two lists of String objects.
     *
     * @param list1 First list
     * @param list2 Second list
     * @return Intersection list (contains values that exist in both lists)
     */
    public static ArrayList<String> Intersect(ArrayList<String> list1, ArrayList<String> list2)
    {
        if (list1.size() == 0)
            return list2;

        Set intersection = new HashSet(list1);
        intersection.retainAll(new HashSet(list2));
        return new ArrayList(intersection);
    }

    /** Finds the union of two lists of String objects
     *
     * @param list1 First list
     * @param list2 Second list
     * @return Union list (contains values that exist in either list)
     */
    public static ArrayList<String> Union(ArrayList<String> list1, ArrayList<String> list2)
    {
        Set union = new HashSet(list1);
        union.addAll(new HashSet(list2));
        return new ArrayList(union);
    }

    /** For a list of String objects, this method trims any white space from each value.
     *
     * @param coll List of String objects
     * @return List of trimmed String objects
     */
    public static ArrayList<String> TrimStrings(ArrayList<String> coll)
    {
        ArrayList<String> result = new ArrayList<String>();

        for (String x : coll)
            result.add(x.trim());

        return result;
    }

    /** For a list of String objects, this method identifies all objects that contain a given value and replaces that text with another value.
     *
     * @param list List of String objects
     * @param from Value to search for
     * @param to Value with which to replace matches
     * @return Replaced list of String objects
     */
    public static ArrayList<String> Replace(ArrayList<String> list, String from, String to)
    {
        ArrayList<String> newList = new ArrayList<String>();

        for (String x : list)
            newList.add(x.replace(from, to));

        return newList;
    }

    /** For a list of String objects, this method replaces all instances that match a given value with another value.
     *
     * @param list List of String objects
     * @param from Value to search for
     * @param to Value with which to replace matches
     * @return Replaced list of String objects
     */
    public static ArrayList<String> ReplaceAllExactMatches(ArrayList<String> list, String from, String to)
    {
        if (list.size() == 0)
            return list;

        ArrayList<String> newList = new ArrayList<String>(list);

        for (int i = 0; i < newList.size(); i++)
            if (newList.get(i).equals(from))
                newList.set(i, to);

        return newList;
    }

    /** This method indicates which element is most frequently observed in a given list of String objects.
     *
     * @param values String values
     * @return Most frequent value
     */
    public static String GetMostFrequentValue(ArrayList<String> values)
    {
        HashMap<String, Integer> frequencyMap = MapUtilities.GetFrequencyMap(values);
        int max = 0;
        String valueOfMax = null;

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet())
            if (entry.getValue() > max)
            {
                max = entry.getValue();
                valueOfMax = entry.getKey();
            }

        return valueOfMax;
    }

    /** This list indicates the number of values in a list of String objects that match a given value.
     *
     * @param values List of String values
     * @param matchValue Match value
     * @return Number of matches
     */
    public static int GetNumMatches(ArrayList<String> values, String matchValue)
    {
        int count = 0;

        for (String value : values)
            if (value.equals(matchValue))
                count++;

        return count;
    }

    /** From a list of String objects, this method randomly selects one of the objects
     *
     * @param list List of String objects
     * @return Randomly selected value
     */
    public static String PickRandomString(ArrayList<String> list, Random random)
    {
        return (String)PickRandomObject(list, random);
    }

    /** From a list of objects, this method randomly selects one of the objects
     *
     * @param list List of objects
     * @return Randomly selected object
     */
    public static Object PickRandomObject(ArrayList list, Random random)
    {
        return Shuffle(list, random).get(0);
    }

    /** For a list of boolean objects, this method indicates whether all are true.
     *
     * @param booleans List of boolean values
     * @return Whether all of the values are true
     */
    public static boolean AllTrue(ArrayList<Boolean> booleans)
    {
        for (Boolean x : booleans)
            if (!x.equals(Boolean.TRUE))
                return false;

        return true;
    }

    /** For a list of boolean objects, this method indicates whether any are true.
     *
     * @param booleans List of boolean values
     * @return Whether any of the values are true
     */
    public static boolean AnyTrue(ArrayList<Boolean> booleans)
    {
        for (Boolean x : booleans)
            if (x.equals(Boolean.TRUE))
                return true;

        return false;
    }

    /** For a list of boolean objects, this method indicates whether any are false.
     *
     * @param booleans List of boolean values
     * @return Whether any of the values are false
     */
    public static boolean AnyFalse(ArrayList<Boolean> booleans)
    {
        for (Boolean x : booleans)
            if (x.equals(Boolean.FALSE))
                return true;

        return false;
    }

    /** This method randomly shuffles a list of String objects.
     *
     * @param list List of String objects
     * @param randomNumberGenerator Random number generator that will be used for shuffling
     * @return Shuffled list
     */
    public static ArrayList<String> Shuffle(ArrayList<String> list, Random randomNumberGenerator)
    {
        ArrayList<String> shuffled = new ArrayList<String>(list);

        Collections.shuffle(shuffled, randomNumberGenerator);

        return shuffled;
    }

    /** This method converts an array of String objects to a single String representation and inserts a delimiter between each object.
     *
     * @param s Array of String objects
     * @param delimiter Delimiter
     * @return Formatted String representation
     */
    public static String Join(String[] s, String delimiter)
    {
        return Join(CreateStringList(s), delimiter);
    }

    /** This method converts a list of String objects to a single String representation and inserts a delimiter between each object.
     * 
     * @param list List of String objects
     * @param delimiter Delimiter
     * @return Formatted String representation
     */
    public static String Join(ArrayList<String> list, String delimiter)
    {
        if (list.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for (String x : list)
            sb.append(x + delimiter);

        sb.delete(sb.length() - delimiter.length(), sb.length());

        return sb.toString();
    }

    /** For a list of double objects, this method identified the indices of elements that match a specified value.
     *
     * @param values List of double objects
     * @param value Value to search for
     * @return Matching indices
     */
    public static ArrayList<Integer> GetIndices(ArrayList<Double> values, double value)
    {
        ArrayList<Integer> indices = new ArrayList<Integer>();

        for (int i=0; i<values.size(); i++)
            if (values.get(i) == value)
                indices.add(i);
        
        return indices;
    }

    /** For a list of double objects, this method indicates how many elements match a specified value.
     *
     * @param values List of double objects
     * @param matchValue Value to search for
     * @return Number of matches
     */
    public static int GetNumEqualTo(ArrayList<Double> values, double matchValue)
    {
        int count = 0;

        for (double value : values)
            if (value == matchValue)
                count++;

        return count;
    }

    /** For a list of double objects, this method indicates which elements are greater than (or equal to) a specified value.
     *
     * @param values List of double objects
     * @param limit Query value
     * @param orEqualTo Indicates whether values should be considered a match if they are equal to the query value
     * @return Elements that are greater than (or equal to) the query value
     */
    public static ArrayList<Double> GreaterThan(ArrayList<Double> values, double limit, boolean orEqualTo)
    {
        ArrayList<Double> modValues = new ArrayList<Double>();

        for (double value : values)
            if (value > limit || (value == limit && orEqualTo))
                modValues.add(value);

        return modValues;
    }

    /** For a list of double objects, this method indicates which elements are less than (or equal to) a specified value.
     *
     * @param values List of double objects
     * @param limit Query value
     * @param orEqualTo Indicates whether values should be considered a match if they are equal to the query value
     * @return Elements that are less than (or equal to) the query value
     */
    public static ArrayList<Double> LessThan(ArrayList<Double> values, double limit, boolean orEqualTo)
    {
        ArrayList<Double> modValues = new ArrayList<Double>();

        for (double value : values)
            if (value < limit || (value == limit && orEqualTo))
                modValues.add(value);

        return modValues;
    }

    /** For a list of double objects, this method indicates the indices of elements that are greater than (or equal to) a specified value.
     *
     * @param values List of double objects
     * @param limit Query value
     * @param orEqualTo Indicates whether values should be considered a match if they are equal to the query value
     * @return Indices of elements that are greater than (or equal to) the query value
     */
    public static ArrayList<Integer> WhichGreaterThan(ArrayList<Double> values, double limit, boolean orEqualTo)
    {
        ArrayList<Integer> greaterThan = new ArrayList<Integer>();

        for (int i=0; i<values.size(); i++)
            if (values.get(i) > limit || (values.get(i) == limit && orEqualTo))
                greaterThan.add(i);

        return greaterThan;
    }

    /** For a list of double objects, this method indicates the indices of elements that are less than (or equal to) a specified value.
     *
     * @param values List of double objects
     * @param limit Query value
     * @param orEqualTo Indicates whether values should be considered a match if they are equal to the query value
     * @return Indices of elements that are less than (or equal to) the query value
     */
    public static ArrayList<Integer> WhichLessThan(ArrayList<Double> values, double limit, boolean orEqualTo)
    {
        ArrayList<Integer> lessThan = new ArrayList<Integer>();

        for (int i=0; i<values.size(); i++)
            if (values.get(i) < limit || (values.get(i) == limit && orEqualTo))
                lessThan.add(i);

        return lessThan;
    }

    /** For a list of String objects, this method retrieves the elements at the given indices.
     *
     * @param values List of String objects
     * @param indices Indices
     * @return List containing values from specified indices
     */
    public static ArrayList<String> Get(ArrayList<String> values, ArrayList<Integer> indices)
    {
        ArrayList<String> matches = new ArrayList<String>();

        for (int i : indices)
            matches.add(values.get(i));

        return matches;
    }

    /** Convenience methods that makes it easier to sort a collection
     *
     * @param list Collection to sort
     * @return Sorted list
     */
    public static ArrayList Sort(Collection list)
    {
        ArrayList newList = new ArrayList(list);
        Collections.sort(newList);
        return newList;
    }

    /** Convenience methods that makes it easier to sort a String list
     *
     * @param list List to sort
     * @return Sorted list
     */
    public static ArrayList<String> SortStringList(ArrayList<String> list)
    {
        ArrayList<NumericString> numericStringList = new ArrayList<NumericString>();
        for (String item : list)
            numericStringList.add(new NumericString(item));

        Collections.sort(numericStringList);

        ArrayList<String> sortedList = new ArrayList<String>();
        for (NumericString item : numericStringList)
            sortedList.add(item.toString());

        return sortedList;
    }

    /** This method inserts a String value at the specified index into a list of String objects.
     *
     * @param list List of String objects
     * @param value Value to insert
     * @param index Index at which to insert the value
     * @return Modified list
     */
    public static ArrayList<String> InsertIntoStringList(ArrayList<String> list, String value, int index)
    {
        ArrayList<String> newList = new ArrayList<String>(list);
        newList.add(index, value);
        return newList;
    }

    /** This method creates a new list with each element of the input list in its lower-case representation.
     *
     * @param list List to be converted to lower case
     * @return New list converted to lower case
     */
    public static ArrayList<String> ToLowerCase(ArrayList<String> list)
    {
        ArrayList<String> newList = new ArrayList<String>();

        for (String x : list)
            newList.add(x.toLowerCase());

        return newList;
    }

    /** Adds a prefix to each element in the list.
     *
     * @param list List of strings
     * @param prefix Prefix that wil be added
     * @return Newly formatted string list
     */
    public static ArrayList<String> Prefix(ArrayList<String> list, String prefix)
    {
        ArrayList<String> newList = new ArrayList<String>();

        for (String x : list)
            newList.add(prefix  + x);

        return newList;
    }
}