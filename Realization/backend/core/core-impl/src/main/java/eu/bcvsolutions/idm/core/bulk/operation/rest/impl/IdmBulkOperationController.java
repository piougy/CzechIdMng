package eu.bcvsolutions.idm.core.bulk.operation.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.bulk.operation.BulkOperationManager;
import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/bulk-operations")
@Api(
		value = IdmBulkOperationController.TAG, 
		description = "Bulk operations", 
		tags = { IdmBulkOperationController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmBulkOperationController implements BaseController {

	protected static final String TAG = "Bulk operations";
	
	@Autowired
	private BulkOperationManager bulkOperationManager;
	
	@ResponseBody
	@ApiOperation(
			value = "Process bulk operation",
			notes= "Process bulk operation",
			response = IdmBulkOperationDto.class,
			tags = { IdmBulkOperationController.TAG } )
	@RequestMapping(method = RequestMethod.POST)
	public Resource<IdmBulkOperationDto> process(
			@ApiParam(value = "Operation request.", required = true)
			@Valid @RequestBody(required = true) IdmBulkOperationDto bulkOperationDto) {
		
		return new Resource<IdmBulkOperationDto>(bulkOperationManager.processOperation(bulkOperationDto));
	}

	@ResponseBody
	@ApiOperation(
			value = "Get list of all bulk operations", 
			notes= "Get list of all bulk operations",
			response = IdmBulkOperationDto.class,
			tags = { IdmBulkOperationController.TAG } )
	@RequestMapping(method = RequestMethod.GET)
	public Resources<IdmBulkOperationDto> findAll(
			@ApiParam(value = "Class of entioty for wich will be search operations.", required = true)
			@NotNull String entityName) {
		Class<?> forName;
		try {
			forName = Class.forName(entityName);
		} catch (ClassNotFoundException e) {
			// TODO: exception
			return null;
		}
		if (AbstractEntity.class.isAssignableFrom(forName)) {
			Class<? extends AbstractEntity> clazz = (Class<? extends AbstractEntity>) forName;
			return new Resources<IdmBulkOperationDto>(bulkOperationManager.getAvailableOperations(clazz));
		}
		// TODO: exception
		return null;
	}
}
