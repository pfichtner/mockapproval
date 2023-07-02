package com.github.pfichtner.showcasre.mockapproval;

import static com.github.pfichtner.showcasre.mockapproval.fwk.FilterInvocationScrubber.mockClassScrubber;
import static com.github.pfichtner.showcasre.mockapproval.fwk.MockPrinter.printInvocations;
import static com.github.pfichtner.showcasre.mockapproval.util.Maps.removeKeys;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static org.approvaltests.Approvals.verify;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.pfichtner.showcasre.mockapproval.fwk.InvocationScrubber;

class TestMockitoApprovals {

	interface HttpClient {
		void patch(String resource, Map<Object, Object> data);
	}

	@Test
	void verifyWithoutScrubbing() {
		HttpClient httpClient = mock(HttpClient.class);
		someBusinessCodeThatInteractsWith1(httpClient);
		approve(httpClient);
	}

	private void someBusinessCodeThatInteractsWith1(HttpClient httpClient) {
		httpClient.patch("/foo/21", emptyMap());
		httpClient.patch("/foo/bar/42", Map.of("someArg", 42));
	}

	@Test
	void verifyWithScrubbing() {
		HttpClient httpClient = mock(HttpClient.class);
		someBusinessCodeThatInteractsWith2(httpClient);
		approve(httpClient, arg2Mapper().then(patchTimestampScrubber()));
	}

	private void someBusinessCodeThatInteractsWith2(HttpClient httpClient) {
		httpClient.patch("/abc", emptyMap());
		httpClient.patch("/abc", null);
		httpClient.patch("/abc", mapWithNullValues());
		httpClient.patch("/x/y/z", Map.of("arg1", 84, "nestedMap", mapWithNullValues(), "timestamp",
				currentTimeMillis(), "uuid", randomUUID()));
	}

	private Map<Object, Object> mapWithNullValues() {
		Map<Object, Object> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", null);
		map.put("z", 26);
		return map;
	}

	private InvocationScrubber patchTimestampScrubber() {
		return mockClassScrubber(HttpClient.class).forMethodName("patch").replaceArg(1, Map.class,
				map -> removeKeys((Map<?, ?>) map, "timestamp", "uuid"));
	}

	/**
	 * Just to verify index works as expected
	 */
	private InvocationScrubber arg2Mapper() {
		return mockClassScrubber(HttpClient.class).forMethodName("patch").replaceArg(2, Object.class, map -> {
			throw new IllegalStateException("Should not have been called");
		});
	}

	private static void approve(Object mock, InvocationScrubber... scrubbers) {
		verify(printInvocations(mock, scrubbers));
	}

}
