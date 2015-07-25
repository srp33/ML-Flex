# THIS SOURCE CODE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND, AND ITS AUTHOR AND THE JOURNAL OF MACHINE LEARNING RESEARCH (JMLR) AND JMLR'S PUBLISHERS AND DISTRIBUTORS, DISCLAIM ANY AND ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND ANY WARRANTIES OR NON INFRINGEMENT. THE USER ASSUMES ALL LIABILITY AND RESPONSIBILITY FOR USE OF THIS SOURCE CODE, AND NEITHER THE AUTHOR NOR JMLR, NOR JMLR'S PUBLISHERS AND DISTRIBUTORS, WILL BE LIABLE FOR DAMAGES OF ANY KIND RESULTING FROM ITS USE. Without lim- iting the generality of the foregoing, neither the author, nor JMLR, nor JMLR's publishers and distributors, warrant that the Source Code will be error-free, will operate without interruption, or will meet the needs of the user.
# 
# --------------------------------------------------------------------------
# 
# Copyright 2011 Stephen Piccolo
# 
# This file is part of ML-Flex.
# 
# ML-Flex is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# any later version.
# 
# ML-Flex is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with ML-Flex. If not, see <http://www.gnu.org/licenses/>.

import sys, math, random, os
from datetime import datetime
import orange, orngBayes, orngTest, orngStat, orngTree, orngFSS, orngSVM, orngEnsemble

def trainTest(learnerType, trainingFilePath, testFilePath, outputPredictionsFilePath, outputProbabilitiesFilePath):
    trainingData = orange.ExampleTable(trainingFilePath)
    testData = orange.ExampleTable(testFilePath)

    learner = eval(learnerType)
    classifier = learner(trainingData)

    predictions = ""

    probabilities = "\t".join(str(x) for x in trainingData.domain.classVar.values) + "\n"

    for testExample in testData:
        classification = classifier(testExample, orange.GetBoth)
        predictions += "%s\n" % classification[0].value
        probabilities += "\t".join([str(x) for x in classification[1]]) + "\n"

    classifier = None
    trainingData = None
    testData = None

    predictionsFile = open(outputPredictionsFilePath, 'w')
    predictionsFile.write(predictions)
    predictionsFile.close()

    probabilitiesFile = open(outputProbabilitiesFilePath, 'w')
    probabilitiesFile.write(probabilities)
    probabilitiesFile.close()

def rankFeatures(selectionType, dataFilePath, outputFilePath):
    examples = orange.ExampleTable(dataFilePath)

    attMeasures = eval(selectionType)
    rankedFeatures = orngFSS.bestNAtts(attMeasures, len(examples.domain.attributes))
    rankedFeatures = [examples.domain[feature].name for feature in rankedFeatures]

    examples = None

    outFile = open(outputFilePath, 'w')
    for rankedFeature in rankedFeatures:
        outFile.write(rankedFeature + "\n")
    outFile.close()

if sys.argv[1] == "rankFeatures":
    rankFeatures(sys.argv[2], sys.argv[3], sys.argv[4])
else:
    if sys.argv[1] == "trainTest":
        trainTest(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6])
    else:
        print "Either rankFeatures or trainTest must be specified"
