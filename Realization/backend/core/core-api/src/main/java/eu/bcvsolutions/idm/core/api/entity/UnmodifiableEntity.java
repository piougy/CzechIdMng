package eu.bcvsolutions.idm.core.api.entity;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;

/**
 * Entity that implements this interface must have attribute (column) - Unmodifiable
 * Column is type of boolean with default value true.
 * Entity that has this flag on true, cant be deleted from rest, from service is remove possible.
 * 
 * Good place for check entity is method validateEntity on {@link AbstractReadWriteDtoController}
 * 
 * This interface is only for information, is not bound by any logic.
 * TODO: add some logic into abstract...
 * 
 * Example of attribute:
<code><br><br>
	&#64;NotNull<br>
	&#64;Column(name = "unmodifiable", nullable = false)<br>
	&#64;private boolean unmodifiable = false;<br>
</code>
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface UnmodifiableEntity {
	
	boolean isUnmodifiable();
	
	void setUnmodifiable(boolean unmodifiable);
}
