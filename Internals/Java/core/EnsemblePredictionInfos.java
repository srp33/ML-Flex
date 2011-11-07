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

import java.util.ArrayList;

/** This class stores a list of CombinedPredictionInfo objects that can be used for making ensemblelearners/combined predictions.
 * @author Stephen Piccolo
 */
public class EnsemblePredictionInfos
{
    /** A list of objects from which information in this object is pulled. */
    public ArrayList<EnsemblePredictionInfo> Infos = new ArrayList<EnsemblePredictionInfo>();

    /** Adds a combined prediction information object to this collection.
     *
     * @param info Object to add
     * @return The current instance for convenience
     */
    public EnsemblePredictionInfos Add(EnsemblePredictionInfo info)
    {
        Infos.add(info);
        return this;
    }

    /** Gets all outer predictions.
     *
     * @return Outer predictions
     * @throws Exception
     */
    public Predictions GetOuterPredictions() throws Exception
    {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        for (EnsemblePredictionInfo x : Infos)
            predictions.add(x.OuterPrediction);

        return new Predictions(predictions);
    }
}
