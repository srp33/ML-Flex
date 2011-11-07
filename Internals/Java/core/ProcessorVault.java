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
import mlflex.dataprocessors.DependentVariableDataProcessor;
import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.dataprocessors.AggregateDataProcessor;
import mlflex.helper.MiscUtilities;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** This class provides convenience methods to access instances of data processors.
 * @author Stephen Piccolo
 */
public class ProcessorVault
{
    /** This object contains an instance of any metadata processors that can be used in this experiment. */
    public ArrayList<AbstractMetadataProcessor> MetadataProcessors = new ArrayList<AbstractMetadataProcessor>();
    /** This object contains an instance of any (non-metadata) processor that can be used in this experiment. */
    public ArrayList<AbstractDataProcessor> AllDataProcessors = new ArrayList<AbstractDataProcessor>();
    /** This object contains an instance of the dependent-variable (class) processor that is used in this experiment. */
    public DependentVariableDataProcessor DependentVariableDataProcessor = null;
    /** This object contains an instance of any independent (prediction) variable processor that can be used in this experiment.*/
    public ArrayList<AbstractDataProcessor> IndependentVariableDataProcessors = new ArrayList<AbstractDataProcessor>();

    /** This method obtains configuration information for the various types of processors that will be used in a given experiment, creates instances of those processors, and caches those instances in public objects for each access (and so they only have to be instantiated one time).
     *
     * @throws Exception
     */
    public void Load() throws Exception
    {
        // Instantiate metadata processors based on config information
        for (String className : Singletons.Config.GetStringListConfigValue("META_DATA_PROCESSORS", ""))
            MetadataProcessors.add((AbstractMetadataProcessor) ((Constructor) Class.forName(className).getConstructor()).newInstance());

        ArrayList<String> dataProcessorsConfigValues = Singletons.Config.GetMandatoryStringListConfigValue("DATA_PROCESSORS");

        // The AggregateDataProcessor must be the last one (if it is specified)
        if (dataProcessorsConfigValues.contains(new AggregateDataProcessor().GetDescription()))
        {
            dataProcessorsConfigValues.remove(new AggregateDataProcessor().GetDescription());
            dataProcessorsConfigValues.add(new AggregateDataProcessor().GetDescription());
        }

        for (String classInstantiationText : dataProcessorsConfigValues)
        {
            AbstractDataProcessor processor = (AbstractDataProcessor)MiscUtilities.InstantiateClassFromText(classInstantiationText);
            IndependentVariableDataProcessors.add(processor);

            AddProcessorToAll(processor);
        }

        DependentVariableDataProcessor = new DependentVariableDataProcessor();

        AddProcessorToAll(DependentVariableDataProcessor);
    }

    private void AddProcessorToAll(AbstractDataProcessor processor)
    {
        if (AllDataProcessors.contains(processor))
            Singletons.Log.ExceptionFatal("Multiple data processors with description of " + processor.GetDescription() + " cannot be used in same experiment.");

        AllDataProcessors.add(processor);
    }
}
