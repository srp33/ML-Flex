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

import mlflex.core.Singletons;
import mlflex.dataprocessors.AbstractMetadataProcessor;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/** This data processor is designed to parse metadata information to support parsing of DNA methylation data in TCGA; specifically, it supports a conversion of probe identifiers to gene symbols. The .adf.txt files can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code can be tweaked to work with different Illumina methylation arrays if needed.
 * @author Stephen Piccolo
 */
public class TcgaMethylationMetadataProcessor extends AbstractMetadataProcessor
{
    @Override
    protected HashMap<String, String> GetSourceDataMap() throws Exception
    {
        HashMap<String, String> dataMap = new HashMap<String, String>();

        for (File file : FileUtilities.GetFilesInDirectory("InputData/Metadata/*OMA*.adf.txt"))
        {
            Singletons.Log.Info("file: " + file.getAbsolutePath());

            for (ArrayList<String> row : FileUtilities.ParseDelimitedFile(file.getAbsolutePath(), "\t", "#", 1))
                dataMap.put(row.get(0), row.get(2));
        }

        return dataMap;
    }

    @Override
    protected boolean Keep(String key, String value) throws Exception
    {
        return !ListUtilities.CreateStringList("", ".").contains(value);// && GetGoGenes().contains(value);
    }
}