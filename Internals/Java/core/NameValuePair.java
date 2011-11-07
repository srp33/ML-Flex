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

package mlflex.core;

import mlflex.helper.DataTypeUtilities;

/** A name-value pair.
 * @author Stephen Piccolo
 */
public class NameValuePair
{
    public String Name;
    public String Value;

    /** Constructor
     *
     * @param name Name
     * @param value Value
     */
    public NameValuePair(String name, String value)
    {
        Name = name;
        Value = value;
    }

    @Override
    public String toString()
    {
        return Name + "\t" + Value;
    }

    /** This is a factory method that aids in creating a NameValuePair object that has an integer value.
     *
     * @param name Name
     * @param value Value
     * @return Name-value pair
     */
    public static NameValuePair Create(String name, int value)
    {
        return new NameValuePair(name, String.valueOf(value));
    }

    /** This is a factory method that aids in creating a NameValuePair object that has a double value. It rounds the double value as necessary.
     *
     * @param name Name
     * @param value Value
     * @return Name-value pair
     */
    public static NameValuePair Create(String name, double value)
    {
        if (Double.isNaN(value))
            return new NameValuePair(name, String.valueOf(Double.NaN));
        else
            return new NameValuePair(name, DataTypeUtilities.ConvertDoubleToString(value));
    }
}
