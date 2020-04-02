package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;

/**
 * Filter for filtering entities changed from given time stamp (auditable.modified or uditable.created  >= modifiedFrom).
 * Created date is used as fallback, if modified is {@code null} => creation is the last modification.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 9.7.7
 */
public interface ModifiedFromFilter extends BaseDataFilter {

	String PARAMETER_MODIFIED_FROM = "modifiedFrom";

	/**
	 * Get modified from for filtering entities changed from given time stamp.
	 *
	 * @return
	 */
	default ZonedDateTime getModifiedFrom() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_MODIFIED_FROM);
	}

	/**
	 * Set modified from for filtering entities changed from this time stamp.
	 *
	 * @param modifiedFrom
	 */
	default void setModifiedFrom(ZonedDateTime modifiedFrom) {
		set(PARAMETER_MODIFIED_FROM, modifiedFrom);
	}
}
