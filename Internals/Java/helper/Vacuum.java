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

package mlflex.helper;

import mlflex.core.Settings;
import mlflex.core.Singletons;
import mlflex.parallelization.LockedCallable;
import mlflex.parallelization.MultiThreadedTaskHandler;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/** This class contains logic to delete any files that need to be deleted before or after an experiment. When executed with multiple threads, this is much faster than doing it at the command line. */
public class Vacuum
{
    /** This method cleans up files that can be deleted.
     *
     * @throws Exception
     */
    public void Clean() throws Exception
    {
        // Create a list of tasks that represent deleting files within a specific directory.
        ArrayList<LockedCallable> callables = new ArrayList<LockedCallable>();

        callables.add(new LockedCallable("Clean up", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                // Add a task for each temp directory
                for (final String directoryPath : ListUtilities.CreateStringList(Settings.TEMP_DATA_DIR, Settings.TEMP_RESULTS_DIR))
                {
                    FileUtilities.DeleteAllFilesAndDirectoriesRecursively(directoryPath);
                    FileUtilities.DeleteDirectory(directoryPath);
                }

                return Boolean.TRUE;
            }
        }));

        MultiThreadedTaskHandler.ExecuteLockTasks("Post experiment cleanup", callables);

        try
        {
            // Can't do this in a LockedCallable object
            MiscUtilities.DeleteCoreDirectory(Settings.LOCKS_DIR);
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug(ex);
        }
    }
}
