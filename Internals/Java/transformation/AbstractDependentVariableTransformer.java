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

package mlflex.transformation;

/** Dependent variables can be transformed before they are used in machine-learning analyses. For example, if the dependent variable is continuous, a transformer can be used to convert it to a discrete value. This class supports generic functionality for performing such transformations. Classes that inherit from this class can support custom transformations.
 * @author Stephen Piccolo
 */
public abstract class AbstractDependentVariableTransformer
{
    /** This method performs transformations of a single dependent variable value. In some cases, this transformation is different depending on the cross validation fold.
     * @param rawValue Raw dependent variable value
     * @return Transformed value
     * @throws Exception
     */
    public abstract String TransformDependentVariableValue(String rawValue) throws Exception;
}