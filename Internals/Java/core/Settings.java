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

package mlflex.core;

import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.util.ArrayList;
import java.util.HashMap;

/** This class stores application wide values. These values are typically machine-specific and are set via command-line parameters.
 * @author Stephen Piccolo
 */
public class Settings
{
    /** The main directory in which the application is executing */
    public static String MAIN_DIR;
    /** Path to a file that indicates which version of the code is being used */
    public static String VERSION_FILE;
    /** Path to the directory that contains files that are semi-hidden from the end user */
    public static String INTERNALS_DIR;
    /** Path to the directory that contains data files */
    public static String DATA_DIR;
    /** Path to the directory that contains temporary data files */
    public static String TEMP_DATA_DIR;
    /** Path to the directory that contains temporary results files */
    public static String TEMP_RESULTS_DIR;
    /** Path to the directory that contains selected features */
    public static String FEATURE_SELECTION_DIR;
    /** Path to the directory that contains predictions resulting from classification */
    public static String PREDICTIONS_DIR;
    /** Path to the directory that contains predictions resulting from ensemble classification */
    public static String ENSEMBLE_PREDICTIONS_DIR;
    /** Path to the directory that stores "lock" files */
    public static String LOCKS_DIR;
    /** Path to the directory that stores output files */
    public static String OUTPUT_DIR;
    /** Path to the directory stores status files */
    public static String STATUS_DIR;
    /** Path to a file that defines learner interfaces */
    public static String LEARNER_TEMPLATES_FILE;
    /** Path to the file that contains configuration parameters for classification algorithms */
    public static String CLASSIFICATION_ALGORITHMS_FILE;
    /** Path to the file that contains configuration parameters for feature selection algorithms */
    public static String FEATURE_SELECTION_ALGORITHMS_FILE;
    /** Path to the file that contains configuration parameters for the experiment */
    public static String EXPERIMENT_FILE;
    /** The maximum number of threads that will be used per node */
    public static int NUM_THREADS;
    /** The maximum length of time that a thread will run before timing out */
    public static long THREAD_TIMEOUT_MINUTES;
    /** The length of time that a thread will pause before retrying to execute a task */
    public static long PAUSE_SECONDS;
    /** String that will be used throughout the experiment to indicate a missing value */
    public static String MISSING_VALUE_STRING = "?";
    /** A map of the learners that have been configured by the user */
    public static HashMap<String, LearnerConfig> LearnerConfigMap = new HashMap<String, LearnerConfig>();
    /** A map of all the classification algorithms that have been loaded into memory */
    public static HashMap<String, ClassificationAlgorithm> ClassificationAlgorithms = new HashMap<String, ClassificationAlgorithm>();
    /** A map of all the feature-selection algorithms that have been loaded into memory */
    public static HashMap<String, FeatureSelectionAlgorithm> FeatureSelectionAlgorithms = new HashMap<String, FeatureSelectionAlgorithm>();
    /** The maximum number of decimal places that will be used for output values */
    public static int RESULTS_NUM_DECIMAL_PLACES = 8;
    /** Whether the data for this experiment will be exported */
    public static boolean EXPORT_DATA;
    /** Whether debugging should be turned on */
    public static boolean DEBUG;
    /** Relative path to the directory containing HTML files for the output report */
    public static String HTML_RELATIVE_DIR = "Html/";

    /** The purpose of this method is to parse the information that is listed in the learners configuration file.
     *
     * @throws Exception
     */
    public static void ParseLearners() throws Exception
    {
        for (String line : FileUtilities.ReadLinesFromFile(LEARNER_TEMPLATES_FILE))
        {
            if (line.startsWith("#") || line.trim().length() == 0) // Ignore comment characters
                continue;

            ArrayList<String> lineItems = ListUtilities.CreateStringList(line.split(";"));
            String description = lineItems.get(0);
            String learnerClassName = lineItems.get(1);
            String commandTemplate = (lineItems.size() > 2) ? lineItems.get(2) : "";

            LearnerConfigMap.put(description, new LearnerConfig(description, learnerClassName, commandTemplate));
        }
    }

    /** The purpose of this method is to parse the algorithms that have been defined in the algorithm configuration files.
     *
     * @throws Exception
     */
    public static void ParseAlgorithms() throws Exception
    {
        for (String line : FileUtilities.ReadLinesFromFile(CLASSIFICATION_ALGORITHMS_FILE))
        {
            if (line.startsWith("#") || line.trim().length() == 0) // Ignore comment characters
                continue;

            ArrayList<String> lineItems = ListUtilities.CreateStringList(line.split(";"));
            String key = lineItems.remove(0);
            String learnerKey = lineItems.remove(0);

            CheckLearnerKey(key, learnerKey);
            ClassificationAlgorithms.put(key, new ClassificationAlgorithm(key, learnerKey, lineItems));
        }

        for (String line : FileUtilities.ReadLinesFromFile(FEATURE_SELECTION_ALGORITHMS_FILE))
        {
            if (line.startsWith("#") || line.trim().length() == 0) // Ignore comment characters
                continue;

            ArrayList<String> lineItems = ListUtilities.CreateStringList(line.split(";"));

            String key = lineItems.remove(0);
            String learnerKey = lineItems.remove(0);
            ArrayList<String> parameters = lineItems;

            CheckLearnerKey(key, learnerKey);
            FeatureSelectionAlgorithms.put(key, new FeatureSelectionAlgorithm(key, learnerKey, parameters));
        }

        // The following algorithms are made available to any experiment and aren't specified in the config file
        FeatureSelectionAlgorithms.put("None", new FeatureSelectionAlgorithm("None", "", new ArrayList<String>()));
        FeatureSelectionAlgorithms.put("PriorKnowledge", new FeatureSelectionAlgorithm("PriorKnowledge", "", new ArrayList<String>()));
    }

    private static void CheckLearnerKey(String algorithmKey, String learnerKey) throws Exception
    {
        if (!Settings.LearnerConfigMap.containsKey(learnerKey))
            Singletons.Log.ExceptionFatal("A learner template key of " + learnerKey + " was specified for the " + algorithmKey + " algorithm, but no such learner template exists.");
    }

    /** Depending on the settings for the experiment, this method returns a path to where the export files should be saved.
     *
     * @return Path to where the export files should be saved
     * @throws Exception
     */
    public static String GetOutputExportDir() throws Exception
    {
        return FileUtilities.CreateDirectoryIfNotExists(Settings.OUTPUT_DIR + "ExportedData/");
    }

    /** Depending on the settings for the experiment and the current iteration, this method returns a path to where the results files should be saved.
     *
     * @param subDirectoryPath Sub directory under results
     * @param appendIterationIfMoreThanOne Whether to append the iteration number if more than one iteration is being executed
     * @return Path to where the results files should be saved
     * @throws Exception
     */
    public static String GetOutputResultsDir(String subDirectoryPath, boolean appendIterationIfMoreThanOne) throws Exception
    {
        String dir = Settings.OUTPUT_DIR + "Results/" + subDirectoryPath;

        if (Singletons.Config.GetNumIterations() > 1 && appendIterationIfMoreThanOne)
            dir += "Iteration" + Singletons.Iteration + "/";

        return dir;
    }

    /** Depending on the settings for the experiment and the current iteration, this method returns a path to where the settings files should be saved.
     *
     * @param appendIterationIfMoreThanOne Whether to append the iteration number if more than one iteration is being executed
     * @return Path to where the settings files should be saved
     * @throws Exception
     */
    public static String GetOutputSettingsDir(boolean appendIterationIfMoreThanOne) throws Exception
    {
        String dirPath = Settings.OUTPUT_DIR + "Settings/";

        if (Singletons.Config.GetNumIterations() > 1 && appendIterationIfMoreThanOne)
            dirPath += "Iteration" + Singletons.Iteration + "/";

        return FileUtilities.CreateDirectoryIfNotExists(dirPath);
    }

    /** Depending on the settings for the experiment, this method returns a path to where the statistics files should be saved.
     *
     * @return Path to where the statistics files should be saved
     * @throws Exception
     */
    public static String GetOutputStatisticsDir() throws Exception
    {
        return FileUtilities.CreateDirectoryIfNotExists(Settings.OUTPUT_DIR + "Statistics/");
    }

    /** Indicates how many different combinations of data processor, feature-selection algorithm, and classification algorithm there are in this experiment.
     *
     * @param includeNumFeaturesOptions Whether to count each number of features options as separate classification algorithm
     * @return Number of unique combinations
     * @throws Exception
     */
    private static int GetNumberClassificationCombinations(boolean includeNumFeaturesOptions) throws Exception
    {
        int count = 0;

        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            for (FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                for (ClassificationAlgorithm classificationAlgorithm : Singletons.Config.GetMainClassificationAlgorithms())
                    count += (includeNumFeaturesOptions ? Singletons.Config.GetNumFeaturesOptions(processor, fsAlgorithm).size() : 1);

        return count;
    }

    /** Indicates whether feature selection needs to be performed at all for this experiment.
     *
     * @return Wheter feature selection needs to be performed at all for this experiment
     * @throws Exception
     */
    public static boolean NeedToSelectFeatures() throws Exception
    {
        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            for (FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                if (FeatureSelectionEvaluator.NeedToSelectFeatures(processor, fsAlgorithm))
                    return true;

        return false;
    }

    /** Indicates whether classification needs to be performed at all for this experiment.
     *
     * @return Whether classification needs to be performed at all for this experiment
     * @throws Exception
     */
    public static boolean NeedToClassify() throws Exception
    {
        return GetNumberClassificationCombinations(false) > 0;
    }

    /** Indicates whether ensemble classification needs to be performed at all for this experiment.
     *
     * @return Whether ensemble classification needs to be performed at all for this experiment
     * @throws Exception
     */
    public static boolean NeedToEnsembleLearn() throws Exception
    {
        return GetNumberClassificationCombinations(false) > 1;
    }

    /** Indicates whether inner cross-validation folds need to be evaluated. In very simple experiments, there is no reason to use inner folds.
     *
     * @return Whether inner cross-validation folds need to be evaluated.
     * @throws Exception
     */
    public static boolean NeedToEvaluateInnerFolds() throws Exception
    {
        return GetNumberClassificationCombinations(true) > 1;
    }

    /** Indicates whether this is a training/testing experiment.
     *
     * @return Whether this is a training/testing experiment
     * @throws Exception
     */
    public static boolean IsTrainTestExperiment() throws Exception
    {
        return Singletons.Config.GetNumOuterCrossValidationFolds() == 1 || (Singletons.Config.GetTrainingInstanceIDs().size() > 0 && Singletons.Config.GetTestInstanceIDs().size() > 0);
    }
}
