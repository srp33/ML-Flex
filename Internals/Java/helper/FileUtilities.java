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

import mlflex.core.Singletons;

import java.io.*;
import java.util.ArrayList;

/** This class provides helper methods for reading, writing, updating, and deleting files.
 * @author Stephen Piccolo
 */
public class FileUtilities
{
    /** Indicates the age (in minutes) of a file.
     *
     * @param filePath Absolute file path
     * @return Age in minutes
     * @throws Exception
     */
    public static double GetFileAgeMinutes(String filePath) throws Exception
    {
        return DateUtilities.DifferenceInMinutes(DateUtilities.GetCurrentDate(), DateUtilities.CreateDate(new File(filePath).lastModified()));
    }

    /** Convenience method that appends text to an existing file.
     *
     * @param filePath Absolute file path
     * @param text Text to append
     * @throws Exception
     */
    public static void AppendTextToFile(String filePath, String text) throws Exception
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        out.write(text);
        out.close();
    }

    /** Appends a line to an existing file (incuding a new line character).
     *
     * @param filePath Absolute file path
     * @param text Text to append
     * @throws Exception
     */
    public static void AppendLineToFile(String filePath, String text) throws Exception
    {
        AppendTextToFile(filePath, text + "\n");
    }

    /** Writest text to a file.
     *
     * @param filePath Absolute file path
     * @param text Text to write
     * @throws Exception
     */
    public static void WriteTextToFile(String filePath, String text) throws Exception
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
        out.write(text);
        out.close();
    }

    /** Writes a new line to a file (including a new line character).
     *
     * @param filePath Absolute file path
     * @param text Text to write
     * @throws Exception
     */
    public static void WriteLineToFile(String filePath, String text) throws Exception
    {
        WriteTextToFile(filePath, text + "\n");
    }

    /** Writes new lines to a file (including new line characters).
     *
     * @param filePath Absolute file path
     * @param rows List of row lists to write
     * @throws Exception
     */
    public static void WriteLinesToFile(String filePath, ArrayList<ArrayList<String>> rows) throws Exception
    {
        WriteLinesToFile(filePath, rows, "");
    }

    /** Writes new lines to a file (including new line characters).
     *
     * @param filePath Absolute file path
     * @param rows List of row lists to write
     * @param headerComment Descriptive comment that will be placed at the top of the file
     * @throws Exception
     */
    public static void WriteLinesToFile(String filePath, ArrayList<ArrayList<String>> rows, String headerComment) throws Exception
    {
        if (rows == null)
            return;

        StringBuffer output = new StringBuffer();

        if (headerComment != null && !headerComment.equals(""))
            output.append("#" + headerComment + "\n");

        for (ArrayList<String> row : rows)
            output.append(ListUtilities.Join(row, "\t") + "\n");

        WriteTextToFile(filePath, output.toString());
    }

    /** Reads a single value from a file.
     *
     * @param filePath Absolute file path
     * @return Value
     * @throws Exception
     */
    public static String ReadScalarFromFile(String filePath) throws Exception
    {
        BigFileReader reader = new BigFileReader(filePath);
        ArrayList<String> lines = new ArrayList<String>();
        for (String line : reader)
            if (!line.startsWith("#")) // This ignores comment characters
                lines.add(line);

        if (lines.size() > 0)
            return lines.get(0);
        
        return "";
    }

    /** Reads lines from a file.
     *
     * @param filePath Absolute file path
     * @return Each line in the file
     * @throws Exception
     */
    public static ArrayList<String> ReadLinesFromFile(String filePath) throws Exception
    {
        return ReadLinesFromFile(filePath, null);
    }

    /** Reads lines from a file.
     *
     * @param filePath Absolute file path
     * @param commentChar Comment character (lines starting with this character are ignored)
     * @return Each line in the file
     * @throws Exception
     */
    public static ArrayList<String> ReadLinesFromFile(String filePath, String commentChar) throws Exception
    {
        ArrayList<String> rows = new ArrayList<String>();

        for (String line : new BigFileReader(filePath))
        {
            if (line.trim().length() == 0 || (commentChar != null && line.startsWith(commentChar)))
                continue;

            rows.add(line.trim());
        }

        return rows;
    }

    /** Reads all text from a file.
     *
     * @param file File object
     * @return String representation of text in a file
     * @throws Exception
     */
    public static String ReadTextFile(File file) throws Exception
    {
        return ReadTextFile(file.getAbsolutePath());
    }

    /** Reads all text from a file.
     *
     * @param filePath Absolute file path
     * @return String representation of text in a file
     * @throws Exception
     */
    public static String ReadTextFile(String filePath) throws Exception
    {
        StringBuilder text = new StringBuilder();

        for (String line : new BigFileReader(filePath))
            text.append(line + "\n");

        return text.toString();
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath) throws Exception
    {
        return ParseDelimitedFile(filePath, "\t");
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter) throws Exception
    {
        return ParseDelimitedFile(filePath, delimiter, "#");
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @param commentChar Comment character (lines starting with this character will be ignored)
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter, String commentChar) throws Exception
    {
        return ParseDelimitedFile(filePath, delimiter, commentChar, 0);
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @param commentChar Comment character (lines starting with this character will be ignored)
     * @param numLinesToSkip Number of lines to skip at the beginning of the file
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter, String commentChar, int numLinesToSkip) throws Exception
    {
        if (!FileExists(filePath))
            throw new Exception("No file exists at " + filePath);

        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

        int linesSkipped = 0;

        BigFileReader reader = new BigFileReader(filePath);
        for (String line : reader)
        {
            if (line == null || line.equals("") || line.startsWith(commentChar))
                continue;

            if (linesSkipped < numLinesToSkip)
            {
                linesSkipped++;
                continue;
            }

            rows.add(ListUtilities.CreateStringList(line.split(delimiter)));
        }

        return rows;
    }

    /** Copies a file from one location to another.
     *
     * @param sourceFilePath Source file path
     * @param destinationFilePath Destination file path
     * @throws Exception
     */
    public static void CopyFile(String sourceFilePath, String destinationFilePath) throws Exception
    {
        File sourceFile = new File(sourceFilePath);
        if (sourceFile.exists())
        {
            File destinationFile = new File(destinationFilePath);

            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destinationFile);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();
        }
    }

    /** Deletes a file.
     *
     * @param file File object
     */
    public static boolean DeleteFile(File file)
    {
        if (file.exists())
            try
            {
                return file.delete();
            }
            catch (Exception ex)
            {
                Singletons.Log.Debug("Could not delete " + file.getAbsolutePath() + "."); // Often this is not a problem, but we're recording it just in case.
                return false;
            }
        else
            return true; // If the file is not there, then indicate that it has been deleted (even if it was by some other process)
    }

    /** Deletes a file.
     *
     * @param filePath Absolute file path
     */
    public static boolean DeleteFile(String filePath)
    {
        return DeleteFile(new File(filePath));
    }

    /** Deletes a file.
     *
     * @param dirPath Absolute file path
     */
    public static void DeleteDirectory(String dirPath)
    {
        File dir = new File(dirPath);

        if (dir.isDirectory())
            DeleteFile(dir);
    }

    /** Deletes all files in a directory.
     *
     * @param dir Absolute directory path
     */
    public static void DeleteFilesInDirectory(String dir)
    {
        DeleteFilesInDirectory(dir, "*");
        DeleteFilesInDirectory(dir, "*.*");
    }

    /** Deletes files in a directory that match a specified file pattern.
     *
     * @param dir Absolute directory path
     * @param pattern File pattern to match
     */
    public static void DeleteFilesInDirectory(String dir, String pattern)
    {
        for (File file : GetFilesInDirectory(dir, pattern))
            if (file.exists())
                file.delete();
    }

    /** Deletes files in a directory and its subdirectories that match a specified file pattern.
     *
     * @param directoryPath Absolute directory path
     * @param pattern File pattern to match
     * @throws Exception
     */
    public static void DeleteFilesRecursively(String directoryPath, String pattern) throws Exception
    {
        CreateDirectoryIfNotExists(directoryPath);

        File[] files = new File(directoryPath).listFiles();

        if (files == null || files.length == 0)
            return;

        for (File file : files)
        {
            if (file.isFile())
            {
                if (new WildCardFileFilter(pattern).accept(file))
                {
                    Singletons.Log.Debug("Deleting file from " + file.getAbsolutePath());
                    DeleteFile(file);
                }
            }
            else
                DeleteFilesRecursively(file.getAbsolutePath(), pattern);
        }
    }

    /** Deletes any empty directories in a specified directory tree.
     *
     * @param directoryPath Absolute directory path
     * @throws Exception
     */
    public static void DeleteEmptyDirectoriesRecursively(String directoryPath) throws Exception
    {
        CreateDirectoryIfNotExists(directoryPath);

        File[] files = new File(directoryPath).listFiles();

        if (files == null || files.length == 0)
            return;

        for (File file : files)
        {
            if (file.isDirectory())
            {
                if (file.listFiles() != null && file.listFiles().length > 0)
                {
                    Singletons.Log.Debug("Going a level deeper to sub-directories of " + file.getAbsolutePath());
                    DeleteEmptyDirectoriesRecursively(file.getAbsolutePath());
                }

                Singletons.Log.Debug("Deleting directory at " + file.getAbsolutePath());
                DeleteFile(file);
            }
        }
    }

    /** Deletes all files recursively that are in a specified directory. Then it deletes the empty directories recursively. Be careful with this one!
     *
     * @param directoryPath Absolute directory path
     * @throws Exception
     */
    public static void DeleteAllFilesAndDirectoriesRecursively(String directoryPath) throws Exception
    {
        DeleteFilesRecursively(directoryPath, "*");
        DeleteEmptyDirectoriesRecursively(directoryPath);
    }

    /** Gets array of files in a directory.
     *
     * @param dir Absolute directory path
     * @return Array of files
     */
    public static ArrayList<File> GetFilesInDirectory(String dir)
    {
        return GetFilesInDirectory(dir, "*", false);
    }

    /** Gets array of files in a directory.
     *
     * @param dir Absolute directory path
     * @param pattern File pattern to match
     * @return Array of files
     */
    public static ArrayList<File> GetFilesInDirectory(String dir, String pattern)
    {
        return GetFilesInDirectory(dir, pattern, false);
    }

    /** Gets array of files in a directory that match a specified file pattern.
     *
     * @param dir Absolute directory path
     * @param pattern File pattern to match
     * @param includeSubdirectories Whether to include subdirectories in the search
     * @return List of files
     */
    public static ArrayList<File> GetFilesInDirectory(String dir, String pattern, boolean includeSubdirectories)
    {
        File directory = new File(dir);
        File[] matches = directory.listFiles(new WildCardFileFilter(pattern));

        ArrayList<File> fileMatches = new ArrayList<File>();

        if (matches == null)
            return fileMatches;

        for (File file : matches)
            if (includeSubdirectories || file.isFile())
                fileMatches.add(file);

        return fileMatches;
    }

    /** This method searches a directory for the files that match the specified pattern. After finding any matching files, it sorts the files by file name and accounts for numeric values properly in the sorting.
     *
     * @param dirPath Directory to be searched
     * @return Sorted list of files found
     * @throws Exception
     */
    public static ArrayList<File> GetFilesInDirectorySorted(String dirPath) throws Exception
    {
        return GetFilesInDirectorySorted(dirPath, "*", false);
    }

    /** This method searches a directory for the files that match the specified pattern. After finding any matching files, it sorts the files by file name and accounts for numeric values properly in the sorting.
     *
     * @param dirPath Directory to be searched
     * @param filePattern File pattern to be searched for (can include wildcards)
     * @param includeSubdirectories Whether to include subdirectories in the search
     * @return Sorted list of files found
     * @throws Exception
     */
    public static ArrayList<File> GetFilesInDirectorySorted(String dirPath, String filePattern, boolean includeSubdirectories) throws Exception
    {
        ArrayList<File> files = GetFilesInDirectory(dirPath, filePattern, includeSubdirectories);

        ArrayList<String> fileNames = new ArrayList<String>();
        for (File file : files)
            fileNames.add(file.getName());

        ArrayList<File> sortedFiles =  new ArrayList<File>();
        for (String fileName : ListUtilities.SortStringList(fileNames))
            sortedFiles.add(new File(dirPath + fileName));

        return sortedFiles;
    }

    /** This method searches a directory for the files that match the specified pattern. After finding any matching files, it sorts the files by file name and accounts for numeric values properly in the sorting.
     *
     * @param dirPath Directory to be searched
     * @param filePattern File pattern to be searched for (can include wildcards)
     * @return Sorted list of files found
     * @throws Exception
     */
    public static ArrayList<File> GetFilesInDirectorySorted(String dirPath, String filePattern) throws Exception
    {
        ArrayList<File> files = GetFilesInDirectory(dirPath, filePattern);

        ArrayList<String> fileNames = new ArrayList<String>();
        for (File file : files)
            fileNames.add(file.getName());

        ArrayList<File> sortedFiles =  new ArrayList<File>();
        for (String fileName : ListUtilities.SortStringList(fileNames))
            sortedFiles.add(new File(dirPath + fileName));

        return sortedFiles;
    }

    /** Gets a list of file objects in a directory and its subdirectories that match a specified file pattern.
     *
     * @param dirPath Absolute directory path
     * @param pattern File pattern to match
     * @return List of matching files
     * @throws Exception
     */
    public static ArrayList<File> GetFilesInDirectoryRecursively(String dirPath, String pattern) throws Exception
    {
        return GetObjectsInDirectoryRecursively(dirPath, pattern, false);
    }

    /** Gets a list of file (and directory) objects in a directory and its subdirectories that match a specified file pattern.
     *
     * @param dirPath Absolute directory path
     * @param pattern File pattern to match
     * @param includeDirectories Whether to include directory objects also
     * @return List of matching files
     * @throws Exception
     */
    public static ArrayList<File> GetObjectsInDirectoryRecursively(String dirPath, String pattern, boolean includeDirectories) throws Exception
    {
        ArrayList<File> results = new ArrayList<File>();

        if (!FileUtilities.DirectoryExists(dirPath))
            return results;

        File directory = new File(dirPath);
        FilenameFilter filter = new WildCardFilenameFilter(pattern);

        for (File file : directory.listFiles())
        {
            if (filter.accept(directory, file.getName()))
                results.add(file);
            else
            {
                if (file.isDirectory())
                {
                    results.addAll(GetObjectsInDirectoryRecursively(file.getAbsolutePath(), pattern, includeDirectories));

                    if (includeDirectories && !results.contains(file))
                        results.add(file);
                }
            }
        }

        return results;
    }

    /** Checks whether a directory currently exists. If not, it (and any parent directories that don't exist) are attempted to be created.
     *
     * @param dirPath Absolute directory path
     * @return Absolute directory path
     * @throws Exception
     */
    public static String CreateDirectoryIfNotExists(String dirPath) throws Exception
    {
        File dir = new File(dirPath);
        if (!dir.exists())
        {
            if (!dir.mkdirs())
                Singletons.Log.Debug("A new directory could not be created at " + dirPath + ".");
        }

        return dirPath;
    }

    /** Indicates whether a file exists (and is not a directory).
     *
     * @param filePath Absolute file path
     * @return Whether the file exists (and is not a directory)
     * @throws Exception
     */
    public static boolean FileExists(String filePath) throws Exception
    {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }

    /** Indicates whether a directory exists.
     *
     * @param filePath Absolute directory path
     * @return Whether the directory exists
     * @throws Exception
     */
    public static boolean DirectoryExists(String filePath) throws Exception
    {
        return new File(filePath).exists();
    }

    /** Checks whether the directory in an absolute file path exists. If not, it is created.
     *
     * @param filePath Absolute file path
     * @throws Exception
     */
    public static void CreateFileDirectoryIfNotExists(String filePath) throws Exception
    {
        File file = new File(filePath);
        String dirPath = file.getParent();

        if (dirPath == null) // If it is just a file name
            return;

        CreateDirectoryIfNotExists(dirPath);
    }

    /** Moves a file from one location to another
     *
     * @param fromFilePath Absolute file path of existing file
     * @param toFilePath Absolute file path to be created
     */
    public static void MoveFile(String fromFilePath, String toFilePath)
    {
        File fromFile = new File(fromFilePath);
        File toFile = new File(toFilePath);

        if (fromFile.exists())
            fromFile.renameTo(toFile);
    }

    /** Creates a directory. If an error occurs while creating the directory, an exception is not thrown. It is understood that sometimes directories are attempted to be created when another thread is also trying to create it, and this sometimes is not considered problematic.
     *
     * @param directoryPath Absolute directory path
     */
    public static boolean CreateDirectoryNoFatalError(String directoryPath)
    {
        File dir = new File(directoryPath);
        if (!dir.exists())
        {
            try
            {
                dir.mkdirs();
                return true;
            }
            catch (Exception ex)
            {
                Singletons.Log.Debug(ex);
                return false;
            }
        }

        return  true;
    }

    /** Deletes a directory.
     *
     * @param directoryPath Absolute directory path
     */
    public static void RemoveDirectory(String directoryPath)
    {
        File directory = new File(directoryPath);
        if (!directory.exists())
            return;

        DeleteFilesInDirectory(directoryPath);
        directory.delete();
    }

    /** Creates (atomically) an empty file.
     *
     * @param filePath Absolute file path
     * @return Whether the file was created successfully
     * @throws Exception
     */
    public static boolean CreateEmptyFile(String filePath) throws Exception
    {
        File file = new File(filePath);
        return CreateEmptyFile(file.getParent(), file.getName());
    }

    /** Creates (atomically) an empty file.
     *
     * @param directoryPath Absolute directory path
     * @param fileName File name
     * @return Whether the file was created successfully
     * @throws Exception
     */
    public static boolean CreateEmptyFile(String directoryPath, String fileName) throws Exception
    {
        if (!FileUtilities.CreateDirectoryNoFatalError(directoryPath))
            return false;

        if (!directoryPath.endsWith("/"))
            directoryPath += "/";

        try
        {
            return new File(directoryPath + fileName).createNewFile();
        }
        catch (Exception ex)
        {
            Singletons.Log.Debug("File could not be created at " + directoryPath + fileName + ". This will be attempted again later.");
            Singletons.Log.Debug(ex);

            return false;
        }
    }

    /** This method parses the file name from a file path. If there is an extension, it removes the extension too.
     *
     * @param filePath Full or relative file path
     * @return File name with no extension
     */
    public static String RemoveFileExtension(String filePath)
    {
        String simpleName = new File(filePath).getName();

        if (simpleName.contains("."))
            simpleName = simpleName.substring(0, simpleName.lastIndexOf('.'));

        return simpleName;
    }

    /** Reads the first line of a file and indicates the header values (column names) in that file.
     *
     * @param filePath Absolute path to the file
     * @return Header values (column names) in the file
     * @throws Exception
     */
    public static ArrayList<String> ReadHeaderLineFromFile(String filePath) throws Exception
    {
        BigFileReader fileReader = new BigFileReader(filePath);
        ArrayList<String> headerItems = ListUtilities.CreateStringList(fileReader.ReadLine().split("\t"));
        fileReader.Close();

        return headerItems;
    }

    /** Inserts a header line into an existing file.
     *
     * @param filePath Path to the file
     * @param header Header that will be added to the file
     * @throws Exception
     */
    public static void InsertFileHeaderLine(String filePath, String header) throws Exception
    {
        WriteTextToFile(filePath, header + "\n" + ReadTextFile(filePath));
    }

    /** Concatenates multiple text files. It also can add a description before each file as well as an overall header describing the output file.
     *
     * @param inFilePaths Input paths that will be combined
     * @param inFileDescriptions An optional description for each input file
     * @param outFilePath Path to the output file that will be created
     * @param headerComment An optional comment that will be placed at the top of the output file
     * @throws Exception
     */
    public static void ConcatenateFiles(ArrayList<String> inFilePaths, ArrayList<String> inFileDescriptions, String outFilePath, String headerComment) throws Exception
    {
        StringBuffer out = new StringBuffer();

        if (headerComment != null && !headerComment.equals(""))
            out.append("#" + headerComment + "\n");

        for (int i=0; i<inFilePaths.size(); i++)
        {
            if (inFileDescriptions != null)
                out.append(inFileDescriptions.get(i));

            out.append(ReadTextFile(inFilePaths.get(i)));
        }

        FileUtilities.WriteTextToFile(outFilePath, out.toString());
    }
}
