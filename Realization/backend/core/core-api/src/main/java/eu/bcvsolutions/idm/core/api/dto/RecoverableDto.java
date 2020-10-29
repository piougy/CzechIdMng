package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Codeable;

/**
 * Recoverable dto - codeable base dto can be recovered.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public interface RecoverableDto extends BaseDto, Codeable {

	@Override
	default Serializable getId() {
		return getCode();
	}
	
	@Override
	default void setId(Serializable id) {
		// nothing by default
	}
}
