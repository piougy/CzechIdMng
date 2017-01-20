package eu.bcvsolutions.idm.ic.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.bcvsolutions.idm.ic.filter.api.IcFilter;

/**
 * Useful for the AND, OR, XOR, etc..
 */
public abstract class IcCompositeFilter implements IcFilter {

    /**
     * Left side of a composite based IcFilter.
     */
    private IcFilter left;

    /**
     * Right side of a composite based IcFilter.
     */
    private IcFilter right;

    /**
     * Create a composite IcFilter w/ the left and right IcFilters provided.
     *
     * @param left
     *            the left side of the composite.
     * @param right
     *            the right side of the composite.
     */
    IcCompositeFilter(IcFilter left, IcFilter right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return the left side of the composite.
     */
    public IcFilter getLeft() {
        return left;
    }

    /**
     * @return the right side of the composite.
     */
    public IcFilter getRight() {
        return right;
    }

    public Collection<IcFilter> getFilters() {
        return Collections.unmodifiableList(newList(getLeft(), getRight()));
    }
    
    /**
     * Create a modifiable list from the arguments. The return value is backed
     * by an {@link ArrayList}.
     */
    @SafeVarargs
	public static <T> List<T> newList(T... arr) {
        List<T> ret = new ArrayList<T>();
        if (arr != null && arr.length != 0) {
            for (T t : arr) {
                ret.add(t);
            }
        }
        return ret;
    }
}
