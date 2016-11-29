package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;

/**
 * Schema attribute handling service
 * @author svandav
 *
 */
public interface SysSchemaAttributeHandlingService extends ReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> {
	
	public static final String ATTRIBUTE_VALUE_KEY = "attributeValue";
	public static final String SYSTEM_KEY = "system";
	public static final String ICF_ATTRIBUTES_KEY = "icfAttributes";
	public static final String ENTITY_KEY = "entity";
	
	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param value
	 * @param attributeHandling
	 * @return transformed value
	 */
	public Object transformValueToResource(Object value, SysSchemaAttributeHandling attributeHandling, AbstractEntity entity);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param value
	 * @param attributeHandling
	 * @param entity 
	 * @param icfAttributes 
	 * @return transformed value
	 */
	public Object transformValueFromResource(Object value, SysSchemaAttributeHandling attributeHandling,  List<IcfAttribute> icfAttributes );
}
