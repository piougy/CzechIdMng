package eu.bcvsolutions.idm.core.model.domain;

/**
 * Pair implementation. Good if you want to return two variables at once.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
public class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {

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
