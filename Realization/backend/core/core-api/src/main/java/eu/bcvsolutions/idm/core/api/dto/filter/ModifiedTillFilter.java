package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;

/**
 * Filter for filtering entities changed till given time stamp (auditable.modified or uditable.created <= modifiedTill).
 * Created date is used as fallback, if modified is {@code null} => creation is the last modification.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface ModifiedTillFilter extends BaseDataFilter {

	String PARAMETER_MODIFIED_TILL = "modifiedTill";

	/**
	 * Get modified till for filtering entities changed till given time stamp
	 *
	 * @return
	 */
	default ZonedDateTime getModifiedTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_MODIFIED_TILL);
	}

	/**
	 * Set modified till for filtering entities changed till this time stamp
	 *
	 * @param modifiedTill
	 */
	default void setModifiedTill(ZonedDateTime modifiedTill) {
		set(PARAMETER_MODIFIED_TILL, modifiedTill);
	}
}
