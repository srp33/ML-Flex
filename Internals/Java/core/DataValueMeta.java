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

package mlflex.core;

import mlflex.summarization.AbstractSummarizer;
import mlflex.summarization.GetFirstSummarizer;
import mlflex.transformation.AbstractTransformer;
import mlflex.transformation.NullTransformer;
import mlflex.helper.ListUtilities;

import java.util.*;

/** This class stores meta information about a single data point.
 * @author Stephen Piccolo
 */
public class DataValueMeta
{
    public String Name;
    public AbstractSummarizer Summarizer;
    public AbstractTransformer Transformer;
    public ArrayList<String> NullValues;

    /** Constructor. By default, the first value is used (when multiple values have been specified), and no transformation occurs.
     *
     * @param name Data point name
     */
    public DataValueMeta(String name)
    {
        this(name, new GetFirstSummarizer(), new NullTransformer());
    }

    /** Constructor.
     *
     * @param name Data point name
     * @param summarizer Data summarizer (how to handle when multiple values have been specified)
     * @param transformer Data transformer
     * @param nullValues Which values are considered to be null/missing
     */
    public DataValueMeta(String name, AbstractSummarizer summarizer, AbstractTransformer transformer, String... nullValues)
    {
        Name = name;
        Summarizer = summarizer;
        Transformer = transformer;
        NullValues = ListUtilities.CreateStringList(nullValues);
    }

    /** Indicates whether a given data value should be considered null or missing.
     *
     * @param value Value to be tested
     * @return Whether it should be considered null or missing
     */
    public boolean IsNullValue(String value)
    {
        ArrayList<String> nullValues = ListUtilities.CreateStringList("null");
        nullValues.addAll(NullValues);

        return nullValues.contains(value);
    }

    @Override
    public boolean equals(Object compareObj)
    {
        if (compareObj == null)
            return false;

        DataValueMeta compare = (DataValueMeta) compareObj;

        return compare.Name.equals(this.Name);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + (this.Name != null ? this.Name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return Name;
    }
}
