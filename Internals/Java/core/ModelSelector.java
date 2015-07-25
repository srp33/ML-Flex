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
import mlflex.evaluation.ClassificationResultsFilePaths;
import mlflex.evaluation.PredictionResults;
import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/** This class is used to help determine which "model" is best for a given data processor, feature-selection algorithm, and classification algorithm. It uses the internal cross-validation folds to determine the "best" model.
 * @author Stephen Piccolo
 */
public class ModelSelector
{
    /** The data processor containing data that will be used for the predictions. */
    public AbstractDataProcessor Processor;
    /** The feature-selection algorithm that will be used. */
    public FeatureSelectionAlgorithm FeatureSelectionAlgorithm;
    /** The classification algorithm that will be used. */
    public ClassificationAlgorithm ClassificationAlgorithm;
    /** A list of prediction evaluator objects that are relevant to this combination. */
    public ArrayList<PredictionEvaluator> PredictionEvaluators;

    /** Constructor
     *
     * @param processor Data processor to be tested
     * @param featureSelectionAlgorithm Feature-selection algorithm that will be used to determine which features to use for classification
     * @param classificationAlgorithm Classification algorithm to be applied
     * @param predictionEvaluators List of model evaluators
     * @throws Exception
     */
    public ModelSelector(AbstractDataProcessor processor, FeatureSelectionAlgorithm featureSelectionAlgorithm, ClassificationAlgorithm classificationAlgorithm, ArrayList<PredictionEvaluator> predictionEvaluators) throws Exception
    {
        Processor = processor;
        FeatureSelectionAlgorithm = featureSelectionAlgorithm;
        ClassificationAlgorithm = classificationAlgorithm;
        PredictionEvaluators = predictionEvaluators;
    }

    private ArrayList<PredictionEvaluator> GetModelEvaluators(int numFeatures) throws Exception
    {
        ArrayList<PredictionEvaluator> matches = new ArrayList<PredictionEvaluator>();

        for (PredictionEvaluator evaluator : PredictionEvaluators)
            if (evaluator.NumFeatures == numFeatures)
                matches.add(evaluator);

        return matches;
    }

    private PredictionEvaluator GetModelEvaluator(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = new PredictionEvaluator(Processor, FeatureSelectionAlgorithm, ClassificationAlgorithm, numFeatures, outerFold, Singletons.InstanceVault.TransformedDependentVariableInstances);
        int index = PredictionEvaluators.indexOf(evaluator);

        if (index == -1)
            return null;

        return PredictionEvaluators.get(index);
    }

    /** This method retrieves predictions that have been made previously for data instances within a given inner cross-validation fold and for a given number of features.
     *
     * @param numFeatures Number of features
     * @param outerFold Outer cross-validation fold number
     * @return Predictions that were made previously
     * @throws Exception
     */
    public Predictions GetInnerPredictions(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = GetModelEvaluator(numFeatures, outerFold);

        if (evaluator == null)
            return new Predictions();

        return evaluator.GetInnerPredictions();
    }

    /** This method retrieves the predictions that were made for all outer cross-validation instances
     *
     * @param numFeatures Number of features that were used for the predictions
     * @return Outer cross-validation fold predictions
     * @throws Exception
     */
    public Predictions GetOuterPredictionsAllFolds(int numFeatures) throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (PredictionEvaluator evaluator : GetModelEvaluators(numFeatures))
            predictions.addAll(evaluator.GetOuterPredictions().GetAll());

        return new Predictions(predictions);
    }

    /** Identifies the predictions that were made for the inner cross-validation folds and that performed the best across all options for number of features.
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestInnerPredictions(int outerFold) throws Exception
    {
        return GetInnerPredictions(GetBestNumFeaturesAcrossInnerFolds(outerFold), outerFold);
    }

    /** Identifies the predictions that were made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestOuterPredictions(int outerFold) throws Exception
    {
        return GetOuterPredictions(GetBestNumFeaturesAcrossInnerFolds(outerFold), outerFold);
    }

    /** This method retrieves predictions that have been made previously for data instances in a given outer cross-validation fold and for a given number of features.
     *
     * @param numFeatures Number of features
     * @param outerFold Outer cross-validation fold number
     * @return Predictions that were made previously
     * @throws Exception
     */
    public Predictions GetOuterPredictions(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = GetModelEvaluator(numFeatures, outerFold);

        if (evaluator == null)
            return new Predictions();

        return evaluator.GetOuterPredictions();
    }

    /** Identifies the predictions that were made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestOuterPredictionsAllFolds() throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            predictions.addAll(GetBestOuterPredictions(outerFold).GetAll());

        return new Predictions(predictions);
    }

    /** Identifies the prediction that was made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @param instanceID Data instance ID
     * @return Prediction
     * @throws Exception
     */
    public Prediction GetBestOuterPrediction(String instanceID) throws Exception
    {
        int outerFold = Singletons.InstanceVault.GetCrossValidationAssignments().GetFoldNumber(instanceID);

        Predictions outerPredictions = GetBestOuterPredictions(outerFold);

        if (outerPredictions.HasPrediction(instanceID))
            return outerPredictions.Get(instanceID);

        return null;
    }

    private double GetMeanOuterAuc(int numFeatures) throws Exception
    {
        ArrayList<Double> aucs = new ArrayList<Double>();

        for (PredictionEvaluator evaluator : GetModelEvaluators(numFeatures))
            aucs.add(new PredictionResults(evaluator.GetOuterPredictions()).GetWekaEvaluation().weightedAreaUnderROC());

        return MathUtilities.Mean(aucs);
    }

    private int _bestNumFeaturesAllFolds = 0;

    /** Identifies the option for the number of features that performed best across all outer cross-validation folds. The "best" performance is determined according to performance within outer cross-validation folds.
     *
     * @return Best number of features
     * @throws Exception
     */
    public int GetBestNumFeaturesAcrossOuterFolds() throws Exception
    {
        ArrayList<Integer> numFeaturesOptions = Singletons.Config.GetNumFeaturesOptions(Processor, FeatureSelectionAlgorithm);

        if (numFeaturesOptions.size() == 1)
            return numFeaturesOptions.get(0);

        if (_bestNumFeaturesAllFolds == 0)
        {
            double bestResult = Double.MIN_VALUE;

            for (int numFeatures : numFeaturesOptions)
            {
                double auc = GetMeanOuterAuc(numFeatures);

                if (auc > bestResult)
                {
                    bestResult = auc;
                    _bestNumFeaturesAllFolds = numFeatures;
                }
            }
        }

        return _bestNumFeaturesAllFolds;
    }

    private ConcurrentHashMap<Integer, Integer> _bestNumFeaturesMap = new ConcurrentHashMap<Integer, Integer>();

    /** Identifies the option for the number of fatures that performed best for a given outer cross-validation fold. The "best" performance is determined according to performance within inner cross-validation folds.
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Best number of features
     * @throws Exception
     */
    public int GetBestNumFeaturesAcrossInnerFolds(int outerFold) throws Exception
    {
        ArrayList<Integer> numFeaturesOptions = Singletons.Config.GetNumFeaturesOptions(Processor, FeatureSelectionAlgorithm);

        if (numFeaturesOptions.size() == 1)
            return numFeaturesOptions.get(0);

        if (!_bestNumFeaturesMap.containsKey(outerFold))
        {
            Singletons.Log.Debug("Getting best num features for outer fold " + outerFold + " and " + GetDescription());
            int bestNumFeatures = 0;
            double bestResult = Double.MIN_VALUE;

            for (int numFeatures : numFeaturesOptions)
            {
                PredictionEvaluator modelEvaluator = GetModelEvaluator(numFeatures, outerFold);

                if (modelEvaluator == null)
                    continue;

                Predictions innerPredictions = modelEvaluator.GetInnerPredictions();

                double auc = new PredictionResults(innerPredictions).GetWekaEvaluation().weightedAreaUnderROC();

                if (auc > bestResult)
                {
                    bestResult = auc;
                    bestNumFeatures = numFeatures;
                }
            }

            _bestNumFeaturesMap.put(outerFold, bestNumFeatures);
        }

        return _bestNumFeaturesMap.get(outerFold);
    }

    /** This method returns an object that specifies where the results for this object should be stored.
     *
     * @param appendIteration Whether to append the iteration number to each path
     * @param isForTrainingData Whether these paths are specific to training data
     * @return A object containing paths that will be used
     * @throws Exception
     */
    public ClassificationResultsFilePaths GetResultsFilePaths(boolean appendIteration, boolean isForTrainingData) throws Exception
    {
        String suffix = isForTrainingData ? "Training_Data/" : "";

        return new ClassificationResultsFilePaths(Settings.GetOutputResultsDir(GetDescription("/") + "/", appendIteration) + suffix);
    }

    /** Provides a detailed description of this object.
     *
     * @return Description
     */
    public String GetDescription() throws Exception
    {
        return GetDescription("_");
    }

    /** Provides a description of this object that can be presented to the user in a specific format.
     *
     * @param delimiter A delimiter between each part of the description
     * @return A formatted description of this object
     * @throws Exception
     */
    public String GetDescription(String delimiter) throws Exception
    {
        ArrayList<String> descriptionItems = ListUtilities.CreateStringList(Processor.GetDescription(), ClassificationAlgorithm);

        if (FeatureSelectionEvaluator.NeedToSelectFeatures(Processor, FeatureSelectionAlgorithm))
            descriptionItems.add(1, FeatureSelectionAlgorithm.Key);

        return ListUtilities.Join(descriptionItems, delimiter);
    }

    @Override
    public boolean equals(Object obj)
    {
        ModelSelector compareObj = (ModelSelector)obj;

        return this.Processor.equals(compareObj.Processor) &&
               this.FeatureSelectionAlgorithm.equals(compareObj.FeatureSelectionAlgorithm) &&
               this.ClassificationAlgorithm.equals(compareObj.ClassificationAlgorithm);
    }

    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        try
        {
            return GetDescription();
        }
        catch (Exception ex)
        {
            Singletons.Log.ExceptionFatal(ex);
            return null; // This statement will never be reached but is necessary for compile
        }
    }

    /** This method constructs a list of ModelSelector objects that are used in an experiment. It creates one for every combination of data processor, feature-selection algorithm, and classification algorithm.
     *
     * @return List of ModelSelector objects
     * @throws Exception
     */
    public static ArrayList<ModelSelector> GetAllModelSelectors() throws Exception
    {
        HashSet<ModelSelector> modelSelectors = new HashSet<ModelSelector>();

        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            if (Singletons.InstanceVault.GetCrossValidationAssignments().HasAnyTestData(processor))
                for (FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                {
                    for (ClassificationAlgorithm cAlgorithm : Singletons.Config.GetMainClassificationAlgorithms())
                    {
                        ArrayList<PredictionEvaluator> predictionEvaluators = new ArrayList<PredictionEvaluator>();

                        for (int outerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
                            for (int numFeatures : Singletons.Config.GetNumFeaturesOptions(processor, fsAlgorithm))
                                predictionEvaluators.add(new PredictionEvaluator(processor, fsAlgorithm, cAlgorithm, numFeatures, outerFold, Singletons.InstanceVault.TransformedDependentVariableInstances));

                        modelSelectors.add(new ModelSelector(processor, fsAlgorithm, cAlgorithm, predictionEvaluators));
                    }
                }

        return new ArrayList<ModelSelector>(modelSelectors);
    }
}