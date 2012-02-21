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

package mlflex.parallelization;

import mlflex.core.Settings;
import mlflex.core.Singletons;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;
import mlflex.helper.MiscUtilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;

/** This class encapsulates logic for executing computational tasks across one or more threads.
 */
public class MultiThreadedTaskHandler
{
    private LinkedList<Callable<Object>> _callables;
    private String _description;
    private int _numThreads;

    /** This default constructor initializes the class. It uses the number of threads that has been specified at the command line.
    * @throws Exception
    */
    public MultiThreadedTaskHandler(String description)
    {
        this(description, Settings.NUM_THREADS);
    }

    /** This constructor accepts the arguments to the class that are necessary to execute tasks in parallel and initializes private variables.
     * @param description A description of the tasks that will be executed in parallel
     * @param numThreads Number of threads on which to execute the callable objects
     * @throws Exception
     */
    public MultiThreadedTaskHandler(String description, int numThreads)
    {
        _callables = new LinkedList<Callable<Object>>();
        _description = description;
        _numThreads = numThreads;
    }

    /** This method can be used to add a task that needs to be executed.
     * @param callable Callable object to be executed
     * @return The current instance of this object for convenience
     */
    public MultiThreadedTaskHandler Add(Callable<Object> callable)
    {
        _callables.add(callable);
        return this;
    }

    /** This method can be used to add a task that needs to be executed.
     * @param callables List of callable objects to be executed
     * @return The current instance of this object for convenience
     */
    public MultiThreadedTaskHandler Add(ArrayList<Callable<Object>> callables)
    {
        _callables.addAll(callables);
        return this;
    }

    /** Executes multiple callable objects in a multithreaded fashion
     *
     * @return Objects that are returned from each _callable object
     * @throws Exception
     */
    public ArrayList Execute() throws Exception
    {
        if (_callables == null || _callables.size() == 0)
            return new ArrayList();

        Singletons.Log.Debug("Attempting to share execution across " + _numThreads + " threads for " + _description + ".");

        // Initialize the service
        //ExecutorService service = Executors.newFixedThreadPool(_numThreads);
        ExecutorService service = new TimeoutThreadPoolExecutor(_numThreads);

        try
        {
            ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();

            // Submit each task to a queue to be executed
            for (Callable<Object> callable : _callables)
                futures.add(service.submit(callable));

            ArrayList results = new ArrayList();

            // Parse through the results of the execution
            for (Future<Object> future : futures)
            {
                Object result = future.get();

                if (result != null)
                    results.add(result);
            }

            return results;
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            // Very important to shut down the service
            service.shutdown();
        }
    }

    /** Executes a series of tasks that are "locked" such that only one thread should execute each. These are locked across multiple compute nodes as well.
     *
     * @param description Name of the series of tasks that will be executed
     * @param lockedCallables Tasks that will be executed
     * @throws Exception
     */
    public static void ExecuteLockTasks(String description, ArrayList<LockedCallable> lockedCallables) throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler(description);

        // Prepare the tasks to be executed
        for (LockedCallable callable : lockedCallables)
        {
            FileUtilities.CreateFileDirectoryIfNotExists(callable.StatusFilePath);
            taskHandler.Add(callable);
        }

        try
        {
            // See if any of the tasks returned a false value and if so, pause then retry
            if (ListUtilities.AnyFalse(ListUtilities.CreateBooleanList(taskHandler.Execute())))
            {
                Pause(description);
                ExecuteLockTasks(description, lockedCallables);
            }
        }
        catch (Exception ex)
        {
            // If an exception occurred, log it, pause, then try again
            Singletons.Log.Exception(ex);
            Pause(description);
            ExecuteLockTasks(description, lockedCallables);
        }
    }

    private static void Pause(String description) throws Exception
    {
        Singletons.Log.Debug("Pausing for " + Settings.PAUSE_SECONDS + " seconds: " + description + ". Other threads may be processing these tasks.");
        MiscUtilities.Sleep(Settings.PAUSE_SECONDS * 1000);
        Singletons.Log.Debug("Done with pause: " + description + ".");
    }
}
