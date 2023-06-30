package com.github.pfichtner.showcasre.mockapproval.fwk;

import static java.util.function.Predicate.isEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterInvocationScrubber implements InvocationScrubber {

	private static class ReplaceArgumentAction {

		private final int index;
		private final Class<?> argType;
		private final Function<Object, Object> action;

		@SuppressWarnings("unchecked")
		public <T> ReplaceArgumentAction(int index, Class<T> argType, Function<T, T> action) {
			this.index = index;
			this.argType = argType;
			this.action = (Function<Object, Object>) action;
		}

	}

	private final List<FilterInvocationScrubber.ReplaceArgumentAction> actions = new ArrayList<>();
	private final Class<?> mockClass;
	private Predicate<String> methodFilter = __ -> true;

	public FilterInvocationScrubber(Class<?> mockClass) {
		this.mockClass = mockClass;
	}

	@Override
	public ScrubParameter scrub(ScrubParameter scrubParameter) {
		if (mockClass.isAssignableFrom(scrubParameter.mockedClass)
				&& methodFilter.test(scrubParameter.mockedMethod.getName())) {
			List<Object> clonedArgs = new ArrayList<>(scrubParameter.arguments);
			for (FilterInvocationScrubber.ReplaceArgumentAction action : actions) {
				if (scrubParameter.arguments.size() > action.index
						&& action.argType.isInstance(scrubParameter.arguments.get(action.index))) {
					clonedArgs.set(action.index, action.action.apply(clonedArgs.get(action.index)));
				}

			}
			return new ScrubParameter(scrubParameter.mockedClass, scrubParameter.mockedMethod, clonedArgs);
		}
		return scrubParameter;
	}

	public static FilterInvocationScrubber mockClassScrubber(Class<?> mockClass) {
		return new FilterInvocationScrubber(mockClass);
	}

	public FilterInvocationScrubber forMethodName(String methodName) {
		this.methodFilter = isEqual(methodName);
		return this;
	}

	public <T> FilterInvocationScrubber replaceArg(int index, Class<T> argType, Function<T, T> action) {
		actions.add(new ReplaceArgumentAction(index, argType, action));
		return this;
	}

}