package eu.bcvsolutions.idm.core.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * filter builders's administration.
 *
 * @author Kolychev Artem
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/filter-builders")
@Api(
		value = FilterBuilderController.TAG,
		description = "Configure filter builders",
		tags = { FilterBuilderController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class FilterBuilderController  {

	protected static final String TAG = "filter builders filters";

    @Autowired
    private FilterManager filterManager;


    @Autowired
    private PagedResourcesAssembler<Object> pagedResourcesAssembler;


	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Find all filter builders",
			nickname = "findAllFilterBuilders",
			tags = { FilterBuilderController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				},
			notes = "Returns all registered filter builders.")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<FilterBuilderDto> filterBuilderDtos = filterManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(filterBuilderDtos, new PageRequest(0, filterBuilderDtos.size() == 0 ? 10 : filterBuilderDtos.size()), filterBuilderDtos.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, FilterBuilderDto.class, null);
		}
		return pagedResourcesAssembler.toResource(page);
	}



    protected FilterBuilderFilter toFilter(MultiValueMap<String, Object> parameters) {
        return new FilterBuilderFilter(parameters);
    }
}
