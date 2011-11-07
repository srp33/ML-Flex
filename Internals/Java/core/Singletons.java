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

import mlflex.helper.Config;
import mlflex.helper.Log;

/** The objects specified in this class are instantiated only once, at the beginning of the experiment and can be accessed globally.
 */
public class Singletons
{
    /** An object that contains code for executing the current experiment */
    public static Experiment Experiment;
    /** Object used for logging */
    public static Log Log;
    /** Object used to access configuration settings for the experiment */
    public static Config Config;
    /** The current iteration */
    public static int Iteration;
    /** The random seed that applies to this experiment */
    public static long RandomSeed;
    /** Object that contains information about data processors used in this experiment */
    public static ProcessorVault ProcessorVault;
    /** Object that provides access to data instances and stores them in memory */
    public static InstanceVault InstanceVault;
}
