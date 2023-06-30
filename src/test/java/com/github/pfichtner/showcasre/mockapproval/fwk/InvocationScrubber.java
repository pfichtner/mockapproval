package com.github.pfichtner.showcasre.mockapproval.fwk;

import java.lang.reflect.Method;
import java.util.List;

public interface InvocationScrubber {

	public static class ScrubParameter {

		final Class<?> mockedClass;
		final Method mockedMethod;
		final List<Object> arguments;

		public ScrubParameter(Class<?> mockedClass, Method mockedMethod, List<Object> arguments) {
			this.mockedClass = mockedClass;
			this.mockedMethod = mockedMethod;
			this.arguments = arguments;
		}
	}

	InvocationScrubber.ScrubParameter scrub(InvocationScrubber.ScrubParameter scrubParameter);

	default InvocationScrubber then(InvocationScrubber other) {
		return p -> other.scrub(scrub(p));
	}

}