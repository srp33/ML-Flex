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

// This line is required by Java to specify which package contains the code
package mlflex.dataprocessors;

// These import statements allow us to use other Java classes besides this one
import mlflex.core.DataInstanceCollection;
import mlflex.core.DataValues;
import mlflex.helper.MathUtilities;
import java.util.Random;

/** This class represents a trivial example of how to create a data processor. */
public class ExampleDataProcessor extends AbstractDataProcessor
{
    /** This method is used to parse/generate raw data that will be input into ML-Flex. In this example, random values for three data points and ten data instances are generated. The random values are continuous values. */
    @Override
    protected void ParseInputData() throws Exception
    {
        // Specify the names of the data points that will be used
        String[] dataPoints = new String[] {"DataPoint1", "DataPoint2", "DataPoint3"};

        // Specify the IDs of the data instances that will be used
        String[] instanceIDs = new String[] {"Instance1", "Instance2", "Instance3", "Instance4", "Instance5", "Instance6", "Instance7", "Instance8", "Instance9", "Instance10"};

        // Loop through the data points
        for (String dataPoint : dataPoints)
            // Loop through the instances
            for (String instanceID : instanceIDs)
                // Save a raw data point using the combination of data point and instance ID
                SaveRawDataPoint(dataPoint, instanceID, String.valueOf(new Random().nextDouble()));
    }

    /** After the raw data are processed and stored, various transformations can be applied to the data before it is used for machine-learning analyses. Implementing this method is one way to perform such transformations. In this example, the values are transformed to the log-2 scale. */
    @Override
    protected DataInstanceCollection TransformInstances(DataInstanceCollection rawInstances) throws Exception
    {
        // Loop through the data instances
        for (DataValues instance : rawInstances)
            // Loop through the data points for each instance
            for (String dataPoint : instance.GetDataPointNames())
            {
                // Retrieve the raw value
                String rawValue = instance.GetDataPointValue(dataPoint);

                // Convert the raw value to a numeric value
                double numericValue = Double.valueOf(rawValue);

                // Perform a log-2 transformation
                double transformedValue = MathUtilities.Log2(numericValue);

                // Update the values in the collection
                instance.UpdateDataPoint(dataPoint, String.valueOf(transformedValue));
            }

        // Return the collection of data in transformed form
        return rawInstances;
    }
}
