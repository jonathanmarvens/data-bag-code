/**
 *  Copyright 2010-2013 Konstantin Livitski
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the Data-bag Project License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Data-bag Project License for more details.
 *
 *  You should find a copy of the Data-bag Project License in the
 *  `data-bag.md` file in the `LICENSE` directory
 *  of this package or repository.  If not, see
 *  <http://www.livitski.name/projects/data-bag/license>. If you have any
 *  questions or concerns, contact the project's maintainers at
 *  <http://www.livitski.name/contact>. 
 */
    
package name.livitski.databag.diff;

import java.io.IOException;

/**
 * Writer for common deltas.
 */
public class CommonDeltaWriter extends DeltaWriter
{
 // NOTE: all methods that write data must start with header() call
 // NOTE: all methods that add a fragment end with fragmentAdded() call
 // NOTE: all methods that write data must update the size using written()
 /**
  * Creates an instance for writing into a {@link ByteSink}.
  * @param out the sink that receives encoded form of this delta 
  */
 public CommonDeltaWriter(ByteSink out)
 {
  super(out, Type.COMMON);
 }

 public void writeFragment(long reverseOffset, long forwardOffset, long length)
 	throws IOException
 {
  header();
  PositiveLongContainer value = new PositiveLongContainer(reverseOffset);
  value.encode(out);
  written(value.getEncodedSize());
  value = new PositiveLongContainer(forwardOffset);
  value.encode(out);
  written(value.getEncodedSize());
  value = new PositiveLongContainer(length);
  value.encode(out);
  written(value.getEncodedSize());
  fragmentAdded();
 }
}
