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

import mlflex.core.DataInstanceCollection;
import mlflex.core.DataValues;
import mlflex.core.Settings;
import mlflex.core.Singletons;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

/** This class contains general-purpose helper methods that are used in various places throughout the code. It also contains Singleton objects (those that are instantiated only once and stored as static variables).
 * @author Stephen
 */
public class MiscUtilities
{

    /** Some external libraries do not work well with special characters, so this method changes those special characters temporarily to other characters.
     *
     * @param names List of names to be formatted
     * @return Formatted names
     */
    public static ArrayList<String> FormatNames(ArrayList<String> names)
    {
        ArrayList<String> formatted = new ArrayList<String>();

        for (String name : names)
            formatted.add(FormatName(name));

        return formatted;
    }

    /** Some external libraries do not work well with special characters, so this method changes those special characters temporarily to other characters.
     *
     * @param name Name to be formatted
     * @return Formatted name
     */
    public static String FormatName(String name)
    {
        return name.replace("/", "_forward_").replace(" ", "_space_").replace("*", "_star_").replace("-", "_hyphen_");
    }

    /** Some external libraries do not work well with special characters. After a name has been formatted, this method changes the characters back to the original characters.
     *
     * @param names List of names to be unformatted
     * @return Unformatted names
     */
    public static ArrayList<String> UnformatNames(ArrayList<String> names)
    {
        ArrayList<String> unformatted = new ArrayList<String>();

        for (String name : names)
            unformatted.add(UnformatName(name));

        return unformatted;
    }

    /** Some external libraries do not work well with special characters. After a name has been formatted, this method changes the characters back to the original characters.
     *
     * @param name Name to be unformatted
     * @return Unformatted name
     */
    public static String UnformatName(String name)
    {
        return name.replace("_forward_", "/").replace("_space_", " ").replace("_star_", "*").replace("_hyphen_", "-");
    }

    /** Indicates the IP address of the machine where this software is being run
     *
     * @return IP address
     * @throws Exception
     */
    public static String GetMachineAddress() throws Exception
    {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /** Generates a unique identifier randomly
     *
     * @return Random unique identifier
     */
    public static String GetUniqueID()
    {
        return "id." + UUID.randomUUID();
    }

    /** Indicates whether a string value is either null or equal to the missing character
     *
     * @param value Value to be tested
     * @return Whether or not it is considered missing
     */
    public static boolean IsMissing(String value)
    {
        return value == null || value.equals(Settings.MISSING_VALUE_STRING);
    }

    /** Deletes all files and directories recursively from a "core" directory...a directory that contains key files that are created in the process of executing an experiment and that contains subdirectories specific to the iteration number.
     *
     * @param directoryPath Absolute path to the iteration directory
     * @throws Exception
     */
    public static void DeleteCoreDirectory(String directoryPath) throws Exception
    {
        directoryPath = directoryPath.replace("/Iteration" + String.valueOf(Singletons.Iteration), "");

        FileUtilities.DeleteAllFilesAndDirectoriesRecursively(directoryPath);
        FileUtilities.DeleteDirectory(directoryPath);
    }

    /** Causes the current thread to sleep for a certain length of time.
     *
     * @param milliseconds The number of milliseconds that the thread should sleep
     * @throws Exception
     */
    public static void Sleep(long milliseconds) throws Exception
    {
        Thread.currentThread().sleep(milliseconds);
    }

    /** Using a text representation of class instantiation as an input, this method instantiates the corresponding class with the specified parameters.
     *
     * @param classInstantiationText Text representation of the class to be instantiated
     * @return Instantiated class
     * @throws Exception
     */
    public static Object InstantiateClassFromText(String classInstantiationText) throws Exception
    {
        Singletons.Log.Debug("Attempting to instantiate " + classInstantiationText);

        // Check to see if any parameters have been specified (in parenthesis)
        if (classInstantiationText.contains("("))
        {
            // Extract the class name
            String className = classInstantiationText.substring(0, classInstantiationText.indexOf("("));

            // Remove any quotation marks and the closing parenthesis
            classInstantiationText = classInstantiationText.replace("\"", "").substring(classInstantiationText.indexOf("(") + 1).replace(")", "");

            // Extract the parameters which are separated by commas
            ArrayList<String> parameters = ListUtilities.CreateStringList(classInstantiationText.split(","));

            // Trim any excess white space from the parameters
            for (int i=0; i<parameters.size(); i++)
                parameters.set(i, parameters.get(i).trim());

            Class[] constructorParamClasses = new Class[parameters.size()];

            // There's probably a more elegant way to do this, but test each parameter for its type
            for (int i=0; i<constructorParamClasses.length; i++)
            {
                constructorParamClasses[i] = String.class;

                if (DataTypeUtilities.IsInteger((parameters.get(i).toString())))
                    constructorParamClasses[i] = Integer.class;
                else
                {
                    if (DataTypeUtilities.IsDouble((parameters.get(i).toString())))
                        constructorParamClasses[i] = Double.class;
                }

                if (DataTypeUtilities.IsBoolean((parameters.get(i).toString())))
                    constructorParamClasses[i] = Boolean.class;
            }

            // Again, there's probably a more elegant way to do this, but create a parameter of the specific type
            Object[] objectParameters = new Object[parameters.size()];
            for (int i=0; i<parameters.size(); i++)
            {
                Object objectParameter = parameters.get(i);

                if (DataTypeUtilities.IsInteger(parameters.get(i).toString()))
                    objectParameter = Integer.parseInt(parameters.get(i));
                else
                {
                    if (DataTypeUtilities.IsDouble(parameters.get(i).toString()))
                        objectParameter = Double.parseDouble(parameters.get(i));
                }

                if (DataTypeUtilities.IsBoolean(parameters.get(i).toString()))
                    objectParameter = Boolean.parseBoolean(parameters.get(i));

                objectParameters[i] = objectParameter;
            }

            return ((java.lang.reflect.Constructor) Class.forName(className).getConstructor(constructorParamClasses)).newInstance(objectParameters);
        }
        else
        {
            return ((java.lang.reflect.Constructor) Class.forName(classInstantiationText).getConstructor()).newInstance();
        }
    }

    /** If you have a string that has words that are each capitalized but are scrunched together without spaces between them, this method adds a space between each word.
     *
     * @param text Text that may need to be separated
     * @return Modified text
     */
    public static String SeparateWords(String text)
    {
        String modString = "";

        for (int i=0; i<text.length(); i++)
        {
            // Look for an upper-case letter
            if (i > 0 && Character.isUpperCase(text.charAt(i)))
                modString += " ";
            modString += text.charAt(i);
        }

        return modString;
    }

    /** This method can be used to convert data points that have more than two possible values into a series of binary data points.
     *
     * @param instances Data instances to be converted
     * @return Converted data instances
     * @throws Exception
     */
    public static DataInstanceCollection ConvertMultiValuedDataPointsToBinary(DataInstanceCollection instances) throws Exception
    {
        DataInstanceCollection modInstances = new DataInstanceCollection();

        for (String dataPointName : instances.GetDataPointNames())
        {
            ArrayList<String> uniqueDataPointValues = instances.GetUniqueValues(dataPointName);

            for (DataValues instance : instances)
            {
                DataValues newInstance = instance.CopyStructure();

                if (uniqueDataPointValues.size() <= 2)
                    newInstance.AddDataPoint(dataPointName, instance.GetDataPointValue(dataPointName));
                else
                {
                    if (DataTypeUtilities.HasOnlyNumeric(uniqueDataPointValues))
                        newInstance.AddDataPoint(dataPointName, instance.GetDataPointValue(dataPointName));
                    else
                        for (String dataPointValueOption : uniqueDataPointValues)
                            newInstance.AddBinaryDataPoint(dataPointName + "_" + dataPointValueOption, instance.GetDataPointValue(dataPointName), dataPointValueOption);
                }

                modInstances.Add(newInstance);
            }
        }

        return modInstances;
    }
}