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

/** This is a wrapper class. It stores a prediction that has been made, along with information (the model) that describes how the prediction was made.
 * @author Stephen Piccolo
 */
public class ModelPrediction
{
    /** A description of how the prediction was made. */
    public String Model;
    /** The prediction that was made. */
    public Prediction Prediction;

    /** Constructor
     *
     * @param model A description of how the prediction was made
     * @param prediction The prediction that was made
     */
    public ModelPrediction(String model, Prediction prediction)
    {
        Model = model;
        Prediction = prediction;
    }
}
