package eu.bcvsolutions.idm.acc.service.api;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Schema attribute handling service
 * 
 * @author svandav
 *
 */
public interface SysSystemAttributeMappingService extends ReadWriteEntityService<SysSystemAttributeMapping, SystemAttributeMappingFilter>, CloneableService<SysSystemAttributeMapping> {
	
	public static final String ATTRIBUTE_VALUE_KEY = "attributeValue";
	public static final String SYSTEM_KEY = "system";
	public static final String IC_ATTRIBUTES_KEY = "icAttributes";
	public static final String ENTITY_KEY = "entity";
	public static final String ACCOUNT_UID = "uid";
	
	/**
	 * All mapped attributes in given mapping
	 * 
	 * @param systemMapping
	 * @return
	 */
	List<SysSystemAttributeMapping> findBySystemMapping(SysSystemMapping systemMapping);
	
	/**
	 * Single mapped attribute in given mapping by given name
	 * 
	 * @param systemMappingId
	 * @param name
	 * @return
	 */
	SysSystemAttributeMapping findBySystemMappingAndName(UUID systemMappingId, String name);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param uid - Account identifier, can be null
	 * @param value
	 * @param attributeMapping
	 * @return transformed value
	 */
	Object transformValueToResource(String uid, Object value, AttributeMapping attributeMapping, AbstractEntity entity);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param uid - Account identifier, can be null
	 * @param value
	 * @param attributeMapping
	 * @param entity 
	 * @param icAttributes 
	 * @return transformed value
	 */
	Object transformValueFromResource(Object value, AttributeMapping attributeMapping,  List<IcAttribute> icAttributes );

	Object transformValueToResource(String uid, Object value, String script, AbstractEntity entity, SysSystem system);

	Object transformValueFromResource(Object value, String script, List<IcAttribute> icAttributes, SysSystem system);

	/**
	 * Check on exists EAV definition for given attribute. If the definition not exist, then we try create it.
	 * Update exist attribute definition is not supported.
	 * 
	 * @param attributeMapping
	 * @param entityType
	 */
	void createExtendedAttributeDefinition(AttributeMapping attributeMapping, Class<?> entityType);
	
	/**
	 * Create instance of IC attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param schemaAttribute
	 * @param idmValue
	 * @return
	 */
	IcAttribute createIcAttribute(SysSchemaAttribute schemaAttribute, Object idmValue);
	
	/**
	 * Method return {@link SysSystemAttributeMapping} for system id, that has flag for authentication attribute.
	 * If this attribute don't exist, found attribute flagged as UID, this attribute must exists.
	 * 
	 * @param systemId
	 * @return
	 */
	SysSystemAttributeMapping getAuthenticationAttribute(UUID systemId);

	/**
	 * Find value for this mapped attribute by property name. Returned value can be list of objects. Returns transformed value.
	 * 
	 * @param uid - Account identifier
	 * @param entity
	 * @param attributeHandling
	 * @param idmValue
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	Object getAttributeValue(String uid, AbstractEntity entity, AttributeMapping attributeHandling);

	/**
	 * Generate UID from UID attribute
	 * @param entity
	 * @param uidAttribute
	 * @return
	 */
	String generateUid(AbstractEntity entity, SysSystemAttributeMapping uidAttribute);

	/**
	 * Return UID attribute from list of mapped attributes
	 * @param mappedAttributes
	 * @return
	 */
	SysSystemAttributeMapping getUidAttribute(List<SysSystemAttributeMapping> mappedAttributes, SysSystem system);
}
