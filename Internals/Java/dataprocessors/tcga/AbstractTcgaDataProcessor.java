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

import mlflex.dataprocessors.AbstractDataProcessor;

/** This abstract class is designed to provide support for all data processors that use TCGA data. All such data processor classes should inherit from this class.
 * @author Stephen Piccolo
 */
public class AbstractTcgaDataProcessor extends AbstractDataProcessor
{
    public String GetInputDataDir()
    {
        return "InputData/TCGA/";
    }

    /** This method formats a column name from the tab-delimited files that TCGA uses.
     *
     * @param columnName Name of column
     * @return Formatted column name
     */
    protected String FormatColumnName(String columnName)
    {
        return columnName;
    }
}
