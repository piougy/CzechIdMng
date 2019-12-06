package eu.bcvsolutions.idm.core.api.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import eu.bcvsolutions.idm.core.api.domain.Codeable;

/**
 * Compare {@link Codeable} by {@code code} attribute.  
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
public class CodeableComparator implements Comparator<Codeable>, Serializable {
	
	private static final long serialVersionUID = 1L;
	//
	private final boolean asc;
	
	public CodeableComparator() {
		asc = true;
	}
	
	public CodeableComparator(boolean asc) {
		this.asc = asc;
	}

	@Override
	public int compare(Codeable one, Codeable two) {
		int result = StringUtils.compareIgnoreCase(one.getCode(), two.getCode());
		//
		return asc ? result : -result;
	}

}
