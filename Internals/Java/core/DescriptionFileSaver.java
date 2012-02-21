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

import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.io.File;
import java.util.ArrayList;

/** This class is used to save output files that describe an experiment. These files help the user interpret how an experiment was performed without having to look at the internal files.
 */
public class DescriptionFileSaver
{
    /** This method saves various description files to the output. These tasks are quick to execute, so they are grouped together rather than parallelized.
     *
     * @throws Exception
     */
    public void SaveExperimentDescriptionFiles() throws Exception
    {
        // Save the version file in output so people know which version was used
        FileUtilities.CopyFile(Settings.VERSION_FILE, Settings.GetOutputSettingsDir(false) + Settings.VERSION_FILE);

        // Copy the experiment file
        FileUtilities.WriteLineToFile(Settings.GetOutputSettingsDir(false) + "Experiment_Settings.txt", "# All settings used in this experiment, whether explicitly set or used by default in the absence of an explicit setting");
        FileUtilities.AppendTextToFile(Settings.GetOutputSettingsDir(false) + "Experiment_Settings.txt", Singletons.Config.toString());

        if (Settings.NeedToClassify())
        {
            // Copy the algorithm configuration files
            FileUtilities.CopyFile(Settings.LEARNER_TEMPLATES_FILE, Settings.GetOutputSettingsDir(false) + new File(Settings.LEARNER_TEMPLATES_FILE).getName());

            // Copy the algorithm configuration files
            FileUtilities.CopyFile(Settings.CLASSIFICATION_ALGORITHMS_FILE, Settings.GetOutputSettingsDir(false) + new File(Settings.CLASSIFICATION_ALGORITHMS_FILE).getName());

            if (Settings.NeedToSelectFeatures())
                FileUtilities.CopyFile(Settings.FEATURE_SELECTION_ALGORITHMS_FILE, Settings.GetOutputSettingsDir(false) + new File(Settings.FEATURE_SELECTION_ALGORITHMS_FILE).getName());

            SaveExcludedTrainingIDInfo();
            SaveCrossValidationAssignments();
        }
    }

    private void SaveExcludedTrainingIDInfo() throws Exception
    {
        ArrayList<String> excludedTrainingIDs = Singletons.InstanceVault.GetCrossValidationAssignments().GetAllExcludedTrainIDs();
        if (excludedTrainingIDs.size() > 0)
        {
            String filePath = Settings.GetOutputSettingsDir(true) + "Excluded_Training_IDs.txt";
            String output = ListUtilities.Join(excludedTrainingIDs, "\n");
            FileUtilities.WriteLineToFile(filePath, "# IDs of data instances that were excluded randomly from training sets, according to the specified experiment settings. If this is a cross-validation experiment, the instances were removed from different folds.");
            FileUtilities.AppendLineToFile(filePath, output);
        }
    }

    private void SaveCrossValidationAssignments() throws Exception
    {
        String outerFoldsFilePath = Settings.GetOutputSettingsDir(true) + "Validation_Assignments_for_Outer_Folds.txt";

        FileUtilities.WriteTextToFile(outerFoldsFilePath, "# The outer cross-validation fold to which each data instance was assigned.\n");
        FileUtilities.AppendTextToFile(outerFoldsFilePath, Singletons.InstanceVault.GetCrossValidationAssignments().toString());

        if (Singletons.InstanceVault.GetCrossValidationAssignments().NumFolds != Singletons.InstanceVault.TransformedDependentVariableInstances.Size())
            for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            {
                String innerFoldFilePath = Settings.GetOutputSettingsDir(true) + "Validation_Assignments_for_Inner_Folds_in_OuterFold_" + outerFold + ".txt";
                FileUtilities.WriteTextToFile(innerFoldFilePath, "# The inner cross-validation fold (within outer fold " + outerFold + ") to which data instances were assigned.\n");
                FileUtilities.AppendTextToFile(innerFoldFilePath, Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(outerFold).toString());
            }
    }
}
