package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Entity event procesor's administration
 * 
 * TODO: enable / disable
 * TODO: change processors order
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/entity-event-processors")
@Api(
		value = EntityEventProcessorController.TAG, 
		description = "Configure event processing", 
		tags = { EntityEventProcessorController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class EntityEventProcessorController {

	protected static final String TAG = "Entity event processors";
	private final EntityEventManager entityEventManager;

	@Autowired
	public EntityEventProcessorController(EntityEventManager entityEventManager) {
		Assert.notNull(entityEventManager, "EntityEventManager is required");
		//
		this.entityEventManager = entityEventManager;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Find all processors", 
			nickname = "findAllEntityEventProcessors", 
			tags = { EntityEventProcessorController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				},
			notes = "Returns all registered entity event processors with state properties (diabled, order)")
	public List<EntityEventProcessorDto> find() {
		return entityEventManager.find(null);
	}
}
