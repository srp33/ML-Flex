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

package mlflex.ensemblelearners;

import mlflex.core.EnsemblePredictionInfo;
import mlflex.core.EnsemblePredictionInfos;
import mlflex.core.ModelPrediction;

/** This class represents an ensemblelearners/combiner approach that computes the combined prediction according to the performance attained in the inner cross-validation folds. The single ModelSelector that attained the best performance in the inner folds is used as the outer-fold combiner prediction.
 * @author Stephen Piccolo
 */
public class SelectBestEnsembleLearner extends AbstractEnsembleLearner
{
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        EnsemblePredictionInfo best = SelectBestPrediction(combinedPredictionInfos);

        return new ModelPrediction(best.Description, best.OuterPrediction);
    }

    private EnsemblePredictionInfo SelectBestPrediction(EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        EnsemblePredictionInfo best = null;

        for (EnsemblePredictionInfo info : combinedPredictionInfos.Infos)
        {
            if (best == null || info.GetWeight() > best.GetWeight())
                best = info;
        }

        return best;
    }
}