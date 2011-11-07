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

import mlflex.helper.FileUtilities;

/** This class simply contains paths to various output files that will record the results of classification.
 *
 */
public class ClassificationResultsFilePaths
{
    /** Directory where the files are located. */
    public String DIRECTORY_PATH;
    /** Path to the file containing performance metrics. */
    public String PERFORMANCE_METRICS;
    /** Path to the file contain class-level metrics. */
    public String PER_CLASS_METRICS;
    /** Path to the file containing a confusion matrix of the results. */
    public String CONFUSION_MATRIX;
    /** Path to the file that lists the output of the classification algorithm. */
    public String ALGORITHM_OUTPUT;
    /** Path to the file that records the number of features versus AUC values. */
    public String NUM_FEATURES;
    /** Path to the file that list prediction info. */
    public String PREDICTIONS;

    /** This constructor allows the user to specify the directory where the files will be saved. This directory will be created if it doesn't already exist.
     *
     * @param directoryPath Absolute path to where the files will be saved
     * @throws Exception
     */
    public ClassificationResultsFilePaths(String directoryPath) throws Exception
    {
        FileUtilities.CreateDirectoryIfNotExists(directoryPath);

        DIRECTORY_PATH = directoryPath;
        PERFORMANCE_METRICS = directoryPath + "Performance_Metrics.txt";
        PER_CLASS_METRICS = directoryPath + "Per_Class_Metrics.txt";
        CONFUSION_MATRIX = directoryPath + "Confusion_Matrix.txt";
        ALGORITHM_OUTPUT = directoryPath + "Algorithm_Output.txt";
        NUM_FEATURES = directoryPath + "Number_of_Features_vs_AUC.txt";
        PREDICTIONS = directoryPath + "Predictions.txt";
    }
}
