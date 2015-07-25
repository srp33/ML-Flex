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

package mlflex.parallelization;

import mlflex.core.*;
import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.ensemblelearners.AbstractEnsembleLearner;
import mlflex.evaluation.ClassificationResultsSaver;
import mlflex.evaluation.FeatureSelectionResultsSaver;
import mlflex.evaluation.HtmlReportGenerator;
import mlflex.helper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

/** This class contains methods that encapsulate logic for dividing an experiment into smaller, parallelizable tasks. The purpose of the class is to keep all that logic in one place rather than have it dispersed throughout the code. */
public class TaskGenerator
{
    /** Contains code for determining an experiment-wide random seed.
     *
     * @param randomSeedFilePath Path where the random seed will be stored
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetRandomSeedTask(final String randomSeedFilePath)
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        callables.add(new LockedCallable("Set random seed", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                // The default seed is the current iteration number
                String randomSeed = String.valueOf(Singletons.Iteration);

                // See if a config value has been specified explicitly
                String configValue = Singletons.Config.GetRandomSeed();

                // If the value is zero, then the seed is generated randomly
                if (configValue.equals("0"))
                    randomSeed = String.valueOf(new Random().nextLong());

                // If a seed is specified in config, use that
                if (!configValue.equals("") && DataTypeUtilities.IsInteger(configValue))
                    randomSeed = configValue;

                // Save the random seed
                String outText = "# This value is the random seed that was used when ML-Flex needed to generate random values or shuffle a list of values (for example, cross-validation fold assignments).\n" + randomSeed.toString();
                FileUtilities.WriteTextToFile(randomSeedFilePath, outText);

                // Make sure it got saved properly
                return randomSeed.equals(FileUtilities.ReadScalarFromFile(randomSeedFilePath));
            }
        }));

        return callables;
    }

    /** Constructs tasks that delete all files related to a task. Because this executes in parallel, it should be much faster than cleaning up at the command line.
     *
     * @return Executable tasks
     */
    public static ArrayList<Callable<Object>> GetResetTasks()
    {
        ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();

        // Add a task for each directory
        for (final String directoryPath : ListUtilities.CreateStringList(Settings.FEATURE_SELECTION_DIR, Settings.PREDICTIONS_DIR, Settings.ENSEMBLE_PREDICTIONS_DIR, Settings.OUTPUT_DIR, Settings.STATUS_DIR, Settings.DATA_DIR, Settings.TEMP_DATA_DIR, Settings.TEMP_RESULTS_DIR, Settings.LOCKS_DIR))
        {
            tasks.add(new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    MiscUtilities.DeleteCoreDirectory(directoryPath);
                    return null;
                }
            });
        }

        return tasks;
    }

    /** Constructs tasks for processing raw metadata.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetProcessRawMetadataTasks()
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final AbstractMetadataProcessor processor : Singletons.ProcessorVault.MetadataProcessors)
        {
            callables.add(new LockedCallable("Metadata/" + processor.GetDescription(), "Save metadata for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.Save();
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for processing raw analysis data.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetProcessRawAnalysisDataTasks()
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
        {
            callables.add(new LockedCallable("RawAnalysisData/" + processor.GetDescription(), "Process raw analysis data for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.ProcessInputData();
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for saving experiment description files.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveExperimentDescriptionFilesTasks() throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        callables.add(new LockedCallable("Description/Save miscellaneous description files", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                new DescriptionFileSaver().SaveExperimentDescriptionFiles();
                return Boolean.TRUE;
            }
        }));

        return callables;
    }

    /** Constructs tasks for saving statistics for each data processor.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveStatisticsTasks()
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final AbstractDataProcessor processor : Singletons.ProcessorVault.AllDataProcessors)
        {
            callables.add(new LockedCallable("Statistics/" + processor.GetDescription(), "Save statistics for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.SaveStatistics();
                }
            }));
        }

        // If there is more than one data processor, record statistics that describe all
        if (Singletons.ProcessorVault.IndependentVariableDataProcessors.size() > 1)
            callables.add(new LockedCallable("Statistics/All", "Save statistics across all independent variable processors", new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return AbstractDataProcessor.SaveStatisticsAcrossAllIndependentVariableProcessors();
                }
            }));

        return callables;
    }

    /** Constructs tasks for selecting features.
     *
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSelectFeaturesTasks(boolean evaluateInner) throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        // First determine whether classification is being performed in this experiment
        if (!Settings.NeedToClassify())
            return callables;

        // Add tasks for each combination for which feature selection is necessary
        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            for (FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                if (FeatureSelectionEvaluator.NeedToSelectFeatures(processor, fsAlgorithm))
                    for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
                        callables.addAll(new FeatureSelectionEvaluator(processor, fsAlgorithm, outerFold).GetSelectFeatureTasks(evaluateInner));

        return callables;
    }

    /** Constructs tasks for making predictions via classification.
     *
     * @param modelSelectors List of model selectors that contain information about processing the tasks.
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetMakePredictionsTasks(ArrayList<ModelSelector> modelSelectors, boolean evaluateInner) throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (ModelSelector selector : modelSelectors)
            for (PredictionEvaluator evaluator : selector.PredictionEvaluators)
                callables.addAll(evaluator.GetPredictionTasks(evaluateInner, true));

        return callables;
    }

    /** Constructs tasks for making ensemble predictions.
     *
     * @param modelSelectors List of model selectors that contain information about processing the tasks.
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetMakeEnsemblePredictionsTasks(final ArrayList<ModelSelector> modelSelectors, final boolean evaluateInner) throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final Integer outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
        {
            callables.add(new LockedCallable("EnsemblePredictions_OuterFold" + outerFold, "Make ensemble predictions for outer fold " + outerFold, new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    // This step pulls the predictions that have already been made so they can be aggregated for the ensemble learnerrs
                    HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap = AbstractEnsembleLearner.GetInstanceEnsemblePredictionInfos(outerFold, modelSelectors, evaluateInner);

                    for (final AbstractEnsembleLearner ensemblePredictor : AbstractEnsembleLearner.GetAllEnsembleLearners())
                    {
                        FileUtilities.CreateDirectoryNoFatalError(ensemblePredictor.GetSaveDirectory(outerFold));

                        if (!ensemblePredictor.MakeEnsemblePredictions(outerFold, ensemblePredictionInfoMap))
                            return Boolean.FALSE;
                    }

                    return Boolean.TRUE;
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for saving feature selection results.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveFeatureSelectionResultsTasks() throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        // First check whether classification was performed
        if (!Settings.NeedToClassify())
            return callables;

        for (final AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            for (final FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                if (FeatureSelectionEvaluator.NeedToSelectFeatures(processor, fsAlgorithm))
                    callables.add(new LockedCallable("FeatureSelectionResults/SaveMeanFeatureRanks/" + processor.GetDescription() + "/" + fsAlgorithm, "Save mean feature ranks for " + processor.GetDescription() + " " + fsAlgorithm.Key, new Callable<Object>()
                    {
                        public Object call() throws Exception
                        {
                            new FeatureSelectionResultsSaver(processor, fsAlgorithm).SaveMeanFeatureRanksFile();
                            return Boolean.TRUE;
                        }
                    }));

        return callables;
    }

    /** Constructs tasks for saving classification results.
     *
     * @param modelSelectors List of model selectors that contain information about processing the tasks.
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveClassificationResultsTasks(ArrayList<ModelSelector> modelSelectors, final boolean evaluateInner) throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final ModelSelector modelSelector : modelSelectors)
        {
            callables.add(new LockedCallable("ClassificationResults/" + modelSelector.GetDescription(), "Save classification results for " + modelSelector.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    new ClassificationResultsSaver(modelSelector,  evaluateInner).SaveClassificationResults();
                    return Boolean.TRUE;
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for saving ensemble classification results.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveEnsembleResultsTasks() throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final AbstractEnsembleLearner ensemblePredictor : AbstractEnsembleLearner.GetAllEnsembleLearners())
        {
            callables.add(new LockedCallable("EnsembleResults/" + ensemblePredictor.GetDescription(), "Save ensemble results " + ensemblePredictor.GetFormattedDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ClassificationResultsSaver.SaveEnsembleResults(ensemblePredictor.GetEnsemblePredictions(), ensemblePredictor.GetResultsFilePaths(true));
                    return Boolean.TRUE;
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for saving multi-iteration classification results summaries.
     *
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveMultiIterationResultsSummaryTasks(final boolean evaluateInner) throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        // This is for each combination of data processor and algorithms
        for (final ModelSelector modelSelector : ModelSelector.GetAllModelSelectors())
        {
            callables.add(new LockedCallable("MultiIterationClassificationResultsSummary/" + modelSelector.GetDescription(), "Save multi-iteration classification results summaries for " + modelSelector.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ArrayList<Boolean> isForTrainingDataOptions = ListUtilities.CreateBooleanList(false);

                    if (evaluateInner && Settings.IsTrainTestExperiment())
                        isForTrainingDataOptions.add(true);

                    for (boolean isForTrainingData : isForTrainingDataOptions)
                        new ClassificationResultsSaver(modelSelector, evaluateInner).SaveMultiIterationClassificationResults(isForTrainingData);

                    return Boolean.TRUE;
                }
            }));
        }

        // This is for the ensemble learners
        for (final AbstractEnsembleLearner ensembleLearner : AbstractEnsembleLearner.GetAllEnsembleLearners())
        {
            callables.add(new LockedCallable("MultiIterationEnsembleResultsSummary/" + ensembleLearner.GetDescription(), "Save multi-iteration ensemble results summary for " + ensembleLearner.GetFormattedDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ClassificationResultsSaver.SaveMultiIterationEnsembleResults(ensembleLearner);
                    return Boolean.TRUE;
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for saving overall results summaries.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveOverallResultsSummariesTasks() throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        callables.add(new LockedCallable("Save overall results summaries", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                ClassificationResultsSaver.SaveOverallResultsSummaries();
                return Boolean.TRUE;
            }
        }));


        return callables;
    }

    /** Constructs tasks for saving reports of the experiment.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetSaveReportTasks() throws Exception
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        callables.add(new LockedCallable("Save report", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                new HtmlReportGenerator().Save();
                return Boolean.TRUE;
            }
        }));

        return callables;
    }

    /** Constructs tasks for exporting data files.
     *
     * @return Executable tasks
     */
    public static ArrayList<LockedCallable> GetExportDataFilesTasks()
    {
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        for (final AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
        {
            // Create tab delimited files
            callables.add(new LockedCallable("ExportData/CreateTabDelimited/" + processor.GetDescription(), "Export tab-delimited file for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    Singletons.InstanceVault.GetInstancesForAnalysis(processor).SaveToFile(Settings.GetOutputExportDir(), processor.GetDescription());
                    return Boolean.TRUE;
                }
            }));

            // Create ARFF files
            callables.add(new LockedCallable("ExportData/CreateArff/" + processor.GetDescription(), "Export ARFF file for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    new AnalysisFileCreator(Settings.GetOutputExportDir(), processor.GetDescription(), Singletons.InstanceVault.GetInstancesForAnalysis(processor), null, true).CreateArffFile();
                    return Boolean.TRUE;
                }
            }));
        }

        return callables;
    }

    /** Constructs tasks for indicating that the current iteration is complete.
     *
     * @return Executable tasks
     */
    public static LockedCallable GetIterationCompleteCallable() throws Exception
    {
        return new LockedCallable("Indicate iteration complete", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                return Boolean.TRUE;
            }
        });
    }
}
