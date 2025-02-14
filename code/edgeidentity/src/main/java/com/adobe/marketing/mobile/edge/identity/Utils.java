/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.identity;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.adobe.marketing.mobile.edge.identity.IdentityConstants.LOG_TAG;

class Utils {

	private Utils() {}

	static boolean isNullOrEmpty(final String str) {
		return str == null || str.isEmpty();
	}

	static boolean isNullOrEmpty(final Map<String, Object> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Adds {@code key}/{@code value} to {@code map} if {@code value} is not null or an
	 * empty collection.
	 *
	 * @param map collection to put {@code value} mapped to {@code key} if {@code value} is
	 *            non-null and contains at least one entry
	 * @param key key used to map {@code value} in {@code map}
	 * @param value a Object to add to {@code map} if not null
	 */
	static void putIfNotNull(final Map<String, Object> map, final String key, final Object value) {
		boolean addValues =
			map != null &&
			key != null &&
			value != null;

		if (addValues) {
			map.put(key, value);
		}
	}

	/* JSON - Map conversion helpers */
	// TODO: add tests / replace with third party library for json conversion; test more around jsonObject/jsonArray with null nodes
	// TODO: check what should be the expected behavior with the konductor team (e.g. don't add the null nodes or add them with null values)
	/**
	 * Converts provided {@link JSONObject} into {@link Map} for any number of levels, which can be used as event data
	 * This method is recursive.
	 * The elements for which the conversion fails will be skipped.
	 *
	 * @param jsonObject to be converted
	 * @return {@link Map} containing the elements from the provided json, null if {@code jsonObject} is null
	 */
	static Map<String, Object> toMap(final JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Map<String, Object> jsonAsMap = new HashMap<>();
		Iterator<String> keysIterator = jsonObject.keys();

		while (keysIterator.hasNext()) {
			String nextKey  = keysIterator.next();
			Object value = null;
			Object returnValue;

			try {
				value = jsonObject.get(nextKey);
			} catch (JSONException e) {
				MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
							   "Utils(toMap) - Unable to convert jsonObject to Map for key " + nextKey + ", skipping.");
			}

			if (value == null) {
				continue;
			}

			if (value instanceof JSONObject) {
				returnValue = toMap((JSONObject)value);
			} else if (value instanceof JSONArray) {
				returnValue = toList((JSONArray) value);
			} else {
				returnValue = value;
			}

			jsonAsMap.put(nextKey, returnValue);
		}

		return jsonAsMap;
	}

	/**
	 * Converts provided {@link JSONArray} into {@link List} for any number of levels which can be used as event data
	 * This method is recursive.
	 * The elements for which the conversion fails will be skipped.
	 *
	 * @param jsonArray to be converted
	 * @return {@link List} containing the elements from the provided json, null if {@code jsonArray} is null
	 */
	static List<Object> toList(final JSONArray jsonArray) {
		if (jsonArray == null) {
			return null;
		}

		List<Object> jsonArrayAsList = new ArrayList<>();
		int size = jsonArray.length();

		for (int i = 0; i < size; i++) {
			Object value = null;
			Object returnValue = null;

			try {
				value = jsonArray.get(i);
			} catch (JSONException e) {
				MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
							   "Utils(toList) - Unable to convert jsonObject to List for index " + i + ", skipping.");
			}

			if (value == null) {
				continue;
			}

			if (value instanceof JSONObject) {
				returnValue = toMap((JSONObject)value);
			} else if (value instanceof JSONArray) {
				returnValue = toList((JSONArray) value);
			} else {
				returnValue = value;
			}

			jsonArrayAsList.add(returnValue);
		}

		return jsonArrayAsList;
	}


	/**
	 * Creates a deep copy of the provided {@link Map}.
	 *
	 * @param map to be copied
	 * @return {@link Map} containing a deep copy of all the elements in {@code map}
	 */
	static Map<String, Object> deepCopy(final Map<String, Object> map) {
		if (map == null) {
			return null;
		}

		try {
			return Utils.toMap(new JSONObject(map));
		} catch (NullPointerException e) {
			MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Utils(deepCopy) - Unable to deep copy map, json string invalid.");
		}

		return null;
	}

	/**
	 * Creates a deep copy of the provided {@code listOfMaps}.
	 *
	 * @param listOfMaps to be copied
	 * @return {@link List} containing a deep copy of all the elements in {@code listOfMaps}
	 * @see {@link #deepCopy(Map)}
	 */
	static List<Map<String, Object>> deepCopy(final List<Map<String, Object>> listOfMaps) {
		if (listOfMaps == null) {
			return null;
		}

		List<Map<String, Object>> deepCopy = new ArrayList<>();

		for (Map<String, Object> map : listOfMaps) {
			deepCopy.add(deepCopy(map));
		}

		return deepCopy;
	}

}
