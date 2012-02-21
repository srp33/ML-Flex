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

import mlflex.core.Predictions;
import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;
import mlflex.learners.WekaLearner;

import java.util.ArrayList;

/** This class provides support for evaluating a set of predictions that have been made and for summarizing the performance of the predictions using various metrics.
 * @author Stephen Piccolo
 */
public class PredictionResults
{
    public Predictions Predictions = new Predictions();

    /** Constructor
     *
     * @param predictions Predictions for which result metrics will be generated
     */
    public PredictionResults(Predictions predictions)
    {
        Predictions = predictions;
    }

    /** The accuracy represents the proportion of predictions that were correctly made (the actual class was the same as the predicted class).
     *
     * @return Accuracy value
     * @throws Exception
     */
    public double GetAccuracy() throws Exception
    {
        return MathUtilities.SmartDivide((double) GetTotalNumberCorrect(), (double) Predictions.Size());
    }

    /** The error rate represents the proportion of predictions that were incorrectly made (the actual class not the same as the predicted class).
     *
     * @return Error rate
     * @throws Exception
     */
    public double GetErrorRate() throws Exception
    {
        return 1 - GetAccuracy();
    }

    /** The baseline accuracy is the accuracy one would expect if the majority class were always selected by default.
     *
     * @return Baseline accuracy
     * @throws Exception
     */
    public double GetBaselineAccuracy() throws Exception
    {
        return MathUtilities.SmartDivide((double) ListUtilities.GetNumMatches(GetActuals(), ListUtilities.GetMostFrequentValue(GetActuals())), (double) Predictions.Size());
    }

    /** The baseline error rate is the error rate one would expect if the majority class were always selected by default.
     *
     * @return Baseline error rate
     * @throws Exception
     */
    public double GetBaselineErrorRate() throws Exception
    {
        return 1 - GetBaselineAccuracy();
    }

    /** The baseline improvement represents the difference between the accuracy attained and the baseline accuracy. Positive values mean the predictions performed better than you would expect by chance.
     *
     * @return Improvement in accuracy over the baseline expectation
     * @throws Exception
     */
    public double GetBaselineImprovement() throws Exception
    {
        return GetAccuracy() - GetBaselineAccuracy();
    }

    /** Indicates the number of actual instances that had a given dependent-variable value.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value
     * @throws Exception
     */
    public double GetNumActualsWithDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return ListUtilities.GetNumMatches(GetActuals(), dependentVariableClass);
    }

    /** Indicates the number of actual instances that had a given dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value that were predicted correctly
     */
    public double GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(String dependentVariableClass)
    {
        double numCorrect = 0.0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).DependentVariableValue.equals(dependentVariableClass) && Predictions.Get(instanceID).WasCorrect())
                numCorrect++;

        return numCorrect;
    }

    /** Indicates the number of actual instances that had a given dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value that were predicted incorrectly
     */
    public double GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(String dependentVariableClass) throws Exception
    {
        double num = 0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).DependentVariableValue.equals(dependentVariableClass) && !Predictions.Get(instanceID).WasCorrect())
                num++;

        return num;
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value
     * @throws Exception
     */
    public double GetProportionActualsWithDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClass(dependentVariableClass) / (double)Predictions.Size();
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value that were predicted correctly
     */
    public double GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass) / GetNumActualsWithDependentVariableClass(dependentVariableClass);
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value that were predicted incorrectly
     */
    public double GetProportionActualsWithDependentVariableClassThatWerePredictedIncorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass) / GetNumActualsWithDependentVariableClass(dependentVariableClass);
    }

    /** Indicates how many predictions were for a particular dependent-variable value.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumPredictionMatches(dependentVariableClass);
    }

    /** Indicates how many predictions were for a particular dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value that were predicted correctly
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClassCorrectly(String dependentVariableClass)
    {
        double numCorrect = 0.0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).Prediction.equals(dependentVariableClass) && Predictions.Get(instanceID).WasCorrect())
                numCorrect++;

        return numCorrect;
    }

    /** Indicates how many predictions were for a particular dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value that were predicted incorrectly
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClassIncorrectly(String dependentVariableClass) throws Exception
    {
        double num = 0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).Prediction.equals(dependentVariableClass) && !Predictions.Get(instanceID).WasCorrect())
                num++;

        return num;
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClass(dependentVariableClass) / (double)Predictions.Size();
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value that were predicted correctly
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClassCorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClassCorrectly(dependentVariableClass) / GetNumPredictedAsDependentVariableClass(dependentVariableClass);
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value that were predicted incorrectly
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClassIncorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClassIncorrectly(dependentVariableClass) / GetNumPredictedAsDependentVariableClass(dependentVariableClass);
    }

//    /** Calculates the Youden index for the predictions. The Youden index is another metric that can be used to assess the performance of data sets with imbalanced class distributions. Youden index=1−(false positive rate+false negative rate) See "Ratio adjustment and calibration scheme for gene-wise normalization to enhance microarray inter-study prediction" by Cheng (Bioinformatics, 2009) "Index for rating diagnostic tests", Cancer, 3, 32–35. See also YOUDEN, W. J. (1950). Index for rating diagnostic tests. Cancer, 3(1), 32-5. http://www.ncbi.nlm.nih.gov/pubmed/15405679.
//     *
//     * @return Youden index value
//     * @throws Exception
//     */
//    public double GetYoudenIndex() throws Exception
//    {
//        ArrayList<String> dependentVariableValues = Singletons.InstanceVault.TransformedDependentVariableOptions;
//
//        Double result = Double.NaN;
//
//        if (dependentVariableValues.size() == 2)
//            result = GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableValues.get(0)) + GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableValues.get(1)) - 1;
//
//        return result;
//    }

    /** Returns a list of actual classes associated with the predictions.
     *
     * @return Actual classes associated with the predictions
     * @throws Exception
     */
    public ArrayList<String> GetActuals() throws Exception
    {
        ArrayList<String> actuals = new ArrayList<String>();
        for (String instanceID : Predictions.GetInstanceIDs())
            actuals.add(Predictions.Get(instanceID).DependentVariableValue);

        return actuals;
    }

    private double GetNumPredictionMatches(String classValue) throws Exception
    {
        double num = 0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).Prediction.equals(classValue))
                num++;

        return num;
    }

    /** This method indicates how many instances with a particular class were predicted as another class.
     *
     * @param actualClass The actual class
     * @param predictedClass The predicted class
     * @return How many of the actual class were predicted as the predicted class
     */
    public int GetNumActualsPredictedAs(String actualClass, String predictedClass)
    {
        int num = 0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).DependentVariableValue.equals(actualClass) && Predictions.Get(instanceID).Prediction.equals(predictedClass))
                num++;

        return num;
    }

    /** This method indicates the total number of correct predictions.
     *
     * @return Total number of correct predictions
     */
    public int GetTotalNumberCorrect()
    {
        int numCorrect = 0;

        for (String instanceID : Predictions.GetInstanceIDs())
            if (Predictions.Get(instanceID).Prediction.equals(Predictions.Get(instanceID).DependentVariableValue))
                numCorrect++;

        return numCorrect;
    }

    /** This method indicates the total number of incorrect predictions.
     *
     * @return Total number of incorrect predictions
     */
    public int GetTotalNumberIncorrect()
    {
        return Predictions.Size() - GetTotalNumberCorrect();
    }

    private CustomWekaEvaluation _wekaEvaluation = null;

    /** This method returns an object that can be used to calculate performance metrics. It uses Weka for the calculations.
     *
     * @return A Weka evaluation object
     * @throws Exception
     */
    public CustomWekaEvaluation GetWekaEvaluation() throws Exception
    {
        if (_wekaEvaluation == null)
            _wekaEvaluation = new WekaLearner().GetWekaEvaluation(Predictions);

        return _wekaEvaluation;
    }
}
