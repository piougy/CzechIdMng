package eu.bcvsolutions.idm.core.model.entity;

import java.util.Date;

/**
 * Base audit information
 * 
 * @author Radek Tomiška 
 *
 */
public interface AuditableEntity {

	/**
	 * Entity author
	 * 
	 * @return
	 */
	String getCreator();

	/**
	 * Entity author
	 * 
	 * @param creator
	 */
	void setCreator(String creator);
	
	/**
	 * Original Entity author (logged as creator)
	 * 
	 * @return
	 */
	String getOriginalCreator();

	/**
	 * Original Entity author (logged as creator)
	 * 
	 * @param creator
	 */
	void setOriginalCreator(String creator);

	/**
	 * Created date
	 * 
	 * @return
	 */
	Date getCreated();

	/**
	 * Created date
	 * 
	 * @param created
	 */
	void setCreated(Date created);

	/**
	 * Original last entity modifier (logged as modifier)
	 * 
	 * @return
	 */
	String getOriginalModifier();
	
	/**
	 * Original last entity modifier (logged as modifier)
	 * 
	 * @param modifier
	 */
	void setOriginalModifier(String modifier);
	
	/**
	 * Last entity modifier
	 * 
	 * @return
	 */
	String getModifier();
	
	/**
	 * Last entity modifier
	 * 
	 * @param modifier
	 */
	void setModifier(String modifier);

	/**
	 * Last entity modified date
	 * 
	 * @return
	 */
	Date getModified();

	/**
	 * Last entity modified date
	 * 
	 * @param modified
	 */
	void setModified(Date modified);
}
