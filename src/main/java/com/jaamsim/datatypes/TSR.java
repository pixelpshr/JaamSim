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
package com.jaamsim.datatypes;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javafx.util.Pair;

/**
 * A Timestamped record, composed of a ZonedTimeStamp and a String.
 */
public class TSR implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4527612850612466922L;

	/** The Pair holding the ZonedDateTime and Record string */
	private Pair<ZonedDateTime, String> pair = null;

	/**
	 * Instantiates a new timestamped record.
	 */
	public TSR() {}

	/**
	 * Instantiates a new timestamped record.
	 *
	 * @param zdt the ZonedDateTime of the record
	 * @param record the record
	 */
	public TSR(ZonedDateTime zdt, String record) {

		// ensure that we store the UTC datetime
		ZoneId utcZoneId = ZoneId.of("Z");
		ZonedDateTime zdtUTC = zdt.withZoneSameInstant(utcZoneId);

		// create a Pair
		this.pair = new Pair<>(zdtUTC, record);

	}

	/**
	 * Instantiates a new timestamped record.
	 *
	 * @param zdtString the string representing the ZonedDateTime. In <A HREF="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_OFFSET_DATE_TIME">ISO_OFFSET_DATE_TIME</A> format.
	 * @param record the record
	 */
	public TSR(String zdtString, String record) {
		// ensure that we store the UTC datetime
		ZoneId utcZoneId = ZoneId.of("Z");
		DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		ZonedDateTime zdtLocal = ZonedDateTime.parse(zdtString, dtFormatter);
		ZonedDateTime zdtUTC = zdtLocal.withZoneSameInstant(utcZoneId);

		// create a Pair
		this.pair = new Pair<>(zdtUTC, record);
	}

	/**
	 * Gets the ZonedDateTime of the record.
	 *
	 * @return the ZonedDateTime
	 */
	public ZonedDateTime getZDT() {
		return pair.getKey();
	}

	/**
	 * Gets the ZonedDateTime of the record.
	 *
	 * @return the ZonedDateTime as a String
	 */
	public String getZDTString() {
		return pair.getKey().toString();
	}

	/**
	 * Gets the record.
	 *
	 * @return the record
	 */
	public String getRecord() {
		return pair.getValue();
	}

	public boolean equals(TSR testValue) {
		if (this.getZDT() == testValue.getZDT()
			&& this.getRecord().equals(testValue.getRecord()))
			return true;
		else
			return false;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pair.toString();
	}
}

class TSRComparator implements Comparator<TSR> {

	@Override
	public int compare(TSR o1, TSR o2) {
		if (o1.getZDT().isBefore(o2.getZDT())) {
			return 1;
		} else {
			return -1;
		}
	}

}