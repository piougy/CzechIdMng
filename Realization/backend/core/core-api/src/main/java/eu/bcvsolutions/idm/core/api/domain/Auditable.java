package eu.bcvsolutions.idm.core.api.domain;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Base audit information
 * 
 * @author Radek Tomiška 
 */
public interface Auditable {
	
	String PROPERTY_CREATED = "created";
	String PROPERTY_CREATOR = "creator";
	String PROPERTY_CREATOR_ID = "creatorId";
	String PROPERTY_ORIGINAL_CREATOR = "originalCreator";
	String PROPERTY_ORIGINAL_CREATOR_ID = "originalCreatorId";
	String PROPERTY_MODIFIED = "modified";
	String PROPERTY_MODIFIER = "modifier";
	String PROPERTY_MODIFIER_ID = "modifierId";
	String PROPERTY_ORIGINAL_MODIFIER = "originalModifier";
	String PROPERTY_ORIGINAL_MODIFIER_ID = "originalModifierId";
	String PROPERTY_TRANSACTION_ID = "transactionId";
	
	/**
	 * Entity identifier
     *
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
	 * Original last entity modifier (logged identity before authentication was switched)
	 * 
	 * @return
	 */
	String getOriginalModifier();
	
	/**
	 * Original last entity modifier (logged identity before authentication was switched)
	 * 
	 * @param modifier
	 */
	void setOriginalModifier(String modifier);
	
	/**
	 * Last entity modifier. When entity is not modified returns {@code null}.
	 * 
	 * @return
	 */
	String getModifier();
	
	/**
	 * Last entity modifier.
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
	 * Original Entity author identifier (logged identity before authentication was switched).
	 * 
	 * @return
	 */
	UUID getOriginalCreatorId();

	/**
	 * Original Entity author identifier (logged identity before authentication was switched).
	 * 
	 * @param originalCreatorId
	 */
	void setOriginalCreatorId(UUID originalCreatorId);

	/**
	 * Last entity modifier identifier. When entity is not modified returns {@code null}.
	 * 
	 * @param modified
	 */
	UUID getModifierId();

	/**
	 * Last entity modifier identifier.
	 * 
	 * @param modifierId
	 */
	void setModifierId(UUID modifierId);

	/**
	 * Original last entity modifier identifier (logged identity before authentication was switched). When entity is not modified returns {@code null}.
	 * 
	 * @return
	 */
	UUID getOriginalModifierId();

	/**
	 * Original last entity modifier identifier (logged identity before authentication was switched).
	 * 
	 * @param originalModifierId
	 */
	void setOriginalModifierId(UUID originalModifierId);
	
	/**
	 * Returns batch transaction id (entity was created or modified in given transaction).
	 * 
	 * @return
	 */
	UUID getTransactionId();

	/**
	 * Sets batch transaction id (entity was created or modified in given transaction).
	 * 
	 * @param originalModifierId
	 */
	void setTransactionId(UUID transactionId);	
	
	/**
	 * Returns entity's realm (tenant) identifier
	 * 
	 * @return
	 */
	UUID getRealmId();
	
	/**
	 * Sets entity's realm (tenant) identifier
	 * 
	 * @param realmId
	 */
	void setRealmId(UUID realmId);
}
