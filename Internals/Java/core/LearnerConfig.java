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

package mlflex.core;

import mlflex.learners.AbstractMachineLearner;

import java.lang.reflect.Constructor;

/**
 * This class stores information that is configured for a given learner. For example, it helps ML-Flex know how to interface with third-party software packages.
 * @author Stephen Piccolo
 */
public class LearnerConfig
{
    public String Key;
    public AbstractMachineLearner MachineLearner;
    public String CommandTemplate;

    /** Constructor.
     *
     * @param key Unique name to reference the learner
     * @param learnerClassName Full name of the ML-Flex Java class that extends mlflex.learners.AbstractMachineLearner that contains the logic for interfacing with the learner
     * @param commandTemplate Template (where applicable) that is used to interface with an external software package at the command line
     * @throws Exception
     */
    public LearnerConfig(String key, String learnerClassName, String commandTemplate) throws Exception
    {
        Key = key;
        MachineLearner = (AbstractMachineLearner) ((Constructor) Class.forName(learnerClassName).getConstructor()).newInstance();
        CommandTemplate = commandTemplate;
    }
}
