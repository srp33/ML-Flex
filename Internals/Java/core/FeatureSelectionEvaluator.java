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
import mlflex.parallelization.LockedCallable;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/** This class coordinates the process of selecting/ranking features for each combination of data processor, feature-selection algorithm, and cross-validation fold.
 * @author Stephen Piccolo
 */
public class FeatureSelectionEvaluator
{
    /** The data processor that is being worked with */
    public final AbstractDataProcessor Processor;
    /** The algorithm that will be used for feature selection */
    public final FeatureSelectionAlgorithm SelectionAlgorithm;
    /** The cross-validation fold number */
    public final int OuterFold;

    private ArrayList<String> _allFeatures;

    /** Constructor
     *
     * @param processor Data processor containing data to be evaluated
     * @param fsAlgorithm Feature selection/ranking algorithm
     * @param outerFold Number of outer cross-validation fold
     */
    public FeatureSelectionEvaluator(AbstractDataProcessor processor, FeatureSelectionAlgorithm fsAlgorithm, int outerFold) throws Exception
    {
        Processor = processor;
        SelectionAlgorithm = fsAlgorithm;
        OuterFold = outerFold;

        _allFeatures = Singletons.InstanceVault.GetDataPointsForAnalysis(Processor);
    }

    private String GetSaveDirectory()
    {
        return Settings.FEATURE_SELECTION_DIR + Processor + "/" + SelectionAlgorithm + "/OuterFold" + OuterFold + "/";
    }

    private String GetOuterSaveFilePath()
    {
        return GetSaveDirectory() + GetOuterFoldFileName();
    }

    private String GetInnerSaveFilePath(int innerFold)
    {
        return GetSaveDirectory() + GetInnerFoldFileName(innerFold);
    }

    private String GetStatusFilePrefix()
    {
        return GetSaveDirectory().replace(Settings.FEATURE_SELECTION_DIR, "SelectedFeatures/");
    }

    private String GetOuterStatusFilePath()
    {
        return GetStatusFilePrefix() + GetOuterFoldFileName();
    }

    private String GetInnerStatusFilePath(int innerFold)
    {
        return GetStatusFilePrefix() + GetInnerFoldFileName(innerFold);
    }

    /** Name of the file where the outer cross-validation results will be stored.
     *
     * @return File name
     */
    private String GetOuterFoldFileName()
    {
        return "OuterFold_SelectedFeatures.txt";
    }

    /** Name of the file where the inner cross-validation results will be stored for a given cross-validation fold.
     *
     * @param innerFold Number of inner cross-validation fold
     * @return File name
     */
    private String GetInnerFoldFileName(int innerFold)
    {
        return "InnerFold" + innerFold + "_SelectedFeatures.txt";
    }

    private DataInstanceCollection GetOuterTrainingInstances() throws Exception
    {
        return Singletons.InstanceVault.GetCrossValidationAssignments().GetTrainInstances(Processor, OuterFold);
    }

    private DataInstanceCollection GetInnerTrainingInstances(int innerFold) throws Exception
    {
        return Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTrainInstances(Processor, innerFold);
    }

    /** This method performs the work of selecting/ranking features for the inner cross-validation folds and sharing the load across multiple threads and computing nodes (where applicable).
     * @param evaluateInner Whether to evaluate inner cross-validation folds
     * @return Callable tasks that need to be executed
     * @throws Exception
     */
    public ArrayList<LockedCallable> GetSelectFeatureTasks(boolean evaluateInner) throws Exception
    {
        // Initialize the directory
        FileUtilities.CreateDirectoryNoFatalError(GetSaveDirectory());

        // This object will store the parallelizable tasks
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        if (evaluateInner)
        {
            for (final int innerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
            {
                // Add a task for a given inner cross-validation fold
                callables.add(new LockedCallable(GetInnerStatusFilePath(innerFold), "Select features for " + GetDescription() + ", outerFold=" + OuterFold + ", innerFold=" + innerFold, new Callable<Object>()
                {
                    public Object call() throws Exception
                    {
                        return SelectAndSaveFeatures(GetInnerTrainingInstances(innerFold), GetInnerSaveFilePath(innerFold));
                    }
                }));
            }
        }

        // Add a task for the outer cross-validation fold
        callables.add(new LockedCallable(GetOuterStatusFilePath(), "Select features for " + GetDescription() + ", outer fold=" + OuterFold, new Callable<Object>()
        {
            public Object call() throws Exception
            {
                return SelectAndSaveFeatures(GetOuterTrainingInstances(), GetOuterSaveFilePath());
            }
        }));

        return callables;
    }

    /** This method does the actual work of selecting features and saving the results to a file
     *
     * @param trainInstances Data instances used for training
     * @param filePath Path where the results will be stored
     * @return Whether features were successfully selected and stored
     * @throws Exception
     */
    private Boolean SelectAndSaveFeatures(DataInstanceCollection trainInstances, String filePath) throws Exception
    {
        ArrayList<String> selectedFeatures = SelectionAlgorithm.SelectFeatures(trainInstances);
        FileUtilities.WriteLineToFile(filePath, ListUtilities.Join(selectedFeatures, ","));

        return selectedFeatures.equals(GetSelectedFeatures(filePath));
    }

    /** This method retrieves the features that have already been selected and saved. It retrieves all features.
     *
     * @param filePath Path where the file may be saved
     * @return Features in ranked order
     * @throws Exception
     */
    private ArrayList<String> GetSelectedFeatures(String filePath) throws Exception
    {
        return GetSelectedFeatures(filePath, -1);
    }

    /** This method retrieves the features that have already been selected and saved. It retrieves only the specified number of top-ranked features.
     *
     * @param filePath Path where the file may be saved
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    private ArrayList<String> GetSelectedFeatures(String filePath, int numTop) throws Exception
    {
        if (!FeatureSelectionEvaluator.NeedToSelectFeatures(Processor, SelectionAlgorithm) && !SelectionAlgorithm.IsPriorKnowledge())
            return new ArrayList<String>(_allFeatures);

        if (SelectionAlgorithm.IsPriorKnowledge())
            return Processor.GetPriorKnowledgeSelectedFeatures();

        if (!FileUtilities.FileExists(filePath))
            return new ArrayList<String>();

        ArrayList<String> selectedFeatures = ListUtilities.CreateStringList(FileUtilities.ReadTextFile(filePath).trim().split(","));

        if (numTop > selectedFeatures.size() || numTop < 1)
            return selectedFeatures;

        return ListUtilities.Subset(selectedFeatures, 0, numTop);
    }

    /** This method retrieves the features that have already been selected and saved for the outer cross-validation fold. It retrieves only the specified number of top-ranked features.
     *
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    public ArrayList<String> GetOuterSelectedFeatures(int numTop) throws Exception
    {
        return GetSelectedFeatures(GetOuterSaveFilePath(), numTop);
    }

    /** This method retrieves the features that have already been selected and saved for the specified inner cross-validation fold. It retrieves only the specified number of top-ranked features.
     *
     * @param innerFold Number of inner cross-validation fold
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    public ArrayList<String> GetInnerSelectedFeatures(int innerFold, int numTop) throws Exception
    {
        return GetSelectedFeatures(GetInnerSaveFilePath(innerFold), numTop);
    }

    /** Returns a description of this object.
     *
     * @return Description
     */
    public String GetDescription()
    {
        return Processor.GetDescription() + "_" + SelectionAlgorithm;
    }

    /** Indicates whether feature selection needs to be performed for a given combination of data processor and feature-selection algorithm.
     *
     * @param processor Data processor
     * @param fsAlgorithm Feature selection algorithm
     * @return Whether feature selection should be performed
     * @throws Exception
     */
    public static boolean NeedToSelectFeatures(AbstractDataProcessor processor, FeatureSelectionAlgorithm fsAlgorithm) throws Exception
    {
        ArrayList<Integer> numFeaturesOptions = Singletons.Config.GetNumFeaturesOptions(processor, fsAlgorithm);

        // If all features will be used in classification models, then no need to perform feature selection
        if (fsAlgorithm.IsNone() || fsAlgorithm.IsPriorKnowledge() || (numFeaturesOptions.size() == 1 && numFeaturesOptions.get(0) == Singletons.InstanceVault.GetDataPointsForAnalysis(processor).size()))
            return false;

        return true;
    }
}