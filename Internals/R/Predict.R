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

trainTestSvm = function(trainData, testData, kernelType)
{
  library(e1071)

  trainClasses = as.factor(trainData[,ncol(trainData)])
  trainData = trainData[,-ncol(trainData)]

  best.parameters = tune.svm(trainData, trainClasses, kernel=kernelType, probability=TRUE, cost=c(0.01,1,100,1000), tunecontrol = tune.control(sampling="cross"))$best.parameters
  model = svm(trainData, trainClasses, type='C', kernel=kernelType, probability=TRUE, cost=best.parameters[,"cost"])

  predictResult = predict(model, testData, probability=TRUE)
  predictedClasses = convertFactorsToCharacter(predictResult)
  predictedProbabilities = attr(predictResult, "probabilities")

  if (!is.matrix(predictedProbabilities))
  {
    results = t(matrix(c(predictedClasses, predictedProbabilities)))
    colnames(results) = c("predictedClasses", names(predictedProbabilities))
  }
  else {
    results = cbind(predictedClasses, predictedProbabilities)
  }

  return(results)
}

convertFactorsToCharacter = function(x)
{
  if (is.factor(x))
    return(as.character(levels(x)[x]))
  else
    return(x)
}

algorithmDescription = commandArgs()[7]
trainDataFilePath = commandArgs()[8]
testDataFilePath = commandArgs()[9]
outputFilePath = commandArgs()[10]

trainData = read.table(trainDataFilePath, sep="\t", row.names=NULL, stringsAsFactors=FALSE, header=TRUE, quote="\"")
testData = read.table(testDataFilePath, sep="\t", row.names=NULL, stringsAsFactors=FALSE, header=TRUE, quote="\"")

saveResults = function(results)
{
  write.table(results, outputFilePath, sep="\t", row.names=FALSE, col.names=TRUE, quote=FALSE)
}

if (grepl("^svm", algorithmDescription))
{
  results = trainTestSvm(trainData, testData, strsplit(algorithmDescription, "_")[[1]][2])
  saveResults(results)
}
