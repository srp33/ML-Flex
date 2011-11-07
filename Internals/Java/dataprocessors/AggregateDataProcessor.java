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
import mlflex.core.Singletons;

/** This class is a data processor that combines data values across multiple other data processors: all other data processors defined as independent variable data processors. Individual data values are prefixed with the description of the source data processor.
 * @author Stephen Piccolo
 */
public class AggregateDataProcessor extends AbstractDataProcessor
{
    @Override
    protected double GetProportionMissingPerInstanceOK()
    {
        return 1.0;
    }

    @Override
    protected double GetProportionMissingPerDataPointOK()
    {
        return 1.0;
    }

    @Override
    protected void ParseInputData() throws Exception
    {
    }

    @Override
    public DataInstanceCollection GetTransformedInstances() throws Exception
    {
        DataInstanceCollection instances = new DataInstanceCollection();

        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            if (!(processor instanceof AggregateDataProcessor))
            {
                DataInstanceCollection processorInstances = Singletons.InstanceVault.GetInstancesForAnalysis(processor);

                if (processorInstances != null)
                {
                    processorInstances.PrefixDataPointNames(processor.GetDescription());
                    instances.Add(processorInstances);
                }
            }

        return instances;
    }
}