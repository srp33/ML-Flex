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

import mlflex.core.FeatureSelectionAlgorithm;
import mlflex.core.Settings;
import mlflex.core.Singletons;
import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.core.ClassificationAlgorithm;

import java.util.*;

/** This class is used to parse configuration values from configuration files.
 * @author Stephen Piccolo
 */
public class Config
{
    private String _filePath;
    private HashMap<String, String> _configValues = new HashMap<String, String>();

    /** Constructor
     *
     * @param filePath Absolute file path to the configuration file this object will represent
     */
    public Config(String filePath) throws Exception
    {
        _filePath = filePath;
        ParseFromFile();
    }

    /** Retrieves all configuration values from the file and stores them in memory.
     *
     * @throws Exception
     */
    private void ParseFromFile() throws Exception
    {
        String fileText = FileUtilities.ReadTextFile(_filePath);

        for (String line : fileText.split("\n"))
        {
            line = line.trim();
            if (!line.startsWith("#"))
            {
                String[] lineValues = line.split("=");
                String rowKey = lineValues[0].trim();

                String rowValue = lineValues.length == 2 ? lineValues[1].trim() : "";

                if (rowValue.equals(""))
                    Singletons.Log.Debug("An empty value was specified for " + rowKey);

                _configValues.put(rowKey,  rowValue);
            }
        }
    }

    /** Indicates whether a configuration value exists for the specified key.
     *
     * @param key Key to be searched for
     * @return Whether a value exists
     */
    public boolean HasConfigValue(String key)
    {
        return _configValues.containsKey(key);
    }

    /** Retrieves the configuration value from the cache or else sets the default value if it has not been specified previously.
     *
     * @param key Configuration key
     * @param defaultValue Default value that is used if no value is explicitly set
     * @return Configuration value
     * @throws Exception
     */
    private String GetConfigValue(String key, String defaultValue) throws Exception
    {
        if (!_configValues.containsKey(key))
            _configValues.put(key, defaultValue);

        return _configValues.get(key);
    }

    /** Convenience method to get a configuration value that is a String.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public String GetStringValue(String key, String defaultValue) throws Exception
    {
        return GetConfigValue(key, defaultValue);
    }

    /** Convenience method to get a configuration value that is a String. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public String GetMandatoryStringValue(String key) throws Exception
    {
        if (!_configValues.containsKey(key))
            ThrowMissingKeyException(key);

        return GetStringValue(key, "");
    }

    /** Convenience method to get a configuration value that is an integer.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public int GetIntValue(String key, String defaultValue) throws Exception
    {
        return Integer.parseInt(GetConfigValue(key, defaultValue));
    }

    /** Convenience method to get a configuration value that is a long object.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public long GetLongValue(String key, String defaultValue) throws Exception
    {
        return Long.parseLong(GetConfigValue(key, defaultValue));
    }

    /** Convenience method to get a configuration value that is a boolean object.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public boolean GetBooleanValue(String key, String defaultValue) throws Exception
    {
        return Boolean.parseBoolean(GetConfigValue(key, defaultValue));
    }

    /** Convenience method to get a configuration value that is a delimited list of String values.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<String> GetStringListConfigValue(String key, String defaultValue) throws Exception
    {
        String configValue = GetConfigValue(key, defaultValue);

        if (configValue.equals(""))
            return new ArrayList<String>();

        return ListUtilities.GetUniqueValues(ListUtilities.CreateStringList(configValue.split(";")));
    }

    /** Convenience method to get a configuration value that is a list of String values. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<String> GetMandatoryStringListConfigValue(String key) throws Exception
    {
        if (!_configValues.containsKey(key))
            ThrowMissingKeyException(key);

        return GetStringListConfigValue(key, "");
    }

    /** Convenience method to get a configuration value that is a delimited list of integer values.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<Integer> GetIntListConfigValue(String key, String defaultValue) throws Exception
    {
        return new ArrayList<Integer>(new HashSet<Integer>(ListUtilities.CreateIntegerList(GetStringListConfigValue(key, defaultValue))));
    }

    private void ThrowMissingKeyException(String key) throws Exception
    {
        throw new Exception("No config value with key of " + key + " in " + _filePath);
    }

    /** Gets the configuration value indicating how many times the experiment should be performed iteratively.
     *
     * @return Number of iterations to perform
     * @throws Exception
     */
    public int GetNumIterations() throws Exception
    {
        return GetIntValue("NUM_ITERATIONS", "10");
    }

    /** Indicates all the iterations that will be performed in an experiment.
     *
     * @return A list with an item for each iteration.
     * @throws Exception
     */
    public ArrayList<Integer> GetIterations() throws Exception
    {
        return ListUtilities.CreateIntegerSequenceList(1, GetNumIterations());
    }

    /** Gets an array of feature-selection algorithms that should be used for the current experiment.
     *
     * @param processor Data processor containing variables that will be evaluated
     * @return Algorithms to use
     * @throws Exception
     */
    public ArrayList<FeatureSelectionAlgorithm> GetFeatureSelectionAlgorithms(AbstractDataProcessor processor) throws Exception
    {
        ArrayList<String> configValues = GetStringListConfigValue("FEATURE_SELECTION_ALGORITHMS", "");

        // This is the default algorithm for which selection is performed
        FeatureSelectionAlgorithm noneAlgorithm = Settings.FeatureSelectionAlgorithms.get("None");

        ArrayList<FeatureSelectionAlgorithm> algorithms = new ArrayList<FeatureSelectionAlgorithm>();

        // Sort through the configured algorithms and see if you find a match in the general algorithms specified
        for (int i=0; i<configValues.size(); i++)
        {
            if (!Settings.FeatureSelectionAlgorithms.containsKey(configValues.get(i)))
                Singletons.Log.ExceptionFatal("No feature selection algorithm has been configured with a " + configValues.get(i) + " key.");

            // Convert the config value to a typed object
            FeatureSelectionAlgorithm algorithm = Settings.FeatureSelectionAlgorithms.get(configValues.get(i));

            // Find the number-feature options
            ArrayList<Integer> numFeaturesOptions = Singletons.Config.GetNumFeaturesOptions(processor, algorithm);

            // See if we need to perform feature selection or not
            if (numFeaturesOptions.size() == 1 && numFeaturesOptions.get(0) == Singletons.InstanceVault.GetDataPointsForAnalysis(processor).size())
            {
                if (!algorithms.contains(noneAlgorithm))
                    algorithms.add(noneAlgorithm);
            }
            else
                algorithms.add(algorithm);
        }

        // If no algorithms have been specified, indicate None
        if (algorithms.size() == 0)
            algorithms.add(noneAlgorithm);

        return algorithms;
    }

    private ClassificationAlgorithm[] GetClassificationAlgorithms(String key, String defaultValue) throws Exception
    {
        // Retrieve config values
        ArrayList<String> configValues = GetStringListConfigValue(key, defaultValue);

        ClassificationAlgorithm[] algorithms = new ClassificationAlgorithm[configValues.size()];

        for (int i=0; i<configValues.size(); i++)
        {
            if (!Settings.ClassificationAlgorithms.containsKey(configValues.get(i)))
                Singletons.Log.ExceptionFatal(new Exception("No classification algorithm has been configured with a " + configValues.get(i) + " key."));

            // Create a typed object
            algorithms[i] = Settings.ClassificationAlgorithms.get(configValues.get(i));
        }

        return algorithms;
    }

    /** Gets an array of classification algorithms that should be used for the current experiment.
     *
     * @return Algorithms to use
     * @throws Exception
     */
    public ClassificationAlgorithm[] GetMainClassificationAlgorithms() throws Exception
    {
        return GetClassificationAlgorithms("CLASSIFICATION_ALGORITHMS", "");
    }

    /** Gets the classification algorithm that should be used as the second-level classification algorithms in the "stacked" ensemble learner.
     *
     * @return Algorithm to use
     * @throws Exception
     */
    public ClassificationAlgorithm GetStackingClassificationAlgorithm() throws Exception
    {
        return GetClassificationAlgorithms("STACKING_CLASSIFICATION_ALGORITHMS", "weka_decision_tree")[0];
    }

    /** Gets the configuration value for the number of outer cross-validation folds.
     *
     * @return Number of outer cross-validation folds
     * @throws Exception
     */
    public int GetNumOuterCrossValidationFolds() throws Exception
    {
        int configNumFolds = GetIntValue("NUM_OUTER_CROSS_VALIDATION_FOLDS", "10");

        if (configNumFolds < 1)
            return Singletons.InstanceVault.TransformedDependentVariableInstances.Size();

        return configNumFolds;
    }

    /** Gets the configuration value for the number of inner cross-validation folds.
     *
\    * @return Number of inner cross-validation folds
     * @throws Exception
     */
    public int GetNumInnerCrossValidationFolds() throws Exception
    {
        return GetIntValue("NUM_INNER_CROSS_VALIDATION_FOLDS", "10");
    }

    /** Gets the configuration value for the random seed that should be used for assigning cross-validation folds.
     *
     * @return Random seed
     * @throws Exception
     */
    public String GetRandomSeed() throws Exception
    {
        return GetStringValue("RANDOM_SEED", "");
    }

    /** Gets the configuration value indicating any data instances that should be excluded.
     *
     * @return List of instances to exclude
     * @throws Exception
     */
    public ArrayList<String> GetInstanceIDsToExclude() throws Exception
    {
        return ListUtilities.CreateStringList(GetStringListConfigValue("INSTANCE_IDS_TO_EXCLUDE", ""));
    }

    /** Indicates how many training instances should be excluded randomly from the analyses. This strategy can be used for evaluating the effects of outliers, etc.
     *
     * @return Number of training instances to exclude randomly
     * @throws Exception
     */
    public int GetNumTrainingInstancesToExcludeRandomly() throws Exception
    {
        return GetIntValue("NUM_TRAINING_INSTANCES_TO_EXCLUDE_RANDOMLY", "0");
    }

    /** Gets the configuration value indicating any data instances that should be used specifically for training.  It is also possible to specify a path to a text file containing a list of data instance IDs (one on each line).
     *
     * @return Data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetTrainingInstanceIDs() throws Exception
    {
        ArrayList<String> instanceIDs = ListUtilities.GetUniqueValues(ListUtilities.CreateStringList(GetStringListConfigValue("TRAIN_INSTANCE_IDS", "")));

        if (instanceIDs.size() == 1)
        {
            String filePath = instanceIDs.get(0);

            if (FileUtilities.FileExists(filePath))
                instanceIDs = FileUtilities.ReadLinesFromFile(filePath);
            else
                throw new Exception("A single value cannot be specified for TRAIN_INSTANCE_IDS unless it is a file name containing a list of training instance IDs. A file with the path " + instanceIDs.get(0) + " does not exist.");
        }

        return instanceIDs;
    }

    /** Gets the configuration value indicating any data instances that should be used specifically for testing. If this is not specified, they will be assigned randomly. It is also possible to specify a path to a text file containing a list of data instance IDs (one on each line).
     *
     * @return Data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetTestInstanceIDs() throws Exception
    {
        ArrayList<String> instanceIDs = ListUtilities.GetUniqueValues(ListUtilities.CreateStringList(GetStringListConfigValue("TEST_INSTANCE_IDS", "")));

        if (instanceIDs.size() == 1)
        {
            String filePath = instanceIDs.get(0);

            if (FileUtilities.FileExists(filePath))
                instanceIDs = FileUtilities.ReadLinesFromFile(filePath);
            else
                throw new Exception("A single value cannot be specified for TEST_INSTANCE_IDS unless it is a file name containing a list of test instance IDs. A file with the path " + instanceIDs.get(0) + " does not exist.");
        }

        return instanceIDs;
    }

    /** Gets the number of feature options that should be used.
     *
     * @param processor Data processor
     * @param algorithm Feature selection algorithm
     * @return List of options
     * @throws Exception
     */
    public ArrayList<Integer> GetNumFeaturesOptions(AbstractDataProcessor processor, FeatureSelectionAlgorithm algorithm) throws Exception
    {
        if (algorithm.IsNone())
            return ListUtilities.CreateIntegerList(Singletons.InstanceVault.GetDataPointsForAnalysis(processor).size());

        if (algorithm.IsPriorKnowledge())
            return ListUtilities.CreateIntegerList(processor.GetPriorKnowledgeSelectedFeatures().size());

        int numDataPoints = Singletons.InstanceVault.GetDataPointsForAnalysis(processor).size();

        HashSet<Integer> options = new HashSet<Integer>();

        for (int numFeatures : GetIntListConfigValue("NUM_FEATURES_OPTIONS", String.valueOf(numDataPoints)))
        {
            if (numDataPoints >= numFeatures && numFeatures > 0)
                options.add(numFeatures);
            if (numFeatures <= 0)
                options.add(numDataPoints);
        }

        if (options.size() == 0)
            options.add(numDataPoints);

        ArrayList<Integer> numFeaturesOptions = new ArrayList<Integer>(options);
        Collections.sort(numFeaturesOptions);

        return numFeaturesOptions;
    }

    /** This method overrides the default implementation of toString. It creates a list of all experiment configuration values that have been specified explicitly or that are specified in the code by default.
     *
     * @return String representation of all configuration values
     */
    @Override
    public String toString()
    {
        StringBuffer output = new StringBuffer();

        for (String configKey : ListUtilities.SortStringList(new ArrayList<String>(_configValues.keySet())))
            if (!_configValues.get(configKey).equals(""))
                output.append(configKey + ": " + _configValues.get(configKey) + "\n");

        return output.toString();
    }

    /** Indicates whether ML-Flex is executing its first iteration.
     *
     * @return Whether or not it is the first iteration
     * @throws Exception
     */
    public static boolean IsFirstIteration() throws Exception
    {
        return Singletons.Iteration == 1;
    }

    /** Indicates whether ML-Flex is executing its final (and possibly only) iteration.
     *
     * @return Whether or not it is the last iteration
     * @throws Exception
     */
    public static boolean IsLastIteration() throws Exception
    {
        return Singletons.Iteration == Singletons.Config.GetNumIterations();
    }
}