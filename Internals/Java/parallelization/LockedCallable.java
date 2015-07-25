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

import java.util.ArrayList;
import java.util.concurrent.Callable;

/** This class encapsulates objects necessary to run tasks in parallel across multiple compute nodes. It contains logic for saving/deleting lock files and for handling errors that may occur. It also contains logic to check whether a status file has been created, which would indicate that this task has already been completed.
 */
public class LockedCallable implements Callable<Object>
{
    public static final String DONE_STATUS = "DONE";

    /** This is the path to the status file that indicates whether this task has been completed previously. */
    public String StatusFilePath;
    /** This is the path to the lock file. */
    private String _lockFilePath;
    /** This is what will be output to log files indicating status. */
    private String _logDescription;
    /** This is the callable that will actually be executed after locking has occurred. */
    private Callable<Object> _callable;

    /** Constructor that accepts the objects that are necessary to support the function of this class. Thisi particular constructor is intended to be used when the task being executed is a simple one that doesn't need a complex status file or lock file. The same value is used for the status file, lock file, and lock description.
     * @param simpleDescription Simple description of the task that will be executed
     * @param callable Object that will be executed when locking is successful
     */
    public LockedCallable(String simpleDescription, Callable<Object> callable)
    {
        this(simpleDescription, new java.io.File(simpleDescription).getName(), callable);
    }

    /** Constructor that accepts the objects that are necessary to support the function of this class.
     * @param statusRelativeFilePath Relative path to the status file that indicates whether this task has been completed previously
     * @param logDescription Name fo the task being executed that will be output to the log files
     * @param callable Object that will be executed when locking is successful
     */
    public LockedCallable(String statusRelativeFilePath, String logDescription, Callable<Object> callable)
    {
        StatusFilePath = Settings.STATUS_DIR + statusRelativeFilePath.replace(" ", "_");
        _lockFilePath = Settings.LOCKS_DIR + statusRelativeFilePath.replace(" ", "_");
        _logDescription = logDescription;
        _callable = callable;
    }

    /** This is a convenience method that accepts an array of LockedCallable objects and converts it into a list.
     *
     * @param callables Array of callable arguments
     * @return A List of callable objects
     */
    public static ArrayList<LockedCallable> CreateLockedCallableList(LockedCallable... callables)
    {
        ArrayList<LockedCallable> callableList = new ArrayList<LockedCallable>();
        for (LockedCallable callable : callables)
            callableList.add(callable);

        return callableList;
    }

    /** This method attempts to create a lock file that will indicate to other threads or compute nodes that the _callable task is being executed. If the file cannot be created (most likely because the task is already being executed by another thread/node, then nothing will happen.
     *
     * @return Result of callable
     * @throws Exception
     */
    public Object call() throws Exception
    {
        // If this task has already been completed, then indicate success
        if (FileUtilities.FileExists(StatusFilePath))
        {
            Singletons.Log.Debug("Status file exists at " + StatusFilePath);

            // See if the task is indicated as already being completed
            if (ReadStatus().equals(DONE_STATUS))
            {
                Singletons.Log.Debug("Status is " + DONE_STATUS + " for " + StatusFilePath);
                return Boolean.TRUE;
            }
            else
            {
                Singletons.Log.Debug("Status is PENDING for " + StatusFilePath);

                // See if the status file is stale
                if (FileUtilities.GetFileAgeMinutes(StatusFilePath) > (double)Settings.THREAD_TIMEOUT_MINUTES)
                {
                    Singletons.Log.Debug("PENDING status file is stale at " + StatusFilePath + ", so attempting to delete it");

                    // Delete the status file if it is stale
                    if (FileUtilities.DeleteFile(StatusFilePath))
                        Singletons.Log.Debug("Stale PENDING status file at " + StatusFilePath + " was deleted");
                    else
                        Singletons.Log.Debug("Stale PENDING status file at " + StatusFilePath + " could not be deleted");

                    return Boolean.FALSE;
                }

                return Boolean.FALSE;
            }
        }

        Singletons.Log.Debug("No status file exists at " + StatusFilePath);

        if (!ShouldCreateLockFile()) // Guard clause, indicating whether lock file should be created
            return Boolean.FALSE;

        // Attempting to create a lock file
        if (FileUtilities.CreateEmptyFile(_lockFilePath))
        {
            Singletons.Log.Debug("Lock file was created at " + _lockFilePath);

            try
            {
                Singletons.Log.Debug("Attempting to create PENDING status file at " + StatusFilePath);
                if (FileUtilities.CreateEmptyFile(StatusFilePath))
                {
                    Singletons.Log.Debug("Created PENDING status file at " + StatusFilePath);
                }
                else
                {
                    Singletons.Log.Debug("PENDING status could not be set because file already exists at " + StatusFilePath);
                    DeleteActiveLockFile();
                    return Boolean.FALSE;
                }
            }
            catch (Exception ex)
            {
                Singletons.Log.Debug("Exception occurred when attempting to set PENDING status on file at " + StatusFilePath);
                Singletons.Log.Debug(ex);
                DeleteActiveLockFile();
                return Boolean.FALSE;
            }

            try
            {
               Singletons.Log.Info("Attempt: " + _logDescription);

                // Try to invoke the command
                if (_callable.call().equals(Boolean.TRUE))
                {
                    // Try to create a status file indicating the command was successful
                    Singletons.Log.Debug("Attempting to set " + DONE_STATUS + " status on " + StatusFilePath);
                    FileUtilities.AppendTextToFile(StatusFilePath, DONE_STATUS);
                    Singletons.Log.Debug(DONE_STATUS + " status set on " + StatusFilePath);
                    Singletons.Log.Info("Success: " + _logDescription);
                    // Remove the lock file because it is no longer necessary
                    DeleteActiveLockFile();
                    return Boolean.TRUE;
                }
                else
                {
                    // The task was not processed successfully for whatever reason, so need to retry
                    Singletons.Log.Debug("Retry required: " + _logDescription);
                    FileUtilities.DeleteFile(StatusFilePath);
                    DeleteActiveLockFile();
                    DeleteActiveStatusFile();
                    return Boolean.FALSE;
                }
            }
            catch (Exception ex)
            {
                // Remove the lock file and shutdown hook because they are no longer necessary
                Singletons.Log.Debug("Exception occurred: " + _logDescription);
                Singletons.Log.Debug(ex);
                DeleteActiveLockFile();
                DeleteActiveStatusFile();
                throw ex;
            }
        }

        return Boolean.FALSE;
    }

    /** Checks for a status file and reads the status from a file to see what the status is.
     *
     * @return Status value
     * @throws Exception
     */
    public String ReadStatus() throws Exception
    {
        String status = "";

        try
        {
            status = FileUtilities.ReadScalarFromFile(StatusFilePath);
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("Error reading status file at " + StatusFilePath);
            Singletons.Log.Debug(ex);
        }

        return status;
    }

    /** Indicates whether this task is already completed
     *
     * @return Whether this task is already completed
     * @throws Exception
     */
    public boolean IsDone() throws Exception
    {
        return FileUtilities.FileExists(StatusFilePath) && ReadStatus().equals(LockedCallable.DONE_STATUS);
    }

    /** This method contains the logic to indicate whether a lock file can be created for this task. If so, then it should be processed. This logic is based on whether other threads are processing it and whether existing lock files are stale, etc.
     *
     * @return Whether the task should be executed
     * @throws Exception
     */
    private Boolean ShouldCreateLockFile() throws Exception
    {
        if (!FileUtilities.FileExists(_lockFilePath))
        {
            Singletons.Log.Debug("No lock file was found at " + _lockFilePath);
            return Boolean.TRUE;
        }

        if (FileUtilities.GetFileAgeMinutes(_lockFilePath) <= (double) Settings.THREAD_TIMEOUT_MINUTES) // The lock file is not stale, so another thread is probably actively processing it
        {
            Singletons.Log.Debug("Lock file was found at " + _lockFilePath + " and is not stale.");
            return Boolean.FALSE;
        }

        if (FileUtilities.DeleteFile(_lockFilePath))
        {
            Singletons.Log.Debug("Stale lock file was found at " + _lockFilePath + " and was deleted.");
            return Boolean.TRUE; // The stale lock file was successfully deleted
        }

        Singletons.Log.Debug("A stale lock file at " + _lockFilePath + " could not be deleted.");
        return Boolean.FALSE; // The lock file is stale but can't be deleted for whatever reason, so delay execution of task
    }

    /** Deletes a lock file that was created by this instance. */
    private void DeleteActiveLockFile()
    {
        Singletons.Log.Debug("Deleting active lock file at " + _lockFilePath);
        FileUtilities.DeleteFile(_lockFilePath);
        Singletons.Log.Debug("Deleted active lock file at " + _lockFilePath);
    }

    /** Deletes a status file that was created by this instance. */
    private void DeleteActiveStatusFile()
    {
        Singletons.Log.Debug("Deleting status file at " + StatusFilePath);
        FileUtilities.DeleteFile(StatusFilePath);
        Singletons.Log.Debug("Deleted status file at " + StatusFilePath);
    }
}
