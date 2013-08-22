/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.sandwell.JavaSimulation;

import com.jaamsim.input.Output;
import com.jaamsim.input.UnitTypeInput;
import com.jaamsim.units.Unit;

public class TimeSeries extends Entity {

	@Keyword(description = "A list of time series records with default format { 'yyyy MM dd HH:mm' value units }, where\n" +
					"yyyy is the year\n" +
					"MM is the month (1-12)\n" +
					"dd is the day of the month\n" +
					"HH is the hour of day (0-23)\n" +
					"mm is the minutes (0-59)\n" +
					"value is the time series value for the given date and time\n" +
					"units is the optional units for the value\n" +
					"The date and times must be given in increasing order.",
	         example = "TimeSeries1  Value { { '2010 1 1 0:00' 0.5 m } { '2010 1 1 3:00' 1.5 m } { '2010 1 1 6:00' 1.2 m } }")
	private final TimeSeriesInput value;

	@Keyword(description = "The unit type for the time series (e.g. DistanceUnit, TimeUnit, MassUnit).  " +
			"If the UnitType keyword is specified, it must be specified before the Value keyword.",
     example = "TimeSeries1  UnitType { DistanceUnit }")
	private final UnitTypeInput unitType;

	@Keyword(description = "The format for the date and time (e.g. 'yyyy-MM-dd HH:mm:ss', yyyy/MM/dd).  " +
	                "Put single quotes around the format if it includes spaces.",
     example = "TimeSeries1  DateFormat { 'yyyy-MM-dd HH:mm' }")
	private final StringInput dateFormat;

	private int indexOfTime;  // The index of the time in the last call to getValueForTime()
	private double cycleTime; // The number of hours in the cycle for the time series

	{
		value = new TimeSeriesInput("Value", "Key Inputs", null);
		this.addInput(value, true);

		unitType = new UnitTypeInput( "UnitType", "Optional" );
		this.addInput( unitType, true );

		dateFormat = new StringInput("DateFormat", "Key Inputs", null);
		this.addInput(dateFormat, true);
	}

	public TimeSeries() { }

	@Override
	public void validate() {
		super.validate();

		if( value.getValue() == null || value.getValue().getTimeList().size() == 0 )
			throw new InputErrorException( "Time series Value must be specified" );
	}

	@Override
	public void earlyInit() {
		super.earlyInit();
		indexOfTime = 0;

		// Determine the cycle time as the last time rounded up to the nearest year
		DoubleVector timeList = value.getValue().getTimeList();
		cycleTime = Math.ceil( timeList.get( timeList.size() - 1 ) / 8760.0 ) * 8760.0;
	}

	@Override
	public void updateForInput( Input<?> in ) {
		super.updateForInput( in );

		if ( in == unitType ) {
			if( value.getValue() != null )
				throw new InputErrorException( "UnitType must be specified before Value");

			value.setUnitType( unitType.getUnitType() );
		}
		if ( in == dateFormat ) {
			try {
				value.setDateFormat( dateFormat.getValue() );
			}
			catch ( IllegalArgumentException e ) {
				throw new InputErrorException( "Invalid date format " + dateFormat.getValue() );
			}
		}
	}

	@Output( name="PresentValue",
			 description="The time series value for the present time." )
	public double getPresentValue( double simTime ) {
		return this.getValueForTime( simTime / 3600.0 );
	}

	/**
	 * Return the value for the given simulation time in hours
	 */
	public double getValueForTime( double time ) {
		DoubleVector timeList = value.getValue().getTimeList();
		DoubleVector valueList = value.getValue().getValueList();

		// Determine the time in the cycle for the given time
		int completedCycles = (int)Math.floor( time / cycleTime );
		double timeInCycle = time - ( completedCycles * cycleTime );

		// Perform linear search for time from indexOfTime
		for( int i = indexOfTime; i < timeList.size()-1; i++ ) {
			if( Tester.lessOrEqualCheckTimeStep( timeList.get( i ), timeInCycle )
					&& Tester.lessCheckTimeStep( timeInCycle, timeList.get( i+1 ) ) ) {
				indexOfTime = i;
				return valueList.get( indexOfTime );
			}
		}

		// If the time in the cycle is greater than the last time, return the last value
		if( Tester.greaterOrEqualCheckTimeStep( timeInCycle, timeList.get( timeList.size() - 1 ) ) ) {
			indexOfTime = timeList.size() - 1;
			return valueList.get( indexOfTime );
		}

		// Perform linear search for time from 0
		for( int i = 0; i < indexOfTime; i++ ) {
			if( Tester.lessOrEqualCheckTimeStep( timeList.get( i ), timeInCycle )
					&& Tester.lessCheckTimeStep( timeInCycle, timeList.get( i+1 ) ) ) {
				indexOfTime = i;
				return valueList.get( indexOfTime );
			}
		}

		// No value was found for time, return 0
		return 0.0;
	}

	/**
	 * Return the time in hours from the given start time
	 * until the value is less than or equal to the given limit.
	 */
	public double calcTimeFrom_UntilLessThanOrEqualTo( double time, double limit ) {

		// If the value at the start time is less than or equal to the limit, return 0
		if( getValueForTime( time ) <= limit )
			return 0;

		DoubleVector timeList = value.getValue().getTimeList();
		DoubleVector valueList = value.getValue().getValueList();

		// Determine the time in the cycle for the given time
		int completedCycles = (int)Math.floor( time / cycleTime );
		double timeInCycle = time - ( completedCycles * cycleTime );

		// Assume indexOfTime corresponds to the given start time
		// Perform linear search for time from indexOfTime + 1
		for( int i = indexOfTime + 1; i < timeList.size(); i++ ) {
			if( valueList.get( i ) <= limit ) {
				return timeList.get( i ) - timeInCycle;
			}
		}

		// Perform linear search for time from 0
		for( int i = 0; i < indexOfTime; i++ ) {
			if( valueList.get( i ) <= limit ) {
				return timeList.get( i ) + cycleTime - timeInCycle;
			}
		}

		// The value is never less than or equal to the limit.  Return infinity
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Return the time in hours from the given start time
	 * until the value is greater than the given limit.
	 */
	public double calcTimeFrom_UntilGreaterThan( double time, double limit ) {

		// If the value at the start time greater the limit, return 0
		if( getValueForTime( time ) > limit )
			return 0;

		DoubleVector timeList = value.getValue().getTimeList();
		DoubleVector valueList = value.getValue().getValueList();

		// Determine the time in the cycle for the given time
		int completedCycles = (int)Math.floor( time / cycleTime );
		double timeInCycle = time - ( completedCycles * cycleTime );

		// Assume indexOfTime corresponds to the given start time
		// Perform linear search for time from indexOfTime + 1
		for( int i = indexOfTime + 1; i < timeList.size(); i++ ) {
			if( valueList.get( i ) > limit ) {
				return timeList.get( i ) - timeInCycle;
			}
		}

		// Perform linear search for time from 0
		for( int i = 0; i < indexOfTime; i++ ) {
			if( valueList.get( i ) > limit ) {
				return timeList.get( i ) + cycleTime - timeInCycle;
			}
		}

		// The value is never greater than the limit.  Return infinity
		return Double.POSITIVE_INFINITY;
	}

	public Class<? extends Unit> getUnitType() {
		return unitType.getUnitType();
	}

	public double getMaxValue() {
		return value.getValue().getMaxValue();
	}

	public double getMinValue() {
		return value.getValue().getMinValue();
	}
}
