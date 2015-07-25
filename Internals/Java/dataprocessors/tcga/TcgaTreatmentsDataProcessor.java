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

package mlflex.dataprocessors.tcga;

import mlflex.core.*;
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.util.ArrayList;

/** This data processor is designed to parse information from the TCGA clinical data; specifically, it handles treatment data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaTreatmentsDataProcessor extends AbstractTcgaDataProcessor
{
    private TcgaClinicalTabDataParser _parser;

    /** Constructor */
    public TcgaTreatmentsDataProcessor() throws Exception
    {
        ArrayList<String> drugList = ListUtilities.CreateStringList("TEMOZOLOMIDE", "DEXAMETHASONE", "LOMUSTINE", "BEVACIZUMAB");
        _parser = new TcgaClinicalTabDataParser(this, GetInputDataDir(), drugList);
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

        String drugName = instance.GetDataPointValue("DRUGNAME");
        if (drugName != null)
        {
            ArrayList<String> drugs = ListUtilities.CreateStringList(drugName.split(","));
            transformed.AddBinaryDataPoint("temozolomide treatment", drugs, "TEMOZOLOMIDE");
            transformed.AddBinaryDataPoint("dexamethasone treatment", drugs, "DEXAMETHASONE");
            transformed.AddBinaryDataPoint("lomustine treatment", drugs, "LOMUSTINE");
            transformed.AddBinaryDataPoint("bevacizumab treatment", drugs, "BEVACIZUMAB");
            transformed.AddBinaryDataPoint("other drug treatment", drugs, "OTHER");
        }

        transformed.AddDataPoint("radiation treatment", instance.GetDataPointValue("RADIATIONTHERAPY"));

        return transformed;
    }

    @Override
    public DataInstanceCollection GetTransformedInstances() throws Exception
    {
        DataInstanceCollection instances = GetInstances();

        for (String x : GetPatientFilterCriteria())
            instances.RemoveDataPointName(MiscUtilities.FormatName(x));

        return instances;
    }

    @Override
    public ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return MiscUtilities.FormatNames(ListUtilities.CreateStringList("temozolomide treatment", "radiation treatment"));
        // Temozolomide: Stupp,Weller
        // Radiation: DAVIS1949,Rich2005,Ruano2009
        // Tumor resection: Lacroix2001,Houillier2006,Ruano2009,Gundersen1996,Lamborn2004,Batchelor
    }

    private DataInstanceCollection _instances = null;
    private DataInstanceCollection GetInstances() throws Exception
    {
        if (_instances == null)
            _instances = GetInstancesFromFile();
        return _instances;
    }

    /** Indicates whether to keep a given data instance or to ignore it.
     *
     * @param instanceID Data instance ID
     * @return Whether to keep the data instance or ignore it
     * @throws Exception
     */
    public boolean KeepInstance(String instanceID) throws Exception
    {
        if (GetPatientFilterCriteria().size() == 0)
            return true;

        DataValues patient = GetInstances().Get(instanceID);

        for (String x : GetPatientFilterCriteria())
        {
            x = MiscUtilities.FormatName(x);

            if (patient.GetDataPointValue(x).equals("0") || patient.GetDataPointValue(x).equals(Settings.MISSING_VALUE_STRING))
                return false;
        }

        return true;
    }

    private static ArrayList<String> GetPatientFilterCriteria() throws Exception
    {
        return Singletons.Config.GetStringListConfigValue("PATIENT_FILTER_CRITERIA", "");
    }
}