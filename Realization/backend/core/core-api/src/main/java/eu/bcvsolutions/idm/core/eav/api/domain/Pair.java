package eu.bcvsolutions.idm.core.eav.api.domain;

/**
 * Pair implementation. Good if you want to return two variables at once.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
public class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {

	private static final long serialVersionUID = 1L;

	public Pair(F f, S s) {
		super(f, s);
	}

	public F getFirst() {
		return getKey();
	}

	public S getSecond() {
		return getValue();
	}

	public String toString() {
		return "[" + getKey() + "," + getValue() + "]";
	}
}
