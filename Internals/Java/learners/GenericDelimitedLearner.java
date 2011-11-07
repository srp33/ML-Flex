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

package mlflex.learners;

import mlflex.core.DataInstanceCollection;
import mlflex.core.Settings;
import mlflex.helper.AnalysisFileCreator;
import mlflex.helper.MiscUtilities;

/** This class is designed to support interfacing between ML-Flex and any third-party machine-learning software that can handle input files in a tab-delimited format. The user specifies all command-line arguments in the Config/* files. It is also important that the third-party software output the results in a specific (simple) format and in a certain order. Within the ML-Flex distribution is a tutorial that explains more about how to do this.
 */
public class GenericDelimitedLearner extends GenericArffLearner
{
    @Override
    protected String CreateInputFile(DataInstanceCollection instances) throws Exception
    {
        return new AnalysisFileCreator(Settings.TEMP_DATA_DIR, MiscUtilities.GetUniqueID(), instances, null, true).CreateTabDelimitedFile().GetTabDelimitedFilePath();
    }
}
