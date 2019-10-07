package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;

/**
 * Filter for filtering entities changed from given time stamp
 * 
 * @author Vít Švanda
 * @since 9.7.7
 */
public interface ModifiedFromFilter extends BaseFilter {
	
	String PARAMETER_MODIFIED_FROM = "modifiedFrom";
	
	/**
	 * Get modified from for filtering entities changed from given time stamp
	 * 
	 * @return
	 */
	ZonedDateTime getModifiedFrom();

	/**
	 * Set modified from for filtering entities changed from this time stamp
	 * 
	 * @param modifiedFrom
	 */
	void setModifiedFrom(ZonedDateTime modifiedFrom);
}