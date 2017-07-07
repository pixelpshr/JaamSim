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

import com.jaamsim.datatypes.TSRList;
import com.jaamsim.input.Output;
import com.jaamsim.input.TimestampedFileInput;
import com.jaamsim.units.DimensionlessUnit;

public class FileToTSRList extends FileToArray {

	TSRList value;

	public FileToTSRList() {}

	@Override
	protected void setValueForURI(URI uri) {
		value = get1DArrayForURI(uri);
	}

	@Override
	protected void clearValue() {
		if (value != null)
			value.clear();
	}

	private static TSRList get1DArrayForURI(URI uri) {
		TSRList tsrList = TimestampedFileInput.getTimestampedRecordsFromURI(uri);

		return tsrList;
	}

	@Output(name = "Value",
	 description = "A TSRList containing the Time Stamped Record data from the input file.",
	    unitType = DimensionlessUnit.class,
	    sequence = 1)
	public TSRList getValue(double simTime) {
		return value;
	}

}
