/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2002-2011 Ausenco Engineering Canada Inc.
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

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class stores Timestamped Record values in an array.
 */
public class TSRList {
	private ArrayList<TSR> storage;

	/**
	 * Construct an empty vector.
	 */
	public TSRList() {
		storage = new ArrayList<>();
	}

	/**
	 * Construct a new copy of a TSRVector
	 *
	 * @param original
	 */
	public TSRList(TSRList original) {
		storage.addAll(original.storage);
	}

	/**
	 * Remove all of the TSRs from this vector.
	 */
	public void clear() {
		storage.clear();
	}

	/**
	 * Return the TSR at the given position in this vector.
	 */
	public TSR get(int index) {
		if (index < 0 || index >= storage.size())
			throw new ArrayIndexOutOfBoundsException("Invalid index:" + index);
		else
			return storage.get(index);
	}

	/**
	 * Return the last TSR in this vector.
	 */
	public TSR lastElement() {
		return get(size() - 1);
	}

	/**
	 * Append the specified TimeStamped Record to the end of the vector.
	 */
	public void add(TSR value) {
		storage.add(value);
	}

	/**
	 * Replaces the element at the specified position in this TSRVector with the
	 * specified element. Returns the element that was replaced in the
	 * TSRVector.
	 *
	 * @param index
	 *            index of element to replace
	 * @param value
	 *            element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @exception ArrayIndexOutOfBoundsException
	 *                index out of range (index < 0 || index >= size())
	 */
	public TSR set(int index, TSR value) {
		if (index < 0 || index >= storage.size()) {
			throw new ArrayIndexOutOfBoundsException("Invalid index:" + index);
		}

		return storage.set(index, value);
	}

	/**
	 * Removes the element at the specified position in this TSRVector. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).
	 * Returns the element that was removed from the TSRVector.
	 *
	 * @param index
	 *            the index of the element to removed
	 * @return element that was removed
	 */
	public TSR remove(int index) {
		if (index < 0 || index >= storage.size()) {
			throw new ArrayIndexOutOfBoundsException("Invalid index:" + index);
		}

		return storage.remove(index);
	}

	/**
	 * Copy the specified vector to this vector.
	 */
	public void set(TSRList original) {
		storage.clear();
		storage.addAll(original.storage);
	}

	/**
	 * Fill the vector with the given number of entries of the given value.
	 */
	public void fillWithEntriesOf(int entries, TSR value) {
		for (int i=0;i<entries;i++) {
			add(value);
		}
	}

	/**
	 * Return the number of TSRs in this vector.
	 */
	public int size() {
		return storage.size();
	}

	/**
	 * Return a string representation of this vector, containing the String
	 * representation of each element.
	 */
	@Override
	public String toString() {

		StringBuilder out = new StringBuilder();

		if (storage.size() == 0) {
			out.append("{ }");
		} else {
			out.append("{ ");
			for (TSR x : storage) {
				out.append("{ ");
				out.append(x.toString());
				out.append(" },");
			}
			out.setLength(out.length()-1); // remove the last comma
			out.append(" }");
		}

		return out.toString();
	}

	/**
	 * get the earliest element in the TSRList, as ordered by the timestamp
	 *
	 * @return the earliest TSR
	 */
	public TSR getMin() {
		if (size() < 1) {
			return null;
		} else {
			TSR testValue = get(0);
			for (TSR x : storage) {
				testValue = testValue.getZDT().compareTo(x.getZDT()) <= 0 ? testValue : x;
			}
			return testValue;
		}
	}

	/**
	 * get the latest element in the TSRList, as ordered by the timestamp
	 *
	 * @return the latest TSR
	 */
	public TSR getMax() {
		if (size() < 1) {
			return null;
		} else {
			TSR testValue = get(0);
			for (TSR x : storage) {
				testValue = testValue.getZDT().compareTo(x.getZDT()) >= 0 ? testValue : x;
			}
			return testValue;
		}
	}

	public int indexOf(TSR testValue) {
		return storage.indexOf(testValue);
	}

	/**
	 * force the list to be ordered by timestamp
	 */
	public void sort() {
		Collections.sort(storage, new TSRComparator());
	}

	/**
	 * Reverse the elements position n to 0 and 0 to n
	 *
	 * @return
	 */
	public void reverse() {
		int numSwaps = this.size() / 2;
		int swapIndex = this.size() - 1;

		for (int i = 0; i < numSwaps; i++, swapIndex--) {
			TSR temp = this.get(swapIndex);
			this.set(swapIndex, this.get(i));
			this.set(i, temp);
		}
	}

}
