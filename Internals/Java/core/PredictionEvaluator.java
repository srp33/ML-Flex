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
import mlflex.parallelization.LockedCallable;
import mlflex.parallelization.MultiThreadedTaskHandler;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/** This class contains logic for evaluating classification models. It orchestrates the process of performing classification for a particular data processor, feature-selection algorithm, classification algorithm, number of features, and cross-validation fold. It also contains logic for distributing tasks across multiple computing nodes and threads.
 * @author Stephen Piccolo
 */
public class PredictionEvaluator
{
    /** The data processor that will be used for making the predictions. */
    public final AbstractDataProcessor Processor;
    /** The feature-selection algorithm that will be used on the data. */
    public final FeatureSelectionAlgorithm FeatureSelectionAlgorithm;
    /** The classification algorithm that will be used on the data. */
    public final ClassificationAlgorithm ClassificationAlgorithm;
    /** The number of top-ranked features that will be used for classification. */
    public final int NumFeatures;
    /** The outer cross-validation fold number. */
    public final int OuterFold;

    private final DataInstanceCollection _dependentVariableInstances;

    /** Constructor
     *
     * @param processor Data processor that handles data that will be used for constructing classification models
     * @param selectionAlgorithm Feature-selection algorithm that will be applied prior to classification
     * @param classificationAlgorithm Classification algorithm that will be used
     * @param numFeatures The number of features that will be used for the classification models
     * @param outerFold Outer cross-validation fold number
     * @param dependentVariableInstances Dependent-variable data instances
     * @throws Exception
     */
    public PredictionEvaluator(AbstractDataProcessor processor, FeatureSelectionAlgorithm selectionAlgorithm, ClassificationAlgorithm classificationAlgorithm, int numFeatures, int outerFold, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        Processor = processor;
        FeatureSelectionAlgorithm = selectionAlgorithm;
        ClassificationAlgorithm = classificationAlgorithm;
        NumFeatures = numFeatures;
        OuterFold = outerFold;
        _dependentVariableInstances = dependentVariableInstances;
    }

    private String GetSaveDirectory()
    {
        return Settings.PREDICTIONS_DIR + Processor + "/" + FeatureSelectionAlgorithm + "/" + ClassificationAlgorithm + "/" + NumFeatures + "Features/OuterFold" + OuterFold + "/";
    }

    private String GetOuterSaveFilePath()
    {
        return GetSaveDirectory() + GetOuterPredictionsFileName();
    }

    private String GetInnerSaveFilePath(int innerFold)
    {
        return GetSaveDirectory() + GetModelPredictionsFileName(innerFold);
    }

    public String GetAlgorithmOutputFilePath()
    {
        return GetSaveDirectory() + "Algorithm_Output.txt";
    }

    private String GetStatusFilePrefix()
    {
        return GetSaveDirectory().replace(Settings.PREDICTIONS_DIR, "Predictions/");
    }

    private String GetOuterStatusFilePath()
    {
        return GetStatusFilePrefix() + GetOuterPredictionsFileName();
    }

    private String GetInnerStatusFilePath(int innerFold)
    {
        return GetStatusFilePrefix() + GetModelPredictionsFileName(innerFold);
    }

    /** Indicates the file name where the outer-fold prediction information should be stored
     *
     * @return File name where prediction information is stored
     */
    public String GetOuterPredictionsFileName()
    {
        return "OuterFold_Predictions.txt";
    }

    /** Indicates the file name where the inner-fold prediction information should be stored
     *
     * @param innerFold Number of inner cross-validation fold
     * @return File name where prediction information is stored
     */
    public String GetModelPredictionsFileName(int innerFold)
    {
        return "InnerFold" + innerFold + "_Predictions.txt";
    }

    /** This method contains logic for performing classification across all cross-validation folds.
     *
     * @param evaluateInner Whether to make predictions for inner cross-validation folds
     * @param includeOuter Whether to make predictions for the outer cross-validation fold
     * @return Callable tasks that need to be executed
     * @throws Exception
     */
    public ArrayList<LockedCallable> GetPredictionTasks(boolean evaluateInner, boolean includeOuter) throws Exception
    {
        FileUtilities.CreateDirectoryNoFatalError(GetSaveDirectory());

        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        if (evaluateInner)
        {
            for (final int innerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
            {
                // This creates a new task that can be executed in parallel
                callables.add(new LockedCallable(GetInnerStatusFilePath(innerFold), "Make predictions for " + GetInnerDescription(innerFold), new Callable<Object>()
                {
                    public Object call() throws Exception
                    {
                        // This is the actual code that will be executed for the task
                        ArrayList<String> features = GetInnerFeatures(innerFold);
                        DataInstanceCollection trainData = Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTrainInstances(Processor, innerFold, features);
                        DataInstanceCollection testData = Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTestInstances(Processor, innerFold, features);

                        return MakeAndSavePredictions(features, trainData, testData, GetInnerSaveFilePath(innerFold), null, GetInnerDescription(innerFold));
                    }
                }));
            }
        }

        // When applicable, add a task for the outer cross-validation fold
        if (includeOuter)
        {
            callables.add(new LockedCallable(GetOuterStatusFilePath(), "Make predictions for " + GetOuterDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ArrayList<String> features = GetOuterFeatures();
                    DataInstanceCollection trainData = Singletons.InstanceVault.GetCrossValidationAssignments().GetTrainInstances(Processor, OuterFold, features);
                    DataInstanceCollection testData = Singletons.InstanceVault.GetCrossValidationAssignments().GetTestInstances(Processor, OuterFold, features);

                    return MakeAndSavePredictions(features, trainData, testData, GetOuterSaveFilePath(), GetAlgorithmOutputFilePath(), GetOuterDescription());
                }
            }));
        }

        return callables;
    }

    private Boolean MakeAndSavePredictions(ArrayList<String> features, DataInstanceCollection trainData, DataInstanceCollection testData, String saveFilePath, String modelFilePath, String description) throws Exception
    {
        // See if we need to make predictions
        if (!NeedToMakePredictions(features, trainData, testData, description))
            return Boolean.TRUE;

        // Make the predictions
        ModelPredictions modelPredictions = ClassificationAlgorithm.TrainTest(trainData, testData, _dependentVariableInstances.Clone());

        // Make sure the predictions are valid
        if (!PredictionsAreValid(testData, modelPredictions, description))
            return Boolean.FALSE;

        // Save the predictions to a file
        modelPredictions.Predictions.SaveToFile(saveFilePath);

        // Save the model/description about the predictions to a file
        if (modelFilePath != null && modelPredictions.Model.length() > 0)
            FileUtilities.WriteTextToFile(modelFilePath, modelPredictions.Model);

        // Indicate whether everything worked properly
        return modelPredictions.Predictions.equals(Predictions.ReadFromFile(saveFilePath));
    }

    private boolean NeedToMakePredictions(ArrayList<String> features, DataInstanceCollection trainData, DataInstanceCollection testData, String description)
    {
        if (features.size() == 0)
        {
            Singletons.Log.Debug("No predictions were saved for " + description + " because no features were selected.");
            return false;
        }

        if (trainData.Size() == 0)
        {
            Singletons.Log.Debug("No predictions were saved for " + description + " because there were no training data instances.");
            return false;
        }

        if (testData.Size() == 0)
        {
            Singletons.Log.Debug("No predictions were saved for " + description + " because there were no test data instances.");
            return false;
        }

        return true;
    }

    private boolean PredictionsAreValid(DataInstanceCollection testData, ModelPredictions modelPredictions, String description) throws Exception
    {
        // Double check that no predictions were made for any training instances
        for (String instanceID : modelPredictions.Predictions.GetInstanceIDs())
            if (!testData.Contains(instanceID))
            {
                String errorMessage = "In " + description + ", a prediction was made for instance " + instanceID + " even though it wasn't a test instance. ";
                errorMessage += "\nTest IDs:\n";
                errorMessage += ListUtilities.Join(testData.GetIDs(), ", ") + "\n";
                errorMessage += "\nPrediction IDs:\n";
                errorMessage += ListUtilities.Join(modelPredictions.Predictions.GetInstanceIDs(), ", ") + "\n";
                Singletons.Log.Exception(errorMessage);

                return false;
            }

        // Make sure a prediction was made for every test instance
        for (String instanceID : testData.GetIDs())
            if (!modelPredictions.Predictions.HasPrediction(instanceID))
            {
                Singletons.Log.Exception("In " + description + ", no prediction was made for instance " + instanceID + ".");
                return false;
            }

        // Make sure we made the correct number of predictions
        if (modelPredictions.Predictions.Size() != testData.Size())
        {
            Singletons.Log.Exception("The number of predictions made for " + description + "(" + modelPredictions.Predictions.Size() + ") was not equal to the number of test instances (" + testData.Size() + ".");
            return false;
        }

        return true;
    }

    private ArrayList<String> GetOuterFeatures() throws Exception
    {
        return new FeatureSelectionEvaluator(Processor, FeatureSelectionAlgorithm, OuterFold).GetOuterSelectedFeatures(NumFeatures);
    }

    private ArrayList<String> GetInnerFeatures(int innerFold) throws Exception
    {
        return new FeatureSelectionEvaluator(Processor, FeatureSelectionAlgorithm, OuterFold).GetInnerSelectedFeatures(innerFold, NumFeatures);
    }

    private Predictions ReadInnerPredictions(int innerFold) throws Exception
    {
        return Predictions.ReadFromFile(GetInnerSaveFilePath(innerFold));
    }

    private Predictions ReadOuterPredictions() throws Exception
    {
        return Predictions.ReadFromFile(GetOuterSaveFilePath());
    }

    private Predictions _innerPredictions = null;
    /** This method retrieves predictions that have been made for patients in the inner cross-validation folds.
     *
     * @return Predictions for data instances in inner cross-validation folds
     * @throws Exception
     */
    public Predictions GetInnerPredictions() throws Exception
    {
        if (_innerPredictions == null)
        {
            MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler("get inner predictions for " + GetOuterDescription());

            for (final int innerFold : Singletons.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
            {
                taskHandler.Add(new Callable<Object>()
                {
                    public Predictions call() throws Exception
                    {
                        if (GetInnerFeatures(innerFold).size() == 0)
                            return new Predictions();

                        return ReadInnerPredictions(innerFold);
                    }
                });
            }

            ArrayList<Prediction> innerPredictions = new ArrayList<Prediction>();
            for (Object x : taskHandler.Execute())
                for (Prediction prediction : ((Predictions)x).GetAll())
                    innerPredictions.add((prediction));

            _innerPredictions = new Predictions(innerPredictions);
        }

        return _innerPredictions;
    }

    private Predictions _outerPredictions = null;
    /** This method retrieves predictions that have been made for patients in the outer cross-validation folds.
     *
     * @return Predictions for data instances in outer cross-validation folds
     * @throws Exception
     */
    public Predictions GetOuterPredictions() throws Exception
    {
        if (_outerPredictions == null)
        {
            if (GetOuterFeatures().size() == 0)
                return new Predictions();

            _outerPredictions = ReadOuterPredictions();
        }

        return _outerPredictions;
    }

    /** Provides a description of this object, specific to processing of inner cross-validation folds.
     *
     * @param innerFold Number of inner cross-validation fold
     * @return Description
     */
    public String GetInnerDescription(int innerFold)
    {
        return GetOuterDescription() + "_InnerFold" + innerFold;
    }

    /** Provides a description of this object, specific to processing of outer cross-validation fold.
     * @return Description
     */
    public String GetOuterDescription()
    {
        return Processor + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm + "_" + NumFeatures + "Features_OuterFold" + OuterFold;
    }

    @Override
    public boolean equals(Object obj)
    {
        PredictionEvaluator compareObj = (PredictionEvaluator)obj;

        return this.Processor.equals(compareObj.Processor) &&
               this.FeatureSelectionAlgorithm.equals(compareObj.FeatureSelectionAlgorithm) &&
               this.ClassificationAlgorithm.equals(compareObj.ClassificationAlgorithm) &&
               this.NumFeatures == compareObj.NumFeatures &&
               this.OuterFold == compareObj.OuterFold;
    }

    @Override
    public String toString()
    {
        return Processor + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm + "_" + NumFeatures + "_" + "OuterFold" + OuterFold;
    }
}
