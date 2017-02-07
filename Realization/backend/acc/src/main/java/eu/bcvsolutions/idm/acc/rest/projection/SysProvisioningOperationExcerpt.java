package eu.bcvsolutions.idm.acc.rest.projection;

import java.util.UUID;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Excerpt projection for {@link SysProvisioningOperation}.
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = SysProvisioningOperation.class)
public interface SysProvisioningOperationExcerpt extends AbstractDtoProjection {
	
	ProvisioningEventType getOperationType();

	SysSystem getSystem();

	SystemEntityType getEntityType();

	UUID getEntityIdentifier();
	
	String getSystemEntityUid();
	
	OperationState getResultState();
	
	OperationResult getResult();
	
	SysProvisioningRequest getRequest();
	
	ProvisioningContext getProvisioningContext();
}
