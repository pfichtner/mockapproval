package com.github.pfichtner.showcasre.mockapproval.util;

import static java.util.Arrays.asList;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {

	private Maps() {
		super();
	}

	public static <K, V> Map<K, V> removeKeys(Map<K, V> map, String... keys) {
		Map<K, V> clone = new LinkedHashMap<>(map);
		clone.keySet().removeAll(asList(keys));
		return clone;
	}

}
