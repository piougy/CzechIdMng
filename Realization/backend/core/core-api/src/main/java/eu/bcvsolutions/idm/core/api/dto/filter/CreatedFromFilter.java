package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;

/**
 * Filter for filtering entities created from given time stamp (auditable.created >= createdFrom).
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface CreatedFromFilter extends BaseDataFilter {

	String PARAMETER_CREATED_FROM = "createdFrom"; 

	/**
	 * Get created from for filtering entities created from this time stamp (auditable.created >= createdFrom).
	 *
	 * @return
	 */
	default ZonedDateTime getCreatedFrom() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_CREATED_FROM);
	}

	/**
	 * Set created from for filtering entities created from this time stamp (auditable.created >= createdFrom).
	 *
	 * @param createdFrom
	 */
	default void setCreatedFrom(ZonedDateTime createdFrom) {
		set(PARAMETER_CREATED_FROM, createdFrom);
	}
}
