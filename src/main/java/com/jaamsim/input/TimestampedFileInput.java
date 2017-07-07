/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2011 Ausenco Engineering Canada Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.format.DateTimeParseException;

import com.jaamsim.datatypes.TSR;
import com.jaamsim.datatypes.TSRList;


/**
 * The Class TimestampedFileInput.<br>
 *
 * Extends the FileInput Class to support a particular type of data file.
 * Each line of the input file must be of the form:<br>
 * <BLOCKQUOTE>yyyy-MM-ddTHH:mm:ss.SSS followed by free text</BLOCKQUOTE><br>
 *
 * See the <A HREF="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns">DateFormatter</A> documentation for an explanation of the timestamp elements.
 */
public class TimestampedFileInput extends FileInput {

	/**
	 * Instantiates a new timestamped file input.
	 *
	 * @param key the keyword
	 * @param cat the category
	 * @param def the URI definition
	 */
	public TimestampedFileInput(String key, String cat, URI def) {
		super(key, cat, def);
	}

	/**
	 * Read from the provided URI and return a list of Pairs of timestamp and string.
	 *
	 * @param uri The filename to be read. Must be of the form "yyyy-MM-ddTHH:mm:ss.SSS followed by free text"
	 * @return list of timestamped records
	 */
	public static TSRList getTimestampedRecordsFromURI(URI uri){

		TSRList rec = new TSRList();

		BufferedReader b = null;
		try {
			InputStream r = uri.toURL().openStream();
			b = new BufferedReader(new InputStreamReader(r));

			while (true) {
				String line = null;
				line = b.readLine();

				if (line == null)
					break;

				// extract the timestamp from the record
				int spaceIndex = line.indexOf(' ');
				String dtString = line.substring(0, spaceIndex);

				// then get the rest of the record
				String recString = line.substring(spaceIndex);

				// create a TSR
				TSR tsr = new TSR(dtString, recString);

				// add the new Pair to the list of records
				rec.add(tsr);
			}
			b.close();
			return rec;
		}
		catch (IllegalArgumentException e) {}
		catch (MalformedURLException e) {}
		catch (IOException e) {
			try {
				if (b != null) b.close();
			}
			catch (IOException e2) {}
		}
		catch (DateTimeParseException e) {}

		return null;

	}

}
