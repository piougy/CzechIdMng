package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;

/**
 * Filter for filtering entities created till given time stamp  (auditable.created <= createdTill).
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface CreatedTillFilter extends BaseDataFilter {

	String PARAMETER_CREATED_TILL = "createdTill"; 

	/**
	 * Get created till for filtering entities created till this time stamp (auditable.created <= createdTill).
	 *
	 * @return
	 */
	default ZonedDateTime getCreatedTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_CREATED_TILL);
	}

	/**
	 * Set created till for filtering entities created till this time stamp (auditable.created <= createdTill).
	 *
	 * @param createdTill
	 */
	default void setCreatedTill(ZonedDateTime createdTill) {
		set(PARAMETER_CREATED_TILL, createdTill);
	}
}
