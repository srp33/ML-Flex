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

/** This is a wrapper class. It stores predictions that have been made, along with information (the model) that describes how the predictions were made.
 * @author Stephen Piccolo
 */
public class ModelPredictions
{
    /** A description of how the prediction was made. */
    public String Model;
    /** The predictions that were made. */
    public Predictions Predictions;

    /** Constructor
     *
     * @param model A description of how the prediction was made
     * @param predictions The predictions that were made
     */
    public ModelPredictions(String model, Predictions predictions)
    {
        Model = model;
        Predictions = predictions;
    }
}
