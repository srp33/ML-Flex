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

package mlflex.evaluation;

import mlflex.core.*;
import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.helper.FileUtilities;
import mlflex.helper.MathUtilities;
import mlflex.helper.ResultsFileUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class saves a summary of feature selection that was performed in the experiment. Specifically, it indicates which features were ranked highest on average across the cross-validation folds.
 */
public class FeatureSelectionResultsSaver
{
    private AbstractDataProcessor _processor;
    private FeatureSelectionAlgorithm _algorithm;

    /** Constructor
     *
     * @param processor Data processor
     * @param algorithm Feature selection algorithm
     */
    public FeatureSelectionResultsSaver(AbstractDataProcessor processor, FeatureSelectionAlgorithm algorithm)
    {
        _processor = processor;
        _algorithm = algorithm;
    }

    /** This method contains the logic to parse the feature-selection results and save output summaries.
     *
     * @throws Exception
     */
    public void SaveMeanFeatureRanksFile() throws Exception
    {
        ArrayList<ArrayList<String>> rankedLists = new ArrayList<ArrayList<String>>();

        for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(_processor))
        {
            Singletons.Log.Debug("Getting ranked features for outer fold " + outerFold);
            ArrayList<String> rankedFeatures = new FeatureSelectionEvaluator(_processor, _algorithm, outerFold).GetOuterSelectedFeatures(-1);
            Singletons.Log.Debug("Done getting ranked features for outer fold " + outerFold);

            if (rankedFeatures.size() == 0)
                throw new Exception("An error occurred when trying to save mean feature ranks file for outer fold " + outerFold + ", feature selection algorithm " + _algorithm.Key + " and " + _processor.GetDescription() + ". No features were selected.");

            Singletons.Log.Debug("Combining ranked features for outer fold " + outerFold);
            rankedLists.add(rankedFeatures);
        }

        String headerComment = " Order in which each feature was ranked by the feature-selection algorithm. When cross-validation was used, the values represent average ranks across the folds. Note that these values are calculated on the assumption that all features received a rank, whereas some feature-selection algorithms do not assign ranks to all.";

        HashMap<String, ArrayList<Double>> rankMap = new HashMap<String, ArrayList<Double>>();

        for (ArrayList<String> rankedFeatures : rankedLists)
        {
            for (int i=0; i<rankedFeatures.size(); i++)
            {
                String feature = rankedFeatures.get(i);
                double rank = (double)(i + 1);

                ArrayList<Double> allRanks = new ArrayList<Double>();
                if (rankMap.containsKey(feature))
                    allRanks = rankMap.get(feature);

                allRanks.add(rank);
                rankMap.put(feature, allRanks);
            }
        }

        Singletons.Log.Debug("Calculating mean ranks");
        ArrayList<FeatureRank> meanRanks = new ArrayList<FeatureRank>();
        for (String feature : rankMap.keySet())
            meanRanks.add(new FeatureRank(feature, MathUtilities.Mean(rankMap.get(feature))));
        Collections.sort(meanRanks);

        Singletons.Log.Debug("Creating mean ranks name-value pairs");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new NameValuePair("Feature", "Rank"));
        for (FeatureRank meanRank : meanRanks)
            nameValuePairs.add(new NameValuePair(meanRank.Feature, String.valueOf(meanRank.Rank)));

        Singletons.Log.Debug("Save the output to a file");
        FileUtilities.CreateFileDirectoryIfNotExists(GetOutFilePath(true));
        ResultsFileUtilities.AppendMatrixColumn(nameValuePairs, GetOutFilePath(true), headerComment);
    }

//    public void SaveMultipleIterationSummary() throws Exception
//    {
//        String thisIterationFilePath = GetOutFilePath(true);
//
//        ArrayList<String> individualFilePaths = new ArrayList<String>();
//        for (int iteration : Singletons.Config.GetIterations())
//            individualFilePaths.add(thisIterationFilePath.replace("Iteration" + Singletons.Iteration, "Iteration" + iteration));
//
//        ResultsFileUtilities.CombineMatrixFiles(individualFilePaths, thisIterationFilePath.replace("Iteration" + Singletons.Iteration + "/", ""));
//    }

    /** This method indicates where the results file will be stored.
     *
     * @param appendIterationIfMoreThanOne Whether to append the iteration name if there is more than one
     * @return Path to the results file
     * @throws Exception
     */
    public String GetOutFilePath(boolean appendIterationIfMoreThanOne) throws Exception
    {
        return Settings.GetOutputResultsDir(_processor.GetDescription() + "/" + _algorithm.Key + "/", appendIterationIfMoreThanOne) + "Feature_Ranks.txt";
    }
}
