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
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/** This class contains methods for logging information about ML-Flex execution. Values are output to the screen and to output files.
 * @author Stephen Piccolo
 */
public class Log
{
    private String _machineAddress;

    /** Indicates how many exceptions have been caught so far as the experiment has been executing. */
    public int NumExceptionsCaught;

    // This uses an external library for logging
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Log.class);

    /** Constructor
     *
     * @throws Exception
     */
    public Log() throws Exception
    {
        NumExceptionsCaught = 0;
        _machineAddress = MiscUtilities.GetMachineAddress();
    }

    /** Saves debug information
     *
     * @param text Debug text
     */
    public void Debug(Object text)
    {
        if (Settings.DEBUG)
            Print(FormatText(text));
    }

    /** Saves debug information
     *
     * @param list List of items to be logged
     */
    public void Debug(ArrayList list)
    {
        if (!Settings.DEBUG)
            return;

        if (list == null)
            Debug("<null ArrayList>");
        else
            Debug("\n" + ListUtilities.Join(ListUtilities.CreateStringList(list), "\n"));
    }

    /** Saves debugging information for a non-fatal error.
     *
     * @param ex Exception that occurred.
     */
    public void Debug(Throwable ex)
    {
        if (!Settings.DEBUG)
            return;

        if (ex == null)
        {
            Info("<null Exception>");
            return;
        }

        Debug("A non-fatal error occurred. It will be logged but may not affect processing of this program.");
        Debug(GetStackTrace(ex));
    }

    /** Saves logging information
     *
     * @param text Logging text
     */
    public void Info(Object text)
    {
        Print(FormatText(text));
    }

    /** Saves logging information
     *
     * @param list List of items to be logged
     */
    public void Info(ArrayList list)
    {
        if (list == null)
        {
            Info("<null ArrayList>");
            return;
        }

        Info("\n" + ListUtilities.Join(ListUtilities.CreateStringList(list), "\n"));
    }

    /** Saves logging information
     *
     * @param map Map of items to be logged
     */
    public void Info(HashMap map)
    {
        if (map == null)
        {
            Info("<null HashMap>");
            return;
        }

        for (Object key : map.keySet())
        {
            Object value = map.get(key);
            String output = key.toString() + "=" + value.toString();
            Info(output);
        }
    }

    /** Saves logging information for a non-fatal error.
     *
     * @param ex Exception that occurred.
     */
    public void Info(Throwable ex)
    {
        if (ex == null)
        {
            Info("<null Exception>");
            return;
        }

        Info("A non-fatal error occurred. It will be logged but may not affect processing of this program.");
        Info(GetStackTrace(ex));
    }

    /** Saves exception information
     *
     * @param message Exception message
     */
    public void Exception(String message)
    {
        Exception(new Exception(message));
    }

    /** Saves exception information
     *
     * @param ex Exception object
     */
    public void Exception(Throwable ex)
    {
        Info(GetStackTrace(ex));

        NumExceptionsCaught++;

        if (NumExceptionsCaught >= 25)
        {
            Info("More than " + NumExceptionsCaught + " non-fatal exceptions have occurred, so aborting!");
            System.exit(0);
        }
     }

    /** Saves exception information when the exception is severe enough that execution of the program should be halted.
     *
     * @param message Exception message
     */
    public void ExceptionFatal(Object message)
    {
        ExceptionFatal(new Exception(String.valueOf(message)));
    }

    /** Saves exception information when the exception is severe enough that execution of the program should be halted.
     *
     * @param ex Exception object
     */
    public void ExceptionFatal(Throwable ex)
    {
        Exception(ex);
        System.exit(0);
     }

    /** Obtains stack-trace information when an exception has occurred.
     *
     * @param throwable Exception object
     * @return Stack-trace information
     */
    public String GetStackTrace(Throwable throwable)
    {
        if (throwable == null)
            return "<null exception>";

        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    private String FormatText(Object text)
    {
        String outText = _machineAddress + " | ";

        String experiment = "";

        if (Singletons.Experiment != null)
        {
            experiment = Singletons.Experiment.Name;
            if (Singletons.Iteration > 1)
                experiment += " (Iteration " + Singletons.Iteration + ")";
            experiment += " | ";
        }

        return outText + experiment + (text == null ? "<null>" : String.valueOf(text));
    }

    private static void Print(Object x)
    {
        try
        {
            String out = x == null ? "<null>" : String.valueOf(x);

            if (out.equals(""))
                return;

            logger.info(out);
        }
        catch (Exception ex)
        {
            System.out.println("Could not log.");
            ex.printStackTrace();
        }
    }

//    public static String GetStackTrace()
//    {
//        StringBuilder trace = new StringBuilder();
//
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for (int i = 1; i < elements.length; i++)
//        {
//            StackTraceElement s = elements[i];
//            trace.append("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
//        }
//
//        return trace.toString();
//    }
}
