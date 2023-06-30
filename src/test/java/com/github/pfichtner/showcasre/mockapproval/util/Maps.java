package com.github.pfichtner.showcasre.mockapproval.util;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

public final class Maps {

	private Maps() {
		super();
	}

	public static <K, V> Map<K, V> removeKeys(Map<K, V> map, String... keys) {
		return map.entrySet().stream().filter(e -> !asList(keys).contains(e.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
