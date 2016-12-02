package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;

/**
 * System role attribute mapping excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSchemaAttributeHandling.class)
public interface SysSchemaAttributeHandlingExcerpt extends AbstractDtoProjection {

	String getName();

	String getIdmPropertyName();

	SysSchemaAttribute getSchemaAttribute();

	boolean isExtendedAttribute();

	boolean isUid();

	@Value("#{target.transformFromResourceScript != null && !target.transformFromResourceScript.isEmpty()}")
	boolean isTransformationFromResource();

	@Value("#{target.transformToResourceScript != null && !target.transformToResourceScript.isEmpty()}")
	boolean isTransformationToResource();

	public boolean isConfidentialAttribute();

	public boolean isEntityAttribute();
}
