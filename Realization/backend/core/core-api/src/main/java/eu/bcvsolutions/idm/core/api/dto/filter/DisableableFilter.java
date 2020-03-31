package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.Disableable;

/**
 * Filter for filtering entities created till given time stamp  (auditable.created <= createdTill).
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface DisableableFilter extends BaseDataFilter {

	String PARAMETER_DISABLED = Disableable.PROPERTY_DISABLED; 

	/**
	 * Filter disabled entities.
	 *
	 * @return
	 */
	default Boolean getDisabled() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_DISABLED);
	}

	/**
	 * Filter disabled entities.
	 *
	 * @param createdTill
	 */
	default void setDisabled(Boolean disabled) {
		set(PARAMETER_DISABLED, disabled);
	}
}
