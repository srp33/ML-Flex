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
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.util.ArrayList;

/** This data processor is designed to parse information from the TCGA clinical data; specifically, it handles "age," "gender" and "KPS" data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaClinicalDataProcessor extends AbstractTcgaDataProcessor
{
    private TcgaClinicalTabDataParser _parser;

    /** Constructor */
    public TcgaClinicalDataProcessor() throws Exception
    {
        _parser = new TcgaClinicalTabDataParser(this, GetInputDataDir());
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
        transformed.AddDataPoint("age at diagnosis", instance.GetDataPointValue("AGEATINITIALPATHOLOGICDIAGNOSIS"));
        transformed.AddDataPoint("gender", instance.GetDataPointValue("GENDER"));
        transformed.AddDataPoint("KPS", instance.GetDataPointValue("KARNOFSKYPERFORMANCESCORE"));

        return transformed;
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("age at diagnosis", "KPS"));
    }
    // KPS: Chandler1993,Gundersen1996,Lacroix2001,Lamborn2004,Krex2007,Houillier2006,Ruano2009,Weller2009,Colman
}