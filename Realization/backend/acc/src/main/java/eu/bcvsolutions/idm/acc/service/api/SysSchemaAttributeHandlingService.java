package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Schema attribute handling service
 * @author svandav
 *
 */
public interface SysSchemaAttributeHandlingService extends ReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> {
	
	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param value
	 * @param attributeHandling
	 * @return transformed value
	 */
	public Object transformValueToSystem(Object value, SysSchemaAttributeHandling attributeHandling);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param value
	 * @param attributeHandling
	 * @return transformed value
	 */
	public Object transformValueFromSystem(Object value, SysSchemaAttributeHandling attributeHandling);
}
