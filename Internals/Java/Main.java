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

package mlflex;

import mlflex.core.*;
import mlflex.helper.*;

import java.io.File;
import java.util.HashMap;

/** This is the class that gets invoked when ML-Flex begins to execute.
 * @author Stephen Piccolo
 */
public class Main
{
    /** This method is the first one invoked when ML-Flex is run from the command line. It sets up an experiment, processes it, and handles exceptions at a high level.
     *
     * @param args Array of arguments that are passed to this application from the Java runtime
     */
    public static void main(String[] args)
    {
        InitializeLogging(args);

        try
        {
            ParseCommandLineSettings(args);
            ProcessExperiment(GetAction(args));

            Singletons.Log.Info("Successfully completed!");
            System.exit(0); // Not sure if this is necessary, but keeping it just in case
        }
        catch (Exception ex)
        {
            Singletons.Log.Exception(ex);
            System.exit(0); // Not sure if this is necessary, but keeping it just in case
        }
    }

    /** Initializes the log files and the logging objects so they can be used through the application.
     *
     * @param args Command-line arguments
     */
    private static void InitializeLogging(String[] args)
    {
        try
        {
            Singletons.Log = new Log();
        }
        catch (Exception ex)
        {
            System.out.println("ML-Flex logging could not be configured.");

            System.out.println("Args:");
            for (String arg : args)
                System.out.println(arg);
            ex.printStackTrace();

            System.exit(0);
        }
    }

    /** Parses configuration settings that have been specified at the command line and saves these settings so they can be used throughout the application.
     *
     * @param args Command-line arguments
     * @throws Exception
     */
    private static void ParseCommandLineSettings(String[] args) throws Exception
    {
        Settings.MAIN_DIR = GetArgValue(args, "MAIN_DIRECTORY", System.getProperty("user.dir") + "/");

        if (!Settings.MAIN_DIR.endsWith("/"))
            Settings.MAIN_DIR += "/";

        Settings.VERSION_FILE = "Version.txt";

        Settings.EXPERIMENT_FILE = GetArgValue(args, "EXPERIMENT_FILE", null);
        Settings.INTERNALS_DIR = FileUtilities.CreateDirectoryIfNotExists(Settings.MAIN_DIR + "Internals/");

        Settings.DEBUG = Boolean.parseBoolean(GetArgValue(args, "DEBUG", "false"));

        String numAvailableProcessors = String.valueOf(Runtime.getRuntime().availableProcessors());
        Settings.NUM_THREADS = Integer.parseInt(GetArgValue(args, "NUM_THREADS", numAvailableProcessors));
        Settings.THREAD_TIMEOUT_MINUTES = Long.parseLong(GetArgValue(args, "THREAD_TIMEOUT_MINUTES", "60"));
        Settings.PAUSE_SECONDS = Long.parseLong(GetArgValue(args, "PAUSE_SECONDS", "5"));
        Settings.EXPORT_DATA = Boolean.parseBoolean(GetArgValue(args, "EXPORT_DATA", "false"));

        Settings.LEARNER_TEMPLATES_FILE = GetArgValue(args, "LEARNER_TEMPLATES_FILE", "Config/Learner_Templates.txt");
        Settings.CLASSIFICATION_ALGORITHMS_FILE = GetArgValue(args, "CLASSIFICATION_ALGORITHMS_FILE", "Config/Classification_Algorithms.txt");
        Settings.FEATURE_SELECTION_ALGORITHMS_FILE = GetArgValue(args, "FEATURE_SELECTION_ALGORITHMS_FILE", "Config/Feature_Selection_Algorithms.txt");

        Settings.ParseLearners();
        Settings.ParseAlgorithms();
    }

    /** This method sets up the experiment to be processed and then processes it.
     *
     * @param action The action to be performed for the experiment
     * @throws Exception
     */
    private static void ProcessExperiment(Action action) throws Exception
    {
        // Make sure we can find the experiment file
        if (!FileUtilities.FileExists(Settings.EXPERIMENT_FILE))
            Singletons.Log.ExceptionFatal("Invalid experiment file: " + Settings.EXPERIMENT_FILE);

        // Parse the experiment name from the file path
        String experiment = FileUtilities.RemoveFileExtension(new File(Settings.EXPERIMENT_FILE).getName());

        Singletons.Log.Info("Beginning experiment " + experiment);

        // Initialize configuration settings specific to this experiment
        Singletons.Config = new Config(Settings.EXPERIMENT_FILE);

        // Initialize singleton objects
        Singletons.Experiment = new Experiment(experiment);
        Singletons.InstanceVault = new InstanceVault();
        Singletons.ProcessorVault = new ProcessorVault();
        Singletons.ProcessorVault.Load();

        for (int i=1; i<(Singletons.Config.GetNumIterations() +1); i++)
        {
            Singletons.Iteration = i;
            Singletons.Log.NumExceptionsCaught = 0;

            // Initialize (and create if needed) the experiment-specific directories
            Settings.DATA_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "Data/" + Singletons.Experiment.toString(), -1);
            Settings.FEATURE_SELECTION_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "SelectedFeatures/" + Singletons.Experiment.toString(), i);
            Settings.PREDICTIONS_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "Predictions/" + Singletons.Experiment.toString(), i);
            Settings.ENSEMBLE_PREDICTIONS_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "EnsemblePredictions/" + Singletons.Experiment.toString(), i);
            Settings.OUTPUT_DIR = InitializeDirectory(Settings.MAIN_DIR, "Output/" + Singletons.Experiment.toString(), -1);
            Settings.LOCKS_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "Locks/" + Singletons.Experiment.toString(), i);
            Settings.STATUS_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "Status/" + Singletons.Experiment.toString(), i);
            Settings.TEMP_DATA_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "TempData/" + Singletons.Experiment.toString(), -1);
            Settings.TEMP_RESULTS_DIR = InitializeDirectory(Settings.INTERNALS_DIR, "TempResults/" + Singletons.Experiment.toString(), -1);

            // Execute the experiment
            Singletons.Experiment.Orchestrate(action);
        }
    }

    /** Formulates a directory path that is specific to a given purpose within an experiment and ensures the directory is created.
     *
     * @param rootDirectory Root directory path
     * @param directoryName Relative directory name
     * @param iteration Iteration number
     * @return Absolute directory path
     * @throws Exception
     */
    private static String InitializeDirectory(String rootDirectory, String directoryName, int iteration) throws  Exception
    {
        String directoryPath = rootDirectory + directoryName + "/";

        if (iteration > 0)
            directoryPath += "Iteration" + iteration + "/";

        try
        {
            return FileUtilities.CreateDirectoryIfNotExists(directoryPath);
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug(ex);
            return directoryPath;
        }
    }

    /** Determines what action has been specified by the user and validates it.
     *
     * @param args Command-line arguments
     * @return The specified action
     * @throws Exception
     */
    private static Action GetAction(String[] args) throws Exception
    {
        String actionText = GetArgValue(args, "ACTION", "");

        if (actionText.equals(""))
            throw new Exception("No valid action value has been specified.");

        try
        {
            return Action.valueOf(actionText);
        }
        catch (Exception ex)
        {
            throw new Exception("Invalid action specified: " + actionText);
        }
    }

    /** Parses a value with a specified key from the command-line arguments.
     *
     * @param args Command-line arguments
     * @param key Key of the argument
     * @param defaultValue Value that is used if no value is specified
     * @return Value of the argument
     * @throws Exception
     */
    private static String GetArgValue(String[] args, String key, String defaultValue) throws Exception
    {
        HashMap<String, String> keyValueMap = new HashMap<String, String>();

        for (String arg : args)
            if (arg.contains("="))
            {
                String[] parts = arg.split("=");
                if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0)
                    keyValueMap.put(parts[0], parts[1]);
            }

        if (keyValueMap.containsKey(key))
            return keyValueMap.get(key);
        else
        {
            if (defaultValue == null)
                throw new Exception("A value for " + key + " must be set at the command line.");
            else
                return defaultValue;
        }
    }
}