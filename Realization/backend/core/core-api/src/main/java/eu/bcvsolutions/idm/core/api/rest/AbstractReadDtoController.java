package eu.bcvsolutions.idm.core.api.rest;

import java.io.Serializable;
import java.util.Collections;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
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

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;

/**
 * Read operations (get, find)
 * 
 * @author Svanda
 *
 */
public abstract class AbstractReadDtoController<DTO extends BaseDto, F extends BaseFilter>
		implements BaseDtoController<DTO> {

	private FilterConverter filterConverter;

	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@Autowired(required = false)
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	private final ReadDtoService<DTO, ?, F> service;
	@Autowired
	private EntityLookupService entityLookupService;

	public AbstractReadDtoController(ReadDtoService<DTO, ?, F> service) {
		Assert.notNull(service, "Service is required!");

		this.service = service;
	}

	/**
	 * Returns DTO service configured to current controller
	 * 
	 * @return
	 */
	protected ReadDtoService<DTO, ?, F> getService() {
		return service;
	}

	/**
	 * Returns controlled DTO class
	 * 
	 * @return
	 */
	protected Class<DTO> getDtoClass() {
		return getService().getDtoClass();
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		DTO entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return new ResponseEntity<>(toResource(entity), HttpStatus.OK);
	}

	/**
	 * Returns DTO by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	public DTO getDto(Serializable backendId) {
		return getService().getDto(backendId);
	}

	/**
	 * Quick search - parameters will be transformed to filter object
	 * 
	 * @see #toFilter(MultiValueMap)
	 * 
	 * @param parameters
	 * @param pageable
	 * @param assembler
	 * @return
	 */
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return toResources(findDtos(toFilter(parameters), pageable), getDtoClass());
	}

	/**
	 * Quick search - finds DTOs by given filter and pageable
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	public Page<DTO> findDtos(F filter, Pageable pageable) {
		return getService().findDto(filter, pageable);
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
		if (source instanceof Page) {
			Page<Object> page = (Page<Object>) source;
			return pageToResources(page, domainType);
		}
		return null;
	}

	protected Resources<?> pageToResources(Page<Object> page, Class<?> domainType) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType, null);
		}

		return pagedResourcesAssembler.toResource(page);
	}

	/**
	 * Transforms request parameters to {@link BaseFilter}.
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
			filterConverter = new FilterConverter(entityLookupService, mapper);
		}
		return filterConverter;
	}
}
