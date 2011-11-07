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

package mlflex.dataprocessors.tcga;

import mlflex.core.DataValueMeta;
import mlflex.core.DataValues;
import mlflex.dataprocessors.AbstractDataProcessor;

/** This data processor is designed to parse information from the TCGA clinical data; specifically, it handles survival data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaSurvivalDataProcessor extends AbstractDataProcessor
{
    private TcgaClinicalTabDataParser _parser;

    /** Constructor */
    public TcgaSurvivalDataProcessor() throws Exception
    {
        _parser = new TcgaClinicalTabDataParser(this, "InputData/TCGA/");
    }

    @Override
    protected void ParseInputData() throws Exception
    {
        _parser.SaveRawData();
    }

    @Override
    public DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return _parser.FindDataValueMeta(dataPointName);
    }

    @Override
    protected boolean KeepRawInstance(DataValues instance) throws Exception
    {
        return _parser.KeepPatient(instance);
    }

    @Override
    protected DataValues TransformRawInstance(DataValues instance) throws Exception
    {
        DataValues transformed = instance.CopyStructure();

        transformed.AddDataPoint("Class", instance.GetDataPointValue(TcgaClinicalTabDataParser.SURVIVAL_DATA_POINT_NAME));

        return transformed;
    }

    @Override
    public boolean KeepTransformedInstance(DataValues instance) throws Exception
    {
        return new TcgaTreatmentsDataProcessor().KeepInstance(instance.GetID());
    }
}