package com.google.common.collect.testing.features;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.features.Feature;
import com.google.common.collect.testing.features.TesterAnnotation;

public enum MultimapFeature implements Feature<Multimap<?, ?>> {
	ALLOWS_DUPLICATE_VALUES, ;

	private final Set<Feature<? super Multimap<?, ?>>> implied;

	MultimapFeature(Feature<? super Multimap<?, ?>> ... implied) {
		this.implied = Helpers.copyToSet(implied);
	}

	@Override
	public Set<Feature<? super Multimap<?, ?>>> getImpliedFeatures() {
		return implied;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@TesterAnnotation
	public @interface Require {
		public abstract MultimapFeature[] value() default {};
		public abstract MultimapFeature[] absent() default {};
	}
}
