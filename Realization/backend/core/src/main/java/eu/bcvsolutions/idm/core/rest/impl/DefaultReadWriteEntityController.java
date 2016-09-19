package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Default CRUD controller for given {@link BaseEntity}.
 * 
 * @author Radek Tomiška
 *
 * @param <E> controlled {@link BaseEntity} type.
 */
public abstract class DefaultReadWriteEntityController<E extends BaseEntity> extends AbstractReadWriteController<E>{

	public DefaultReadWriteEntityController(ReadWriteEntityService<E> entityService) {
		super(entityService);
	}
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> searchCollectionResource(@PageableDefault Pageable pageable, 
			@RequestParam MultiValueMap<String, Object> parameters, 
			PagedResourcesAssembler<Object> pagedAssembler,
			PersistentEntityResourceAssembler assembler) {
		return super.searchCollectionResource(pageable, parameters, pagedAssembler, assembler);
	}
	
	@Override
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> searchQuickCollectionResource(@PageableDefault Pageable pageable, 
			@RequestParam MultiValueMap<String, Object> parameters, 
			PagedResourcesAssembler<Object> pagedAssembler,
			PersistentEntityResourceAssembler assembler) {
		return super.searchQuickCollectionResource(pageable, parameters, pagedAssembler, assembler);
	}
	
	@Override
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> getItemResource(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.getItemResource(backendId, assembler);
	}
	
	@Override
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> postCollectionResource(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws Exception {
		return super.postCollectionResource(nativeRequest, assembler);
	}
	
	@Override
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> putItemResource(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws Exception {
		return super.putItemResource(backendId, nativeRequest, assembler);
	}
	
	@Override
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patchItemResource(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws Exception {
		return super.patchItemResource(backendId, nativeRequest, assembler);
	}
	
	@Override
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteItemResource(@PathVariable @NotNull String backendId) {
		return super.deleteItemResource(backendId);
	}
}
