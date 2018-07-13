package eu.bcvsolutions.idm.core.api.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import eu.bcvsolutions.idm.core.api.domain.Auditable;

/**
 * Compare {@link Auditable} by {@code created} attribute.  
 * 
 * @author Radek Tomiška
 *
 */
public class CreatedComparator implements Comparator<Auditable>, Serializable {
	
	private static final long serialVersionUID = 1L;
	//
	private final boolean asc;
	
	public CreatedComparator() {
		asc = true;
	}
	
	public CreatedComparator(boolean asc) {
		this.asc = asc;
	}

	@Override
	public int compare(Auditable o1, Auditable o2) {
		int result = o1.getCreated().compareTo(o2.getCreated());
		//
		return asc ? result : -result;
	}

}
