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

def parseArffAttributes(filePath):
    # Get lines in the file that have no comment character and are not empty
    fileLines = [line.strip() for line in file(filePath) if not line.startswith("%") and len(line.strip()) > 0]

    # Parse out only the lines that are attributes (except the class attribute)
    attributeLines = [line.replace("\t", " ") for line in fileLines if line.upper().startswith("@ATTRIBUTE ") and not line.upper().startswith("@ATTRIBUTE CLASS")]

    # Parse out the attribute names and return them
    return [attribute.split(" ")[1] for attribute in attributeLines]

# Get the arguments that have been specified at the command line
inputFilePath = sys.argv[1]
randomSeed = int(sys.argv[2]) # This random seed demonstrates how to pass other arguments to this learner
outFilePath = sys.argv[3]

attributes = parseArffAttributes(inputFilePath)

# Set the random seed, so the random shuffling will be reproducible
random.seed(randomSeed)

# Randomly rank the attributes (this is a dumb learner for demo purposes)
random.shuffle(attributes)

# Write the attributes that have been ranked (randomly) to the output file
outFile = open(outFilePath, 'w')
for attribute in attributes:
    outFile.write(attribute + "\n")
outFile.close()
