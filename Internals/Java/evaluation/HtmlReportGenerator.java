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

package mlflex.evaluation;

import mlflex.core.*;
import mlflex.dataprocessors.AbstractDataProcessor;
import mlflex.ensemblelearners.AbstractEnsembleLearner;
import mlflex.helper.FileUtilities;
import mlflex.helper.ListUtilities;

import java.io.File;
import java.util.ArrayList;

/** This class is used to generate an HTML report that summarizes the results of this experiment. It takes the output files and puts a shiny cover on it.
 */
public class HtmlReportGenerator
{
    private static final String LOGO_FILE_NAME = "Logo.gif";

    private StringBuffer _htmlBuffer = new StringBuffer();
    private String _htmlHeader = null;
    private String _htmlFooter = null;

    /** This method does the overall work of creating the report by adding the various components to it.
     *
     * @throws Exception
     */
    public void Save() throws Exception
    {
        // Initialize the main page
        _htmlBuffer.append(GetHtmlHeader().replace(LOGO_FILE_NAME, Settings.HTML_RELATIVE_DIR + LOGO_FILE_NAME));

        AddItalicsHeader(_htmlBuffer, "This report summarizes the output of the " + Singletons.Experiment.Name + " experiment. The links below provide access to information about the data that were used, classification performance that was attained, settings that were used, etc. All data displayed in this report can be found in parsable form in the Output directory within ML-Flex.");

        AddSummaryStatistics();

        // Add classification results if classification was performed
        if (Settings.NeedToClassify())
        {
            AddClassificationResults();

            if (Settings.NeedToEnsembleLearn())
                AddEnsembleResults();

            if (Settings.NeedToSelectFeatures())
                AddFeatureSelectionResults();
        }

        // Add a link to export files
        if (Settings.EXPORT_DATA)
            AddExportFiles();

        AddSettingsFiles();

        // Add the footer
        _htmlBuffer.append(GetHtmlFooter());

        // Save the main report page
        SaveToFile(_htmlBuffer.toString(), GetReportFilePath(), false);
        CopyLogo(Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR + LOGO_FILE_NAME);

        Singletons.Log.Info("The output of this experiment can be found at " + Settings.OUTPUT_DIR + ".");
    }

    private void AddSummaryStatistics() throws Exception
    {
        AddSectionHeader(_htmlBuffer, "Summary Statistics");
        for (AbstractDataProcessor processor : Singletons.ProcessorVault.AllDataProcessors)
            AddLink(_htmlBuffer, processor.GetStatisticsFilePath(), processor.GetDescription().replace("_", " ") + " Data", "", "", false);
    }

    private void AddClassificationResults() throws Exception
    {
        AddSectionHeader(_htmlBuffer, "Classification Results");

        // Add overall summaries
        AddLink(_htmlBuffer, Settings.GetOutputResultsDir("", false) + new ClassificationResultsFilePaths("").PERFORMANCE_METRICS, "Overall Summary of Performance Metrics", "", "", false);
        AddLink(_htmlBuffer, Settings.GetOutputResultsDir("", false) + new ClassificationResultsFilePaths("").PER_CLASS_METRICS, "Overall Summary of Per-Class Metrics", "", "", false);

        for (ModelSelector modelSelector : ModelSelector.GetAllModelSelectors())
        {
            // Add results for each combination of data processor and algorithms
            AddItalicsHeader(_htmlBuffer, modelSelector.GetDescription(" - "));
            _htmlBuffer.append("<ul>");
            for (java.io.File file : FileUtilities.GetFilesInDirectorySorted(modelSelector.GetResultsFilePaths(false, false).DIRECTORY_PATH))
            {
                _htmlBuffer.append("<li>");
                AddLink(_htmlBuffer, file.getAbsolutePath(), "", "", "", false);
            }

            // Add multi-iteration reports where applicable
            if (Singletons.Config.GetNumIterations() > 1)
            {
                _htmlBuffer.append("<li>");
                AddIterationLinks(modelSelector.GetResultsFilePaths(false, false).DIRECTORY_PATH, "", modelSelector.GetResultsFilePaths(false, false).DIRECTORY_PATH.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR), "Results per Iteration", "../../../../../");
            }

            _htmlBuffer.append("</ul>");

            // Add reports for inner cross-validation folds when applicable
            if (Settings.NeedToEvaluateInnerFolds() && Settings.IsTrainTestExperiment())
            {
                AddItalicsHeader(_htmlBuffer, modelSelector.GetDescription(" - ") + " - Training Data");

                // Add results for each combination of data processor and algorithms
                _htmlBuffer.append("<ul>");
                for (java.io.File file : FileUtilities.GetFilesInDirectorySorted(modelSelector.GetResultsFilePaths(false, true).DIRECTORY_PATH))
                {
                    _htmlBuffer.append("<li>");
                    AddLink(_htmlBuffer, file.getAbsolutePath());
                }

                // Add multi-iteration reports where applicable
                if (Singletons.Config.GetNumIterations() > 1)
                {
                    _htmlBuffer.append("<li>");
                    AddIterationLinks(modelSelector.GetResultsFilePaths(false, false).DIRECTORY_PATH, "Training_Data", modelSelector.GetResultsFilePaths(false, true).DIRECTORY_PATH.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR), "Results per Iteration", "../../../../../../");
                }

                _htmlBuffer.append("</ul>");
            }
        }
    }

    private void AddEnsembleResults() throws Exception
    {
        if (Settings.NeedToEnsembleLearn())
            AddSectionHeader(_htmlBuffer, "Ensemble Results");

        for (AbstractEnsembleLearner ensemblePredictor : AbstractEnsembleLearner.GetAllEnsembleLearners())
        {
            AddItalicsHeader(_htmlBuffer, ensemblePredictor.GetFormattedDescription() + "<ul>");

            // Add results for each ensemble learner
            for (java.io.File file : FileUtilities.GetFilesInDirectorySorted(ensemblePredictor.GetResultsFilePaths(false).DIRECTORY_PATH))
            {
                _htmlBuffer.append("<li>");
                AddLink(_htmlBuffer, file.getAbsolutePath());
            }

            // Add multi-iteration reports where applicable
            if (Singletons.Config.GetNumIterations() > 1)
            {
                _htmlBuffer.append("<li>");
                AddIterationLinks(ensemblePredictor.GetResultsFilePaths(false).DIRECTORY_PATH, "", ensemblePredictor.GetResultsFilePaths(false).DIRECTORY_PATH.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR), "Results Per Iteration", "../../../../");
            }

            _htmlBuffer.append("</ul>");
        }
    }

    private void AddFeatureSelectionResults() throws Exception
    {
        AddSectionHeader(_htmlBuffer, "Feature Selection Results");

        for (AbstractDataProcessor processor : Singletons.ProcessorVault.IndependentVariableDataProcessors)
            for (FeatureSelectionAlgorithm fsAlgorithm : Singletons.Config.GetFeatureSelectionAlgorithms(processor))
                if (FeatureSelectionEvaluator.NeedToSelectFeatures(processor, fsAlgorithm))
                {
                    // Add a page that summarizes the mean feature ranks for each combination of data processor and algorithm
                    String description = processor.GetDescription() + " - " + fsAlgorithm;
                    String ranksFilePath = new FeatureSelectionResultsSaver(processor, fsAlgorithm).GetOutFilePath(false);

                    if (Singletons.Config.GetNumIterations() == 1)
                        AddLink(_htmlBuffer, ranksFilePath, description, "", "", false);
                    else
                    {
                        // Add results for each iteration where applicable
                        String rootDirPath = new File(ranksFilePath).getParent() + "/";
                        AddIterationLinks(rootDirPath, "", rootDirPath.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR), description, "../../../../");
                    }
                }
    }

    private void AddExportFiles() throws Exception
    {
        AddSectionHeader(_htmlBuffer, "Exported Data Files");
        for (File file : FileUtilities.GetFilesInDirectorySorted(Settings.GetOutputExportDir(), "*"))
            AddLink(_htmlBuffer, file.getAbsolutePath(), file.getName(), "", "", true);
    }

    private void AddSettingsFiles() throws Exception
    {
        AddSectionHeader(_htmlBuffer, "Settings");

        for (File file : FileUtilities.GetFilesInDirectorySorted(Settings.GetOutputSettingsDir(false)))
            AddLink(_htmlBuffer, file.getAbsolutePath());

        if (Singletons.Config.GetNumIterations() > 1)
            // Add description files for each iteration
            AddIterationLinks(Settings.GetOutputSettingsDir(false), "", Settings.GetOutputSettingsDir(false).replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR), "Settings Per Iteration", "../../");
    }

    private String GetHtmlHeader() throws Exception
    {
        if (_htmlHeader == null)
            _htmlHeader = FileUtilities.ReadTextFile(Settings.INTERNALS_DIR + Settings.HTML_RELATIVE_DIR + "Header.html");

        return _htmlHeader;
    }

    private String GetHtmlFooter() throws Exception
    {
        if (_htmlFooter == null)
            _htmlFooter = FileUtilities.ReadTextFile(Settings.INTERNALS_DIR + Settings.HTML_RELATIVE_DIR + "Footer.html");

        return _htmlFooter;
    }

    private void AddSectionHeader(StringBuffer buffer, String text)
    {
        buffer.append("<h2>" + text + "</h2>");
    }

    private void AddItalicsHeader(StringBuffer buffer, String text)
    {
        buffer.append("<em>" + text + "</em>");
    }

    private void AddRelativeLink(StringBuffer buffer, String relativeFilePath, String description) throws Exception
    {
        buffer.append("<p><a href='" + relativeFilePath + "'>" + description + "</a></p>");
    }

    private ArrayList<ArrayList<String>> GetFileLines(String filePath) throws Exception
    {
        ArrayList<ArrayList<String>> fileLines = FileUtilities.ParseDelimitedFile(filePath, "\t", "!"); // In order to keep the header comment, use an unusual commentChar

        // Check whether the header row in the table is missing the top-left cell
        if ((fileLines.size() > 2 && fileLines.get(1).size() < fileLines.get(2).size()) || (fileLines.size() == 2 && fileLines.get(1).size() > 1))
            fileLines.get(1).add(0, "");

        return fileLines;
    }

    // Auto detect to see if the file has a table structure
    private boolean FileHasTableFormat(ArrayList<ArrayList<String>> fileLines)
    {
        // File has only a header comment and a single line after it
        if (fileLines.size() <= 2)
            return false;

        // File has a header and more than two lines, but the lines are not separated by tabs
        if (fileLines.size() > 2 && fileLines.get(2).size() == 1)
            return false;

        return true;
    }

    private String ExtractFileHeaderComment(ArrayList<ArrayList<String>> fileLines)
    {
        return fileLines.remove(0).get(0).substring(2);
    }

    private void AddLink(StringBuffer buffer, String textFilePath) throws Exception
    {
        AddLink(buffer, textFilePath, "", "", "", false);
    }

    private void AddLink(StringBuffer buffer, String textFilePath, String linkText, String linkedPageHeader, String relativePathPrefix, boolean neverConvertToTable) throws Exception
    {
        // Read the text from the existing file
        ArrayList<ArrayList<String>> fileLines = GetFileLines(textFilePath);

        // Formulate what will be used for the links
        linkText = linkText == "" ? FormatFileName(textFilePath) : linkText;
        linkedPageHeader = linkedPageHeader == "" ? linkText : linkedPageHeader;

        String relativePath = "";

        // Check to see if the file has a table format and should be converted to an HTML table
        if (FileHasTableFormat(fileLines) && !neverConvertToTable)
        {
            String outFilePath = textFilePath.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR).replace(".txt", ".html");
            relativePath = textFilePath.replace(Settings.OUTPUT_DIR, Settings.HTML_RELATIVE_DIR).replace(".txt", ".html");
            // Format the data as an HTML table and save it
            SaveToFile(FormatHtmlTable(fileLines, linkedPageHeader), outFilePath, true);
        }
        else
        {
            // Copy the file and link to it
            String outFilePath = textFilePath.replace(Settings.OUTPUT_DIR, Settings.OUTPUT_DIR + Settings.HTML_RELATIVE_DIR);
            relativePath = textFilePath.replace(Settings.OUTPUT_DIR, Settings.HTML_RELATIVE_DIR);

            FileUtilities.CreateFileDirectoryIfNotExists(outFilePath);
            FileUtilities.CopyFile(textFilePath, outFilePath);
        }

        AddRelativeLink(buffer, relativePathPrefix + relativePath, linkText);
    }

    private String FormatFileName(String textFilePath)
    {
        return FileUtilities.RemoveFileExtension(textFilePath).replace("_", " ");
    }

    private String FormatHtmlTable(ArrayList<ArrayList<String>> fileLines, String description) throws Exception
    {
        StringBuffer out = new StringBuffer();

        out.append(GetHtmlHeader());
        AddSectionHeader(out, description);
        out.append("<p><em>" + ExtractFileHeaderComment(fileLines) + "</em></p>");

        out.append("<p><table border=2 cellpadding=5 cellspacing=0>");
        for (int i=0; i<fileLines.size(); i++)
        {
            ArrayList<String> lineItems = fileLines.get(i);

            // Italicize the first row
            if (i == 0)
                out.append("<tr><td nowrap='nowrap'><em>" + ListUtilities.Join(lineItems, "</em></td><td nowrap='nowrap'><em>").replace("_", " ") + "</em></td></tr>");
            else
                out.append("<tr><td nowrap='nowrap'>" + ListUtilities.Join(lineItems, "</td><td nowrap='nowrap'>") + "</td></tr>");
        }
        out.append("</table></p>");

        out.append(GetHtmlFooter());

        return out.toString();
    }

    private void AddIterationLinks(String iterationRootDirPath, String iterationSubDirPath, String outDirPath, String description, String relativePathPrefix) throws Exception
    {
        StringBuffer iterationBuffer = new StringBuffer();
        iterationBuffer.append(GetHtmlHeader());

        // Add a link for each iteration
        for (java.io.File iterationDir : FileUtilities.GetFilesInDirectorySorted(iterationRootDirPath, "Iteration", true))
        {
            AddSectionHeader(iterationBuffer, iterationDir.getName().replace("Iteration", "Iteration "));

            for (java.io.File file : FileUtilities.GetFilesInDirectorySorted(iterationDir.getAbsolutePath() + "/" + iterationSubDirPath))
                AddLink(iterationBuffer, file.getAbsolutePath(), "", "", relativePathPrefix, false);
        }

        String outFilePath = outDirPath + "Per_Iteration.html";

        iterationBuffer.append(GetHtmlFooter());
        SaveToFile(iterationBuffer.toString(), outFilePath, true);
        AddRelativeLink(_htmlBuffer, outFilePath.replace(Settings.OUTPUT_DIR, ""), description);
    }

    private void SaveToFile(String html, String filePath, boolean copyLogo) throws Exception
    {
        FileUtilities.CreateFileDirectoryIfNotExists(filePath);
        FileUtilities.WriteTextToFile(filePath, html);

        if (copyLogo)
            CopyLogo(new File(filePath).getParent() + "/" + LOGO_FILE_NAME);
    }

    private void CopyLogo(String destinationFilePath) throws Exception
    {
        if (!FileUtilities.FileExists(destinationFilePath))
            FileUtilities.CopyFile(Settings.INTERNALS_DIR + Settings.HTML_RELATIVE_DIR + LOGO_FILE_NAME, destinationFilePath);
    }

    private static String GetReportFilePath()
    {
        return Settings.OUTPUT_DIR + "Report.html";
    }
}
