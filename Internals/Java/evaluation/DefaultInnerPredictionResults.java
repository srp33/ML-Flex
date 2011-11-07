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

import mlflex.core.Prediction;
import mlflex.core.Predictions;
import mlflex.core.DataValues;
import mlflex.core.Singletons;

import java.util.ArrayList;

/** This class extends PredictionResults and is used in situations where you need to specify default values for the performance metrics.
 *
 */
public class DefaultInnerPredictionResults extends PredictionResults
{
    /** Constructor
     *
     * @param outerFold Number of outer cross-validation fold
     * @throws Exception
     */
    public DefaultInnerPredictionResults(int outerFold) throws Exception
    {
        super(null);
        Predictions = GetDefaultPredictions(outerFold);
    }

    private Predictions GetDefaultPredictions(int outerFold) throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (DataValues instance : Singletons.InstanceVault.TransformedDependentVariableInstances.Get(Singletons.InstanceVault.GetCrossValidationAssignments().GetTestIDs(outerFold)))
        {
            String dependentVariableValue = Singletons.InstanceVault.GetTransformedDependentVariableValue(instance.GetID());
            // The default is that the actual class is the same as the predicted class
            predictions.add(new Prediction(instance.GetID(), dependentVariableValue, dependentVariableValue));
        }
        
        return new Predictions(predictions);
    }
}
