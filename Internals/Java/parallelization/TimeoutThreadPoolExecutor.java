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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/** This class is used to monitor threads that are executed in parallel. If a thread executes longer than a configurable timeout period, execution is terminated. */
public class TimeoutThreadPoolExecutor extends ThreadPoolExecutor
{
    private final HashMap<String, Timer> _timerMap = new HashMap<String, Timer>();

    /** Constructor
     *
     * @param numberThreads The maximum number of threads that will be executed
     */
    public TimeoutThreadPoolExecutor(int numberThreads)
    {
        super(numberThreads, numberThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    protected void beforeExecute(final Thread thread, final Runnable runnable)
    {
        super.beforeExecute(thread, runnable);

        // Start the timing and set the timeout period
        Timer timer = new Timer(true);
        timer.schedule(new TimeOutTask(thread), Settings.THREAD_TIMEOUT_MINUTES * 60000L);

        // Store the timer in a global variable so it can be accessed later
        _timerMap.put(runnable.toString(), timer);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable)
    {
        try
        {
            // Cancel the timer if it is still there
            if (_timerMap.containsKey(runnable.toString()))
                _timerMap.get(runnable.toString()).cancel();
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("Error occurred when attempting to cancel timed-out thread.");
            Singletons.Log.Debug(ex);
        }
        finally
        {
            // Clean up
            if (_timerMap.containsKey(runnable.toString()))
                _timerMap.remove(runnable.toString());

            super.afterExecute(runnable, throwable);
        }
    }

    /** A simple task that enables tasks to be interrupted. */
    private class TimeOutTask extends TimerTask
    {
        Thread _thread;

        TimeOutTask(Thread thread)
        {
            _thread = thread;
        }

        public void run()
        {
            if(_thread!= null && _thread.isAlive())
                _thread.interrupt();
        }
    }
}
