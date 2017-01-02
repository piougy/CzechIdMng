package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.MappingAttribute;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Schema attribute handling service
 * @author svandav
 *
 */
public interface SysSchemaAttributeHandlingService extends ReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> {
	
	public static final String ATTRIBUTE_VALUE_KEY = "attributeValue";
	public static final String SYSTEM_KEY = "system";
	public static final String IC_ATTRIBUTES_KEY = "icAttributes";
	public static final String ENTITY_KEY = "entity";
	
	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param value
	 * @param attributeHandling
	 * @return transformed value
	 */
	public Object transformValueToResource(Object value, MappingAttribute attributeHandling, AbstractEntity entity);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param value
	 * @param attributeHandling
	 * @param entity 
	 * @param icAttributes 
	 * @return transformed value
	 */
	public Object transformValueFromResource(Object value, MappingAttribute attributeHandling,  List<IcAttribute> icAttributes );

	Object transformValueToResource(Object value, String script, AbstractEntity entity, SysSystem system);

	Object transformValueFromResource(Object value, String script, List<IcAttribute> icAttributes, SysSystem system);

	/**
	 * Check on exists EAV definition for given attribute. If the definition not exist, then we try create it.
	 * Update exist attribute definition is not supported.
	 * @param entity
	 * @param entityType
	 */
	void createExtendedAttributeDefinition(MappingAttribute entity, Class<?> entityType);
}
