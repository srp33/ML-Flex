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
inputFilePath = sys.argv[1]
randomSeed = int(sys.argv[2]) # This random seed demonstrates how to pass other arguments to this learner
outFilePath = sys.argv[3]

# Parse the attribute names from the first element in each row in the file
attributes = [line.split("\t")[0] for line in file(inputFilePath)]

# Ignore the header row and the class row
del attributes[0]
del attributes[-1] # -1 = last element in Python

# Set the random seed, so the random shuffling will be reproducible
random.seed(randomSeed)

# Randomly rank the attributes (this is a dumb learner for demo purposes)
random.shuffle(attributes)

# Write the attributes that have been ranked (randomly) to the output file
outFile = open(outFilePath, 'w')
for attribute in attributes:
    outFile.write(attribute + "\n")
outFile.close()
