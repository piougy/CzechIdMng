package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Schema attribute handling service
 * 
 * @author svandav
 *
 */
public interface SysSystemAttributeMappingService extends ReadWriteEntityService<SysSystemAttributeMapping, SystemAttributeMappingFilter> {
	
	public static final String ATTRIBUTE_VALUE_KEY = "attributeValue";
	public static final String SYSTEM_KEY = "system";
	public static final String IC_ATTRIBUTES_KEY = "icAttributes";
	public static final String ENTITY_KEY = "entity";
	
	public List<SysSystemAttributeMapping> findBySystemMapping(SysSystemMapping systemMapping);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param value
	 * @param attributeMapping
	 * @return transformed value
	 */
	public Object transformValueToResource(Object value, AttributeMapping attributeMapping, AbstractEntity entity);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param value
	 * @param attributeMapping
	 * @param entity 
	 * @param icAttributes 
	 * @return transformed value
	 */
	public Object transformValueFromResource(Object value, AttributeMapping attributeMapping,  List<IcAttribute> icAttributes );

	Object transformValueToResource(Object value, String script, AbstractEntity entity, SysSystem system);

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
	 * @param attributeMapping
	 * @param idmValue
	 * @return
	 */
	IcAttribute createIcAttribute(AttributeMapping attributeMapping, Object idmValue);
}
