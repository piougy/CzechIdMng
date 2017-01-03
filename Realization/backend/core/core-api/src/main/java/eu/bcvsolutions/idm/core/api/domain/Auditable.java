package eu.bcvsolutions.idm.core.api.domain;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Base audit information
 * 
 * @author Radek Tomiška 
 *
 */
public interface Auditable {
	
	final String PROPERTY_CREATED = "created";
	final String PROPERTY_CREATOR = "creator";
	final String PROPERTY_CREATOR_ID = "creatorId";
	final String PROPERTY_ORIGINAL_CREATOR = "originalCreator";
	final String PROPERTY_ORIGINAL_CREATOR_ID = "originalCreatorId";
	final String PROPERTY_MODIFIED = "modified";
	final String PROPERTY_MODIFIER = "modifier";
	final String PROPERTY_MODIFIER_ID = "modifierId";
	final String PROPERTY_ORIGINAL_MODIFIER = "originalModifier";
	final String PROPERTY_ORIGINAL_MODIFIER_ID = "originalModifierId";
	
	/**
	 * Entity identifier
	 * @return
	 */
	UUID getId();
	
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
	DateTime getCreated();

	/**
	 * Created date
	 * 
	 * @param created
	 */
	void setCreated(DateTime created);

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
	DateTime getModified();

	/**
	 * Last entity modified date
	 * 
	 * @param modified
	 */
	void setModified(DateTime modified);
	
	/**
	 * Entity author identifier
	 * 
	 * @return
	 */
	UUID getCreatorId();

	/**
	 * Entity author identifier
	 * 
	 * @param creatorId
	 */
	void setCreatorId(UUID creatorId);

	/**
	 * Original Entity author identifier (logged as creator)
	 * 
	 * @return
	 */
	UUID getOriginalCreatorId();

	/**
	 * Original Entity author identifier (logged as creator)
	 * 
	 * @param originalCreatorId
	 */
	void setOriginalCreatorId(UUID originalCreatorId);

	/**
	 * Last entity modifier identifier
	 * 
	 * @param modified
	 */
	UUID getModifierId();

	/**
	 * Last entity modifier identifier
	 * 
	 * @param modifierId
	 */
	void setModifierId(UUID modifierId);

	/**
	 * Original last entity modifier identifier (logged as modifier)
	 * 
	 * @return
	 */
	UUID getOriginalModifierId();

	/**
	 * Original last entity modifier identifier (logged as modifier)
	 * 
	 * @param originalModifierId
	 */
	void setOriginalModifierId(UUID originalModifierId);
}
