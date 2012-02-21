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

package mlflex.dataprocessors;

import mlflex.core.DataInstanceCollection;
import mlflex.core.DataValues;
import mlflex.core.Singletons;
import mlflex.helper.ListUtilities;
import mlflex.helper.MathUtilities;

import java.util.*;

/** This data processor generates data randomly. It is used for negative testing (ensuring that no positive result is attained when none is expected).
 * @author Stephen Piccolo
 */
public class RandomDataProcessor extends AbstractDataProcessor
{
    private String _description;
    private int _numInstances;
    private int _numDiscreteDataPoints;
    private int _numContinuousDataPoints;
    private Random _random;

    /** This constructor allows the user to specify how many random instances should be generated and how many data points should be generated per data instance.
     * @param numInstances Number of data instances for which random data should be generated.
     * @param numDiscreteDataPoints Number of discrete data points for which data should be generated (the discrete data points are either "Value1" or "Value2")
     * @param numContinuousDataPoints Number of continuous data points for which data should be generated (the data points come from a standard normal distrubution)
     */
    public RandomDataProcessor(Integer numInstances, Integer numDiscreteDataPoints, Integer numContinuousDataPoints)
    {
        this(numInstances, numDiscreteDataPoints, numContinuousDataPoints, "RandomData_" + numInstances + "Instances_" + numDiscreteDataPoints + "DiscreteDataPoints_" + numContinuousDataPoints + "ContinuousDataPoints");
    }

    /** This constructor allows the user to specify how many random instances should be generated and how many data points should be generated per data instance.
     * @param numInstances Number of data instances for which random data should be generated.
     * @param numDiscreteDataPoints Number of discrete data points for which data should be generated (the discrete data points are either "Value1" or "Value2")
     * @param numContinuousDataPoints Number of continuous data points for which data should be generated (the data points come from a standard normal distribution)
     * @param description Description of this data set that can be used to identify it uniquely
     */
    public RandomDataProcessor(Integer numInstances, Integer numDiscreteDataPoints, Integer numContinuousDataPoints, String description)
    {
        _numInstances = numInstances;
        _numDiscreteDataPoints = numDiscreteDataPoints;
        _numContinuousDataPoints = numContinuousDataPoints;
        _description = description;
        _random = new Random(Singletons.RandomSeed + (long)(_numInstances + _numDiscreteDataPoints + _numContinuousDataPoints + _description.hashCode()));
    }

    @Override
    public String GetDescription()
    {
        return _description;
    }

    @Override
    public DataInstanceCollection GetTransformedInstances() throws Exception
    {
        ArrayList<String> discreteDataPoints = GenerateDataPointNames(_numDiscreteDataPoints, "D");
        ArrayList<String> continuousDataPoints = GenerateDataPointNames(_numContinuousDataPoints, "C");
        DataInstanceCollection instances = new DataInstanceCollection();

        for (int i=0; i<_numInstances; i++)
        {
            DataValues dv = new DataValues("ID" + i);

            for (String dataPointName : discreteDataPoints)
                dv.AddDataPoint(dataPointName, GenerateRandomDiscreteValue());

            for (String dataPointName : continuousDataPoints)
                dv.AddDataPoint(dataPointName, String.valueOf(GenerateRandomContinuousValue()));

            dv.AddDataPoint("Class", GenerateRandomDiscreteValue());

            instances.Add(dv);

            if (i > 0 && i % 100 == 0)
                Singletons.Log.Debug("Generating random data instances: " + i);
        }

        return instances;
    }

    @Override
    protected void ParseInputData() throws Exception
    {
    }

    private static ArrayList<String> GenerateDataPointNames(int number, String suffix)
    {
        ArrayList<String> alphabet = ListUtilities.ALPHABET;
        ArrayList<String> dataPoints = new ArrayList<String>();

        for (int i = 0; i < alphabet.size() && dataPoints.size() < number; i++)
            for (int j = 0; j < alphabet.size() && dataPoints.size() < number; j++)
                for (int k = 0; k < alphabet.size() && dataPoints.size() < number; k++)
                    for (int l = 0; l < alphabet.size() && dataPoints.size() < number; l++)
                        for (int m = 0; m < alphabet.size() && dataPoints.size() < number; m++)
                        {
                            String dataPoint = alphabet.get(i) + alphabet.get(j) + alphabet.get(k) + alphabet.get(l) + alphabet.get(m);
                            dataPoint += (suffix.equals("") ? "" : "_" + suffix);
                            dataPoints.add(dataPoint);
                        }

        return dataPoints;
    }

    private double GenerateRandomContinuousValue()
    {
        return MathUtilities.Round(_random.nextGaussian(), 8);
    }

    private String GenerateRandomDiscreteValue()
    {
        ArrayList<String> values = new ArrayList<String>();
        values.add("Value1");
        values.add("Value2");

        return ListUtilities.PickRandomString(values, _random);
    }
}
