package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Schema attribute handling excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysRoleSystemAttribute.class)
public interface SysRoleSystemAttributeExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	String getIdmPropertyName();
	
	SysSystemAttributeMapping getSystemAttributeMapping();

	boolean isExtendedAttribute();
	
	boolean isEntityAttribute();
	
	boolean isConfidentialAttribute();
	
	boolean isUid();
	
	boolean isDisabledDefaultAttribute();
	
	@Value("#{target.transformScript != null && !target.transformScript.isEmpty()}") 
	boolean isTransformScript();


}
