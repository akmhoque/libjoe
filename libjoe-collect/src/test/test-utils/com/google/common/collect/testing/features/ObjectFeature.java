package com.google.common.collect.testing.features;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import com.google.common.collect.testing.Helpers;

@SuppressWarnings("unchecked")
public enum ObjectFeature implements Feature<Object> {
	SERIALIZABLE, ;

	private final Set<Feature<? super Object>> implied;

	ObjectFeature(Feature<Object> ... implied) {
		this.implied = (Set<Feature<? super Object>>) (Set<?>) Helpers.copyToSet(implied);
	}

	@Override
	public Set<Feature<? super Object>> getImpliedFeatures() {
		return implied;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@TesterAnnotation
	public @interface Require {
		public abstract ObjectFeature[] value() default {};
		public abstract ObjectFeature[] absent() default {};
	}
}
