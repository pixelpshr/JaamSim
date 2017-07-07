/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2017 JaamSim Software Inc.
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
package com.jaamsim.BasicObjects;

import java.net.URI;
import java.util.ArrayList;

import com.jaamsim.input.FileInput;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

public class FileToStringVector extends FileToArray {

	String[] value;

	public FileToStringVector() {}

	@Override
	protected void setValueForURI(URI uri) {
		value = get1DArrayForURI(uri);
	}

	@Override
	protected void clearValue() {
		value = null;
	}

	private static String[] get1DArrayForURI(URI uri) {
		ArrayList<ArrayList<String>> tokens = FileInput.getTokensFromURI(uri);
		int n = 0;
		for (ArrayList<String> record : tokens) {
			n += record.size();
		}
		String[] ret = new String[n];
		int i = 0;
		for (ArrayList<String> record : tokens) {
			for (String str : record) {
				try {
					ret[i] = str;
				}
				catch (IndexOutOfBoundsException e) {
					ret[i] = "";
				}
				i++;
			}
		}
		return ret;
	}

	@Output(name = "Value",
	 description = "A vector containing the String data from the input file.",
	    unitType = DimensionlessUnit.class,
	    sequence = 1)
	public String[] getValue(double simTime) {
		return value;
	}

}
