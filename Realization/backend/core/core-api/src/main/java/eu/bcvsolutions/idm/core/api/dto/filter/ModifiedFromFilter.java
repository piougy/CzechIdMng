package eu.bcvsolutions.idm.core.api.dto.filter;

import org.joda.time.DateTime;

/**
 * Filter for filtering entities changed from given time stamp
 * 
 * @author Vít Švanda
 *
 */
public interface ModifiedFromFilter extends BaseFilter {
	
	public static final String PARAMETER_MODIFIED_FROM = "modifiedFrom";

	/**
	 * Get modified from for filtering entities changed from given time stamp
	 * 
	 * @return
	 */
	DateTime getModifiedFrom();

	/**
	 * Set modified from for filtering entities changed from this time stamp
	 * 
	 * @param modifiedFrom
	 */
	void setModifiedFrom(DateTime modifiedFrom);

}