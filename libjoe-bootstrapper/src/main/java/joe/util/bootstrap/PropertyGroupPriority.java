package joe.util.bootstrap;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.collect.Ordering;

/**
 * Enumeration of property groups in their priority order.
 * 
 * @author Joe Kearney
 */
public enum PropertyGroupPriority {
	/** Properties set from code invoking the bootstrapper */
	CODE,
	/** System properties */
	SYSTEM,
	/** User-specific properties */
	USER,
	/** Machine-specific properties */
	MACHINE,
	/** Operating system-specific properties */
	OS,
	/** Properties specific to development environments */
	IDE,
	/** Properties specific to other specific environments */
	ADDITIONAL,
	/** Properties specific to other production environments (e.g. prod, dev, test) */
	ENVIRONMENT,
	/** Default properties for the application */
	COMMON,
	/** Properties set from code invoking the bootstrapper that may be overridden by any other property priority */
	OVERRIDEABLE_CODE, ;
	
	/**
	 * List of priorities in ascending order.
	 */
	public static final List<PropertyGroupPriority> ascendingPriority = Ordering.natural().reverse().immutableSortedCopy(
			asList(PropertyGroupPriority.values()));
}
