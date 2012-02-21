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

package mlflex.ensemblelearners;

import mlflex.core.*;
import mlflex.dataprocessors.AggregateDataProcessor;
import mlflex.evaluation.ClassificationResultsFilePaths;
import mlflex.evaluation.DefaultInnerPredictionResults;
import mlflex.evaluation.PredictionResults;
import mlflex.helper.FileUtilities;
import mlflex.helper.MiscUtilities;

import java.util.ArrayList;
import java.util.HashMap;

/** ML-Flex supports ensemble-learning approaches for aggregating evidence from multiple classification predictions. This class is a generic handler of all ensemblelearners approaches in ML-Flex.
 * @author Stephen Piccolo
 */
public abstract class AbstractEnsembleLearner
{
    /** This method specifies the directory where the ensemblelearners predictions will be saved.
     *
     * @param outerFold Number of outer cross-validation foldd
     * @return Absolute directory path
     */
    public String GetSaveDirectory(int outerFold)
    {
        return Settings.ENSEMBLE_PREDICTIONS_DIR + "OuterFold" + outerFold + "/";
    }

    /** This method specifies the file path where the ensemblelearners predictions will be saved.
     *
     * @param outerFold Number of outer cross-validation foldd
     * @return Absolute file path
     */
    private String GetSaveFilePath(int outerFold)
    {
        return GetSaveDirectory(outerFold) + GetDescription() + "_Predictions.txt";
    }

    /** This method is the workhorse of this class. Having received information about individual predictions that were made for each data instance, it combines the individual predictions into a combined prediction, using the custom logic of the overriding class.
     *
     * @param outerFold Number of outer cross-validation fold
     * @param ensemblePredictionInfoMap Map of ensemblelearners prediction info (one for each data instance)
     * @return Indicates whether this method was successful
     * @throws Exception
     */
    public Boolean MakeEnsemblePredictions(int outerFold, HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        Predictions ensemblePredictions = MakeInstancePredictions(ensemblePredictionInfoMap);
        ensemblePredictions.SaveToFile(GetSaveFilePath(outerFold));

        return ensemblePredictions.equals(GetEnsemblePredictions(outerFold));
    }

    /** This method reads from file the ensemblelearners predictions that were previously made across all outer cross-validation folds.
     * @return Ensemble predictions that were made previously and stored on disk.
     * @throws Exception
     */
    public Predictions GetEnsemblePredictions() throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            if (FileUtilities.FileExists(GetSaveFilePath(outerFold)))
                predictions.addAll(Predictions.ReadFromFile(GetSaveFilePath(outerFold)).GetAll());

        return new Predictions(predictions);
    }

    /** This method reads from file the ensemblelearners predictions that were previously made.
     * @param outerFold Number of outer cross-validation fold
     * @return Combined predictions that were made previously and stored on disk.
     * @throws Exception
     */
    public Predictions GetEnsemblePredictions(int outerFold) throws Exception
    {
        return Predictions.ReadFromFile(GetSaveFilePath(outerFold));
    }

    /** This method makes ensemblelearners predictions for a particulate data instance. This method is what needs to be overridden by most classes that inherit from this class.
     *
     * @param ensemblePredictionInfoMap Prediction info for the data instance
     * @return Combined prediction
     * @throws Exception
     */
    protected Predictions MakeInstancePredictions(HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (String instanceID : ensemblePredictionInfoMap.keySet())
            predictions.add(MakeInstancePrediction(instanceID, ensemblePredictionInfoMap.get(instanceID)).Prediction);

        return new Predictions(predictions);
    }

    /** This method makes an ensemblelearners prediction for a particulate data instance. This method is what needs to be overridden by most classes that inherit from this class.
     *
     * @param instanceID Data instance ID
     * @param predictionInfos Prediction info for the data instance
     * @return Combined prediction
     * @throws Exception
     */
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos predictionInfos) throws Exception
    {
        throw new Exception("Not implemented");
    }

    public ClassificationResultsFilePaths GetResultsFilePaths(boolean appendIteration) throws Exception
    {
        return new ClassificationResultsFilePaths(Settings.GetOutputResultsDir("Ensemble/" + GetDescription() + "/", appendIteration));
    }

    /** This method provides a simple description of the ensemblelearners approach. By default this is the name of the class.
     *
     * @return Simple description of ensemblelearners approach
     */
    public String GetDescription()
    {
        return getClass().getSimpleName();
    }

    /** Creates a formatted description of this object.
     *
     * @return A formatted description of this object
     */
    public String GetFormattedDescription()
    {
        return MiscUtilities.SeparateWords(GetDescription());
    }

    /** This method indicates which ensemblelearners predictors should be used (if any).
     *
     * @return A list of ensemblelearners predictor objects
     * @throws Exception
     */
    public static ArrayList<AbstractEnsembleLearner> GetAllEnsembleLearners() throws Exception
    {
        ArrayList<AbstractEnsembleLearner> ensemblePredictors = new ArrayList<AbstractEnsembleLearner>();

        if (!Settings.NeedToEnsembleLearn())
            return ensemblePredictors;

        ensemblePredictors.add(new MajorityVoteEnsembleLearner());
        ensemblePredictors.add(new WeightedVoteEnsembleLearner());
        ensemblePredictors.add(new SelectBestEnsembleLearner());
        ensemblePredictors.add(new MaxProbabilityEnsembleLearner());
        ensemblePredictors.add(new MeanProbabilityEnsembleLearner());
        ensemblePredictors.add(new WeightedMeanProbabilityEnsembleLearner());
        ensemblePredictors.add(new StackedEnsembleLearner(true));
        //ensemblePredictors.add(new StackedEnsembleLearner(false));

        return ensemblePredictors;
    }

    /** This method retrieves information from predictions that were made previously, so that prediction information can be used for ensemblelearners learning.
     *
     * @param outerFold Number of outer cross-validation fold
     * @param modelSelectors List of model selectors, which are used for retrieving the predictions
     * @return A map containing predictions for each data instance
     * @throws Exception
     */
    public static HashMap<String, EnsemblePredictionInfos> GetInstanceEnsemblePredictionInfos(int outerFold, ArrayList<ModelSelector> modelSelectors, boolean evaluateInner) throws Exception
    {
        HashMap<String, EnsemblePredictionInfos> instanceEnsemblePredictionInfoMap = new HashMap<String, EnsemblePredictionInfos>();

        for (final ModelSelector modelSelector : modelSelectors)
        {
            if (modelSelector.Processor instanceof AggregateDataProcessor)
                continue;

            // These are the default values
            PredictionResults innerPredictionResults = new DefaultInnerPredictionResults(outerFold);
            Predictions outerPredictions = modelSelector.GetOuterPredictions(Singletons.Config.GetNumFeaturesOptions(modelSelector.Processor, Singletons.Config.GetFeatureSelectionAlgorithms(modelSelector.Processor).get(0)).get(0), outerFold);

            // If we need to consider inner folds, retrieve inner-fold predictions
            if (evaluateInner)
            {
                int bestNumFeatures = modelSelector.GetBestNumFeaturesAcrossInnerFolds(outerFold);
                Predictions innerPredictions = modelSelector.GetInnerPredictions(bestNumFeatures, outerFold);
                outerPredictions = modelSelector.GetOuterPredictions(bestNumFeatures, outerFold);

                if (innerPredictions.Size() == 0 || outerPredictions.Size() == 0)
                    continue;

                innerPredictionResults = new PredictionResults(innerPredictions);
            }

            for (String instanceID : outerPredictions.GetInstanceIDs())
            {
                EnsemblePredictionInfo instanceInfo = new EnsemblePredictionInfo(outerPredictions.Get(instanceID), innerPredictionResults, modelSelector.GetDescription());

                if (instanceEnsemblePredictionInfoMap.containsKey(instanceID))
                    instanceEnsemblePredictionInfoMap.put(instanceID, instanceEnsemblePredictionInfoMap.get(instanceID).Add(instanceInfo));
                else
                    instanceEnsemblePredictionInfoMap.put(instanceID, new EnsemblePredictionInfos().Add(instanceInfo));

                //The code below will give you ensemblelearners results for all number of features
                //for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(modelSelector.Processor))
                //    predictionInfos.Add(new EnsemblePredictionInfo(modelSelector.GetOuterPrediction(numFeatures, instanceID), modelSelector.GetInnerPredictions(numFeatures, instanceID), modelSelector.GetSimpleDescription()));
            }
        }

        return instanceEnsemblePredictionInfoMap;
    }
}
