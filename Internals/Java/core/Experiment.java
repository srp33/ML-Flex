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

import mlflex.Action;
import mlflex.helper.Config;
import mlflex.helper.FileUtilities;
import mlflex.helper.Vacuum;
import mlflex.parallelization.LockedCallable;
import mlflex.parallelization.MultiThreadedTaskHandler;
import mlflex.parallelization.TaskGenerator;

import java.util.ArrayList;

/** This class contains high-level commands for executing tasks within a given experiment. It makes sure steps are performed in the correct order and verifies that all prerequisite steps are performed before moving to the next step.
 *
 * @author Stephen Piccolo
 */
public class Experiment
{
    /** Name of the experiment. */
    public String Name;

    /** Constructor used to initialize an experiment.
     *
     * @param name Short name of the experiment
     */
    public Experiment(String name)
    {
        Name = name;
    }

    /** This method orchestrates the computational tasks that will be performed for an experiment, depending on what is requested by the user. It is also intended to provide a high-level view of the workflow that is followed in an experiment.
     *
     * @param action The action to be orchestrated
     * @throws Exception
     */
    public void Orchestrate(Action action) throws Exception
    {
        // Removes any files that were created in a previous run of the experiment
        if (action.equals(Action.Reset))
        {
            if (Config.IsLastIteration()) // Only do the reset one time if multiple iterations are specified
                Reset();

            return;
        }

        // Short circuit the current iteration of the experiment if it has already been completed
        if (IsIterationComplete())
        {
            PrintIterationCompleteMessage();
            return;
        }

        // Set and save random seed so that all nodes use the same random seed for each iteration.
        SetRandomSeed();

        // The following steps prepare the data that will be used for a given experiment. Because the same data will be used in all iterations, these steps need only be performed on the first iteration.
        if (Config.IsFirstIteration())
        {
            ProcessRawMetadata();
            ProcessRawAnalysisData();
        }

        // Load data into memory before performing evaluation steps
        Singletons.InstanceVault.LoadDataInstancesIntoMemory();

        // Initialize cross-validation assignments
        Singletons.InstanceVault.GetCrossValidationAssignments(true);

        if (Config.IsFirstIteration())
        {
            // This saves summary information about the data
            SaveStatistics();

            // This exports the data in various formats
            ExportDataFiles();
        }

        // Save files that describe the settings used for this experiment
        SaveExperimentDescriptionFiles();

        // Determine the models that need to be generated across all algorithms, cross-validation folds, numbers of features, etc.
        ArrayList<ModelSelector> modelSelectors = ModelSelector.GetAllModelSelectors();

        // Do we need to evaluate inner cross-validation folds?
        boolean evaluateInner = Settings.NeedToEvaluateInnerFolds();

        // Perform feature selection
        SelectFeatures(evaluateInner);

        // Perform classification for individual learners
        MakePredictions(modelSelectors, evaluateInner);

        // Perform classification for ensemble learners
        MakeEnsemblePredictions(modelSelectors, evaluateInner);

        // Calculate and save results for individual data / algorithm combinations
        SaveFeatureSelectionResults();
        SaveClassificationResults(modelSelectors, evaluateInner);
        SaveEnsembleResults();

        // Calculate and save multi-iteration results summaries (when there are multiple iterations)
        if (Singletons.Config.GetNumIterations() > 1 && Config.IsLastIteration())
            SaveMultiIterationResultsSummaries(evaluateInner);

        if (Config.IsLastIteration())
        {
            // Save overall results summaries
            SaveOverallResultsSummaries();

            // Generate final report
            SaveReport();

            // Clean up any temporary files, etc. that are inadvertently left over
            new Vacuum().Clean();
        }

        IndicateIterationComplete();
    }

    /** This method deletes any files that might exist from previous runs of this experiment so the current experiment can start from scratch.
     *
     * @throws Exception
     */
    private void Reset() throws Exception
    {
        new MultiThreadedTaskHandler("reset").Add(TaskGenerator.GetResetTasks()).Execute();
    }

    /** This method explains to the user that the current iteration of the experiment has previously been completed. If it is the last (or only) iteration, the user is advised to reset the experiment.
     *
     * @throws Exception
     */
    private void PrintIterationCompleteMessage() throws Exception
    {
        String allCompleteMessage = "This experiment was previously completed successfully. To re-run it, you must first run the experiment with ACTION=Reset specified at the command line. This will delete results from the previous execution. Then run the experiment with ACTION=Process specified.";
        String iterationCompleteMessage = "Iteration " + Singletons.Iteration + " was previously completed successfully.";

        if (Config.IsLastIteration())
            Singletons.Log.Info(allCompleteMessage);
        else
            Singletons.Log.Info(iterationCompleteMessage);
    }

    /** This method sets the random seed that is used for this iteration of the experiment. By using the locking mechanism, it ensures that the seed is the same across all compute nodes/threads.
     *
     * @throws Exception
     */
    private void SetRandomSeed() throws Exception
    {
        // Specify file path where random seed will be saved
        String randomSeedFilePath = Settings.GetOutputSettingsDir(true) + "Random_Seed.txt";

        // Save random seed if it hasn't already been saved
        MultiThreadedTaskHandler.ExecuteLockTasks("Set random seed", TaskGenerator.GetRandomSeedTask(randomSeedFilePath));

        // Get the saved seed
        Singletons.RandomSeed = Long.parseLong(FileUtilities.ReadScalarFromFile(randomSeedFilePath));
    }

    /** Processes any raw metadata that have been specified by the user for this experiment.
     *
     * @throws Exception
     */
    private void ProcessRawMetadata() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Process raw metadata", TaskGenerator.GetProcessRawMetadataTasks());
    }

    /** Parses and filters and saves all raw analysis data into the ML-Flex native format.
     *
     * @throws Exception
     */
    private void ProcessRawAnalysisData() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Process raw analysis data", TaskGenerator.GetProcessRawAnalysisDataTasks());
    }

    /** Saves files that describe the experiment being executed. These files indicate experiment and configuration settings, cross-validation assignments, etc.
     *
     * @throws Exception
     */
    private void SaveExperimentDescriptionFiles() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Save experiment description files", TaskGenerator.GetSaveExperimentDescriptionFilesTasks());
    }

    /** Saves summary information for each data processor.
     *
     * @throws Exception
     */
    private void SaveStatistics() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Save statistics", TaskGenerator.GetSaveStatisticsTasks());
    }

    /** Performs feature selection across all algorithms and data processors that have been specified.
     *
     * @param evaluateInner Whether feature selection needs to be performed for inner cross-validation folds
     * @throws Exception
     */
    private void SelectFeatures(boolean evaluateInner) throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Select features", TaskGenerator.GetSelectFeaturesTasks(evaluateInner));
    }

    /** Makes predictions (classifies) for all algorithms and data processors that have been specified.
     *
     * @param modelSelectors List of model selectors that indicate combinations of algorithms and data processors
     * @param evaluateInner Whether classification needs to be performed for inner cross-validation folds
     * @throws Exception
     */
    private void MakePredictions(ArrayList<ModelSelector> modelSelectors, boolean evaluateInner) throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Make predictions", TaskGenerator.GetMakePredictionsTasks(modelSelectors, evaluateInner));
    }

    /** Makes ensemble learner predictions for all ensemble learners that have been specified.
     *
     * @param modelSelectors List of model selectors that indicate combinations of algorithms and data processors
     * @param evaluateInner Whether classification needs to be performed for inner cross-validation folds
     * @throws Exception
     */
    private void MakeEnsemblePredictions(ArrayList<ModelSelector> modelSelectors, boolean evaluateInner) throws Exception
    {
        if (Settings.NeedToEnsembleLearn())
            MultiThreadedTaskHandler.ExecuteLockTasks("Make ensemble predictions", TaskGenerator.GetMakeEnsemblePredictionsTasks(modelSelectors, evaluateInner));
    }

    /** Saves feature selection results.
     *
     * @throws Exception
     */
    private void SaveFeatureSelectionResults() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Save feature selection results", TaskGenerator.GetSaveFeatureSelectionResultsTasks());
    }

    /** Saves results for individual learners.
     *
     * @param modelSelectors List of model selectors that indicate combinations of algorithms and data processors
     * @param evaluateInner Whether classification needs to be performed for inner cross-validation folds
     * @throws Exception
     */
    private void SaveClassificationResults(ArrayList<ModelSelector> modelSelectors, boolean evaluateInner) throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Save classification results", TaskGenerator.GetSaveClassificationResultsTasks(modelSelectors, evaluateInner));
    }

    /** Saves results for ensemble learners.
     *
     * @throws Exception
     */
    private void SaveEnsembleResults() throws Exception
    {
        if (Settings.NeedToEnsembleLearn())
            MultiThreadedTaskHandler.ExecuteLockTasks("Save ensemble results", TaskGenerator.GetSaveEnsembleResultsTasks());
    }

    /** Saves summary files that provide an overview of results that span all iterations (if more than one was executed).
     *
     * @param evaluateInner Whether classification needs to be performed for inner cross-validation folds
     * @throws Exception
     */
    private void SaveMultiIterationResultsSummaries(boolean evaluateInner) throws Exception
    {
        if (Settings.NeedToClassify())
            MultiThreadedTaskHandler.ExecuteLockTasks("Save multi-iteration classification results summaries", TaskGenerator.GetSaveMultiIterationResultsSummaryTasks(evaluateInner));
    }

    /** Saves overall results summaries across all combinations of data processor, feature selection algorithm, and classification algorithm.
     *
     * @throws Exception
     */
    private void SaveOverallResultsSummaries() throws Exception
    {
        if (Settings.NeedToClassify())
            MultiThreadedTaskHandler.ExecuteLockTasks("Save overall results summaries", TaskGenerator.GetSaveOverallResultsSummariesTasks());
    }

    /** Saves an HTML-formatted report that makes it easy for the user to access all results for this experiment.
     *
     * @throws Exception
     */
    private void SaveReport() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Save report", TaskGenerator.GetSaveReportTasks());
    }

    /** Exports data in various formats.
     *
     * @throws Exception
     */
    private void ExportDataFiles() throws Exception
    {
        // If exporting is enabled via a command-line option or if there are no classification algorithms specified, then export data
        if (Settings.EXPORT_DATA || !Settings.NeedToClassify())
            MultiThreadedTaskHandler.ExecuteLockTasks("Export data files", TaskGenerator.GetExportDataFilesTasks());
    }

    /** Indicate that the current iteration of this experiment has been completed successfully. This speeds up experiments that cover multiple iterations.
     *
     * @throws Exception
     */
    private void IndicateIterationComplete() throws Exception
    {
        MultiThreadedTaskHandler.ExecuteLockTasks("Indicate iteration complete", LockedCallable.CreateLockedCallableList(TaskGenerator.GetIterationCompleteCallable()));
    }

    /** Indicates whether the current iteration of this experiment has been completed successfully.
     *
     * @return Whether the current iteration of this experiment has been completed successfully.
     * @throws Exception
     */
    private boolean IsIterationComplete() throws Exception
    {
        return TaskGenerator.GetIterationCompleteCallable().IsDone();
    }

    @Override
    public String toString()
    {
        return Name + "_Experiment";
    }
}