package eu.bcvsolutions.idm.core.api.dto.filter;

import org.joda.time.DateTime;

/**
 * Filter for filtering entities changed from given time stamp
 * 
 * @author Vít Švanda
 *
 */
public interface ModifiedFromFilter extends BaseFilter {

	DateTime getModifiedFrom();

	void setModifiedFrom(DateTime modifiedFrom);

}