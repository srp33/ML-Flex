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

import mlflex.core.Settings;
import mlflex.core.Singletons;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** This class is used to execute commands at the command. It contains functionality to invoke commands and retrieve results using a text-file based approach.
 * @author Stephen Piccolo
 */
public class CommandLineClient
{
    public static final String STANDARD_OUT_KEY = "STANDARD_OUT";

    /** Executes the specified command at the command line
     *
     * @param commandText System command that will be executed
     * @return A map of keys and values that were returned by the command
     * @throws Exception
     */
    public static HashMap<String, String> RunAnalysis(String commandText) throws Exception
    {
        return RunAnalysis(commandText, null);
    }

    /** Executes the specified command at the command line
     *
     * @param commandText System command that will be executed
     * @param outputDirectoryPath Absolute directory path where any output files will be stored temporarily
     * @return A map of keys and values that were returned by the command
     * @throws Exception
     */
    public static HashMap<String, String> RunAnalysis(String commandText, String outputDirectoryPath) throws Exception
    {
        Singletons.Log.Debug("System command:");
        Singletons.Log.Debug(commandText);

        // Write the command to a temporary script file. This helps avoid issues with quotes, etc.
        String tempScriptFilePath = Settings.TEMP_DATA_DIR + MiscUtilities.GetUniqueID();
        FileUtilities.WriteTextToFile(tempScriptFilePath, commandText);
        new File(tempScriptFilePath).setExecutable(true);

        // Execute the command via the temporary script
        Process p = Runtime.getRuntime().exec(tempScriptFilePath);

        // Read the output and error streams from the process
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        StringBuffer output = new StringBuffer();
        StringBuffer error = new StringBuffer();

        // Parse the output stream
        String s;
        while ((s = stdInput.readLine()) != null)
            output.append(s + "\n");

        // Parse the error stream
        while ((s = stdError.readLine()) != null)
            error.append(s + "\n");

        // Close the process objects
        stdInput.close();
        stdError.close();
        p.destroy();

        ArrayList<java.io.File> outputFiles = new ArrayList<java.io.File>();

        // Retrieve output files that were saved by the process
        if (outputDirectoryPath != null)
            outputFiles = FileUtilities.GetFilesInDirectory(outputDirectoryPath);

        // Print the output
        if (output.length() > 0)
            Singletons.Log.Debug("Command output: " + output.toString());

        // Print the error, including parameters that had been specified, to aid in troubleshooting
        if (error.length() > 0)
        {
            Singletons.Log.Debug("Command error: " + error.toString());

            Singletons.Log.Debug("Output files:");
            for (java.io.File file : outputFiles)
            {
                Singletons.Log.Debug(file.getName());
                Singletons.Log.Debug(FileUtilities.ReadTextFile(file));
            }
        }

        // Create a map that summarizes the output
        HashMap<String, String> results = new HashMap<String, String>();
        for (java.io.File file : outputFiles)
            results.put(file.getName(), FileUtilities.ReadTextFile(file).trim());
        results.put(STANDARD_OUT_KEY, output.toString());

        // Clean up
        if (outputDirectoryPath != null)
            FileUtilities.RemoveDirectory(outputDirectoryPath);

        FileUtilities.DeleteFile(tempScriptFilePath);

        return results;
    }

    /** Convenience method for accessing an individual result of a command.
     *
     * @param results Command results
     * @param name Key of result to retrieve
     * @return Result
     * @throws Exception
     */
    public static String GetCommandResult(HashMap<String, String> results, String name) throws Exception
    {
        if (results.get(name) == null)
        {
            String error = "An error occurred in executing at the command line. The result named " + name + " could not be found.";

            if (results.size() > 0)
                error += "\nResults that could be found:\n";

            for (Map.Entry<String, String> entry : results.entrySet())
                error += entry.getKey() + ": " + entry.getValue() + "\n";
            
            throw new Exception(error);
        }

        return results.get(name).trim();
    }
}
