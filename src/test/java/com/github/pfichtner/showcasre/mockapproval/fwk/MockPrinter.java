package com.github.pfichtner.showcasre.mockapproval.fwk;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.mockito.Mockito.mockingDetails;

import org.mockito.invocation.Invocation;

import com.github.pfichtner.showcasre.mockapproval.fwk.InvocationScrubber.ScrubParameter;

public final class MockPrinter {

	private MockPrinter() {
		super();
	}

	public static String printInvocations(Object mock, InvocationScrubber... scrubbers) {
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
