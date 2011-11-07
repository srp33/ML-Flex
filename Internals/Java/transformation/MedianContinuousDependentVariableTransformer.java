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

package mlflex.transformation;

import mlflex.core.DataInstanceCollection;
import mlflex.core.Singletons;
import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;

import java.util.ArrayList;

/** When the dependent variable contains continuous values, this class transforms those values to discrete values, depending on whether each value is higher or lower than the median value.
 * @author Stephen Piccolo
 */
public class MedianContinuousDependentVariableTransformer extends ContinuousDependentValueBinaryTransformer
{
    public MedianContinuousDependentVariableTransformer() throws Exception
    {
        super(GetMedianValue(), "HIGH", "LOW");
    }

    @Override
    public String TransformDependentVariableValue(String rawValue) throws Exception
    {
        double survival = Double.parseDouble(rawValue);
        return (survival > GetMedianValue()) ? "HIGH" : "LOW";
    }

    private static double GetMedianValue() throws Exception
    {
        DataInstanceCollection dependentVariableInstances = Singletons.InstanceVault.RawDependentVariableInstances;

        ArrayList<Double> values = ListUtilities.CreateDoubleList(dependentVariableInstances.GetDataPointValues(Singletons.ProcessorVault.DependentVariableDataProcessor.DataPointName).GetAllValues());

        return MathUtilities.Median(values);
    }
}
