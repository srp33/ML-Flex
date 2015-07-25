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

import java.text.SimpleDateFormat;
import java.util.*;

/** This class provides helper methods for handling dates in Java.
 * @author Stephen Piccolo
 */
public class DateUtilities
{
    /** Finds the difference in months between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in months
     */
    public static double DifferenceInMonths(Date date1, Date date2)
    {
        return DifferenceInYears(date1, date2) * 12;
    }

    /** Finds the difference in years between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in years
     */
    public static double DifferenceInYears(Date date1, Date date2)
    {
        double days = DifferenceInDays(date1, date2);
        return days / 365.2425;
    }

    /** Finds the difference in days between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in days
     */
    public static double DifferenceInDays(Date date1, Date date2)
    {
        return DifferenceInHours(date1, date2) / 24.0;
    }

    /** Finds the difference in hours between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in hours
     */
    public static double DifferenceInHours(Date date1, Date date2)
    {
        return DifferenceInMinutes(date1, date2) / 60.0;
    }

    /** Finds the difference in minutes between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in minutes
     */
    public static double DifferenceInMinutes(Date date1, Date date2)
    {
        return DifferenceInSeconds(date1, date2) / 60.0;
    }

    /** Finds the difference in seconds between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Difference in seconds
     */
    public static double DifferenceInSeconds(Date date1, Date date2)
    {
        return DifferenceInMilliseconds(date1, date2) / 1000.0;
    }

    private static double DifferenceInMilliseconds(Date date1, Date date2)
    {
        return GetTimeInMilliseconds(date1) - GetTimeInMilliseconds(date2);
    }

    private static long GetTimeInMilliseconds(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getTimeInMillis() + cal.getTimeZone().getOffset(cal.getTimeInMillis());
    }

    /** Gets the current time but formats it in a format that can be included as part of a file name.
     *
     * @return Formatted timestamp
     */
    public static String GetTimeStampFormattedForFileName()
    {
        return GetTimeStamp().replace(":", "-").replace("/", "-");
    }

    /** Gets the current time as a String.
     *
     * @return The current time
     */
    public static String GetTimeStamp()
    {
        return DateUtilities.ConvertToFullString(GetCurrentDate());
    }

    /** Indicates whether the year in the specified date is in a leap year.
     *
     * @param date1 Date object
     * @return Whether the year is in a leap year
     */
    public static boolean IsInLeapYear(Date date1)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date1);
        return cal.isLeapYear(cal.get(cal.YEAR));
    }

    /** Convenience method for creating a date object.
     *
     * @param year Year
     * @param month Month
     * @param day Day
     * @return Date object
     */
    public static Date CreateDate(int year, int month, int day)
    {
        return CreateDate(year, month, year, 0, 0, 0);
    }

    /** Convenience method for creating a date object.
     *
     * @param year Year
     * @param month Month
     * @param day Day
     * @param hour Hour
     * @param minute Minute
     * @param second Second
     * @return Date object
     */
    public static Date CreateDate(int year, int month, int day, int hour, int minute, int second)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, second);
        return cal.getTime();
    }

    /** Convenience method for creating a date object.
     *
     * @param milliseconds Time in milliseconds.
     * @return Date object
     */
    public static Date CreateDate(long milliseconds)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        return cal.getTime();
    }

    /** Creates a date object for the current date/time.
     *
     * @return Date object with the current date/time
     */
    public static Date GetCurrentDate()
    {
        return new Date();
    }

    /** Converts a date object to a String object that specifies the full date/time.
     *
     * @param date Date object
     * @return Full string representation
     */
    public static String ConvertToFullString(Date date)
    {
        return ConvertToString(date, "yyyy/MM/dd HH:mm:ss");
    }

    /** Converts a date object to a String object that specifies the time.
     *
     * @param date Date object
     * @return Time string representation
     */
    public static String ConvertToTimeString(Date date)
    {
        return ConvertToString(date, "HH:mm:ss");
    }

    /** Converts a date object to a string using the specified format.
     *
     * @param date Date object
     * @param format String format
     * @return Formatted date/time
     */
    public static String ConvertToString(Date date, String format)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /** Parses a date object from a string representation.
     *
     * @param dateString String representation of a date
     * @return Date object
     */
    public static Date ParseDate(String dateString)
    {
        return ParseDate(dateString, "MM/dd/yyyy");
    }

    /** Parses a date object from a string representation using the specified date pattern.
     *
     * @param dateString String representation
     * @param datePattern Date pattern
     * @return Date object
     */
    public static Date ParseDate(String dateString, String datePattern)
    {
        try
        {
            SimpleDateFormat fmt = new SimpleDateFormat(datePattern);
            return fmt.parse(dateString);
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
