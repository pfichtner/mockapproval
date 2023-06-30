package com.github.pfichtner.showcasre.mockapproval;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.approvaltests.Approvals.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;

import com.github.pfichtner.showcasre.mockapproval.TestMockitoApprovals.HttpClient;
import com.github.pfichtner.showcasre.mockapproval.TestMockitoApprovals.InvocationScrubber.ScrubParameter;

class TestMockitoApprovals {

	public static interface InvocationScrubber {

		public static class ScrubParameter {

			private final Class<?> mockedClass;
			private final Method mockedMethod;
			private final List<Object> arguments;

			public ScrubParameter(Class<?> mockedClass, Method mockedMethod, List<Object> arguments) {
				this.mockedClass = mockedClass;
				this.mockedMethod = mockedMethod;
				this.arguments = arguments;
			}
		}

		ScrubParameter scrub(ScrubParameter scrubParameter);

		default InvocationScrubber then(InvocationScrubber second) {
			InvocationScrubber outer = this;
			return p -> second.scrub(outer.scrub(p));
		}
	}

	interface HttpClient {
		void patch(String resource, Map<Object, Object> data);
	}

	@Test
	void verifyWithoutScrubbing() {
		HttpClient httpClient = mock(HttpClient.class);
		httpClient.patch("/foo/21", emptyMap());
		httpClient.patch("/foo/bar/42", emptyMap());
		approve(httpClient);
	}

	@Test
	void verifyWithScrubbing() {
		HttpClient httpClient = mock(HttpClient.class);
		httpClient.patch("/foo/21", Map.of("someArg", 42, "timestamp", currentTimeMillis()));
		httpClient.patch("/foo/bar/42", Map.of("someOtherArg", 84, "timestamp", currentTimeMillis(), "uuid", randomUUID()));
		approve(httpClient, noopScrubber().then(patchTimestampScrubber()));
	}

	private InvocationScrubber patchTimestampScrubber() {
		return p -> {
			if (p.mockedClass == HttpClient.class && p.mockedMethod.getName().equals("patch") && p.arguments.size() >= 2
					&& p.arguments.get(1) instanceof Map) {
				List<Object> newArguments = new ArrayList<>(p.arguments);
				newArguments.set(1,
						((Map<?, ?>) p.arguments.get(1)).entrySet().stream()
								.filter(e -> !asList("timestamp", "uuid").contains((Object) e.getKey()))
								.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
				return new ScrubParameter(p.mockedClass, p.mockedMethod, newArguments);
			}
			return p;
		};
	}

	/**
	 * Just to verify {@link InvocationScrubber#then(InvocationScrubber)}
	 */
	private InvocationScrubber noopScrubber() {
		return p -> p;
	}

	private static void approve(Object mock, InvocationScrubber... scrubbers) {
		verify(approvalString(mock, scrubbers));
	}

	private static String approvalString(Object mock, InvocationScrubber... scrubbers) {
		StringBuilder sb = new StringBuilder();
		for (Invocation invocation : mockingDetails(mock).getInvocations()) {
			ScrubParameter scrubParameter = scrub(invocation, scrubbers);
			sb = sb.append(String.format("%s#%s(%s)\n", scrubParameter.mockedClass.getName(),
					scrubParameter.mockedMethod.getName(),
					scrubParameter.arguments.stream().map(String::valueOf).collect(joining(","))));

		}
		return sb.toString();
	}

	private static ScrubParameter scrub(Invocation invocation, InvocationScrubber... scrubbers) {
		ScrubParameter scrubParameter = new ScrubParameter(invocation.getMethod().getDeclaringClass(),
				invocation.getMethod(), asList(invocation.getArguments()));
		for (InvocationScrubber scrubber : scrubbers) {
			scrubParameter = scrubber.scrub(scrubParameter);
		}
		return scrubParameter;
	}

}
