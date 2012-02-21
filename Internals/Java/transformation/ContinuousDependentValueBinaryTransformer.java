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

package mlflex.transformation;

/** When the dependent variable is continuous, it can be transformed to a discrete value. This transformer enables it to be transformed depending on whether the continuous value is greater than or less than a specific value.
 * @author Stephen Piccolo
 */
public class ContinuousDependentValueBinaryTransformer extends AbstractDependentVariableTransformer
{
    private double _thresholdValue;
    private String _lowDescriptor;
    private String _highDescriptor;

    /** Constructor
     *
     * @param thresholdValue The value threshold that separates the "low" instances from the "high" instances
     * @param lowDescriptor The text descriptor for the "low" instances
     * @param highDescriptor The text descriptor for the "high" instances
     */
    public ContinuousDependentValueBinaryTransformer(double thresholdValue, String lowDescriptor, String highDescriptor)
    {
        _thresholdValue = thresholdValue;
        _lowDescriptor = lowDescriptor;
        _highDescriptor = highDescriptor;
    }

    @Override
    public String TransformDependentVariableValue(String rawValue) throws Exception
    {
        return Double.parseDouble(rawValue) > _thresholdValue ? _highDescriptor : _lowDescriptor;
    }
}
