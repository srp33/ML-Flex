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

import os, sys, random

# Get the arguments that have been specified at the command line
inputTrainingFilePath = sys.argv[1]
inputTestFilePath = sys.argv[2]
randomSeed = int(sys.argv[3]) # This random seed demonstrates how to pass other arguments to this learner
outFilePath = sys.argv[4]

# Read the data from the input files (assume data separated by tabs)
trainingData = [line.rstrip().split("\t") for line in file(inputTrainingFilePath)]
testData = [line.rstrip().split("\t") for line in file(inputTestFilePath)]

# Get the class value for each instance (values in last row, ignore row name)
testClasses = testData[-1][1:]

# Discover the different class values that have been used
testClassOptions = sorted(list(set(testClasses)))

# Set the random seed, so the random shuffling will be reproducible
random.seed(randomSeed)

# Randomly shuffle the class values (this is a dumb learner for demo purposes)
random.shuffle(testClasses)

# Open the output file
outFile = open(outFilePath, 'w')

# Write the header row = "Prediction" and one entry for each class, separated by tabs
outFile.write("\t".join(["Prediction"] + testClassOptions) + "\n")

for testClass in testClasses:
    classProbabilities = ["0.0" for classOption in testClassOptions] # Set the class probabilities all to zero by default
    classProbabilities[testClassOptions.index(testClass)] = "1.0" # Set the predicted class probability to one (hard threshold)
    outFile.write("\t".join([testClass] + classProbabilities) + "\n") # Output the values, separated by tabs

outFile.close()
