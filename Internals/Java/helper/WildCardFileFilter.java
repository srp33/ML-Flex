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

package mlflex.helper;

import java.io.*;
import java.util.regex.*;

/** This class is used when it is necessary to search the file system using wildcard file names.
 * @author Stephen Piccolo
 */
public class WildCardFileFilter implements FileFilter
{
    private String _pattern;

     /** Basic constructor
     *
     * @param pattern File name pattern (can include wildcard (*) characters)
     */
    public WildCardFileFilter(String pattern)
    {
        _pattern = pattern.replace("*", ".*").replace("?", ".");
    }

    public boolean accept(File file)
    {
        return Pattern.compile(_pattern).matcher(file.getName()).find();
    }
}
