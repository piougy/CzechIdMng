package eu.bcvsolutions.idm.core.api.rest;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * Read operations (get, find, autocomplete)
 * 
 * @param <DTO> dto type
 * @param <F> filter type - {@link DataFilter} is preferred.
 * @author Svanda
 * @author Radek Tomiška
 */
public abstract class AbstractReadDtoController<DTO extends BaseDto, F extends BaseFilter>
		implements BaseDtoController<DTO> {

	// private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReadDtoController.class);
	private FilterConverter filterConverter;
	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	@Autowired(required = false)
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	private final ReadDtoService<DTO, F> service;
	@Autowired
	private LookupService lookupService;

	public AbstractReadDtoController(ReadDtoService<DTO, F> service) {
		Assert.notNull(service, "Service is required!");

		this.service = service;
	}

	/**
	 * Returns DTO service configured to current controller
	 * 
	 * @return
	 */
	protected ReadDtoService<DTO, F> getService() {
		return service;
	}

	/**
	 * Returns controlled DTO class.
	 * 
	 * @return
	 */
	protected Class<DTO> getDtoClass() {
		return getService().getDtoClass();
	}
	
	/**
	 * Returns controlled {@link BaseFilter} type class.
	 * 
	 * @return
	 */
	protected Class<F> getFilterClass() {
		return getService().getFilterClass();
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "Read record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true)
			@PathVariable @NotNull String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return new ResponseEntity<>(toResource(dto), HttpStatus.OK);
	}

	/**
	 * Returns DTO by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DTO getDto(Serializable backendId) {
		if(lookupService == null) {
			return getService().get(backendId, IdmBasePermission.READ);
		}
		DTO dto = (DTO) lookupService.lookupDto(getDtoClass(), backendId);
		return checkAccess(dto, IdmBasePermission.READ);
	}

	/**
	 * Quick search - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
     * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Search records (/search/quick alias)", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return toResources(find(toFilter(parameters), pageable, IdmBasePermission.READ), getDtoClass());
	}
	
	/**
	 * All endpoints will support find quick method.
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Search records", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}
	
	/**
	 * Quick search for autocomplete (read data to select box etc.) - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @param assembler
	 * @return
     * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Autocomplete records (selectbox usage)", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return toResources(find(toFilter(parameters), pageable, IdmBasePermission.AUTOCOMPLETE), getDtoClass());
	}

	/**
	 * Quick search - finds DTOs by given filter and pageable
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission
	 * @return
	 */
	public Page<DTO> find(F filter, Pageable pageable, BasePermission permission) {
		return getService().find(filter, pageable, permission);
	}
	
	/**
	 * Returns, what currently logged identity can do with given dto
	 * 
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "What logged identity can do with given record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public Set<String> getPermissions(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true)
			@PathVariable @NotNull String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return getService().getPermissions(dto.getId());
	}

	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	protected ResourceSupport toResource(DTO dto) {
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		Resource<DTO> resourceSupport = new Resource<DTO>(dto, selfLink);
		return resourceSupport;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Resources<?> toResources(Iterable<?> source, Class<?> domainType) {
		if (source == null) {
			return new Resources(Collections.emptyList());
		}
		Page<Object> page;
		if (source instanceof Page) {
			page = (Page<Object>) source;
		} else {
			// Iterable to Page
			List records = Lists.newArrayList(source);
			page = new PageImpl(records, new PageRequest(0, records.size()), records.size());
		}
		return pageToResources(page, domainType);
	}

	protected Resources<?> pageToResources(Page<Object> page, Class<?> domainType) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType, null);
		}

		return pagedResourcesAssembler.toResource(page);
	}

	/**
	 * Transforms request parameters to:
	 * - {@link BaseFilter} using object mapper
	 * - {@link DataFilter} using reflection with constructor(parameters).
	 * 
	 * @param parameters
	 * @return
	 */
	protected F toFilter(MultiValueMap<String, Object> parameters) {	
		return getParameterConverter().toFilter(parameters, getService().getFilterClass());
	}

	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, mapper);
		}
		return filterConverter;
	}
	
	/**
	 * Evaluates authorization permission on given dto
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	protected DTO checkAccess(DTO dto, BasePermission... permission) {
		return getService().checkAccess(dto, permission);
	}
}
