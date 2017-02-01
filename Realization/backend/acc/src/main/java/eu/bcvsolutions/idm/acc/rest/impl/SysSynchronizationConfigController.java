package eu.bcvsolutions.idm.acc.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationConfigService;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.security.api.domain.Enabled;;

/**
 * System synchronization config rest
 * 
 * @author svandav
 *
 */
@RepositoryRestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/system-synchronization-configs")
public class SysSynchronizationConfigController
		extends AbstractReadWriteEntityController<SysSyncConfig, SynchronizationConfigFilter> {

	private final SynchronizationService synchronizationService;
	
	@Autowired
	public SysSynchronizationConfigController(EntityLookupService entityLookupService,
			SysSynchronizationConfigService service, SynchronizationService synchronizationService) {
		super(entityLookupService, service);
		Assert.notNull(synchronizationService);
		
		this.synchronizationService = synchronizationService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Start synchronization
	 * @param backendId
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_WRITE + "')")
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.POST)
	public ResponseEntity<?> startSynchronization(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return new ResponseEntity<>(toResource(this.synchronizationService.startSynchronizationEvent(this.getEntityService().get(backendId)), assembler), HttpStatus.OK);
	}
	
	/**
	 * Cancel synchronization
	 * @param backendId
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_WRITE + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.POST)
	public ResponseEntity<?> cancelSynchronization(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return new ResponseEntity<>(toResource(this.synchronizationService.stopSynchronizationEvent(this.getEntityService().get(backendId)), assembler), HttpStatus.OK);
	}
	
	/**
	 * Is synchronization running
	 * @param backendId
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/isRunning", method = RequestMethod.POST)
	public ResponseEntity<?> isRunningSynchronization(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		
		boolean running = ((SysSynchronizationConfigService)this.getEntityService()).isRunning(this.getEntityService().get(backendId));
		return new ResponseEntity<>(running, HttpStatus.OK);
	}

	@Override
	protected SynchronizationConfigFilter toFilter(MultiValueMap<String, Object> parameters) {
		SynchronizationConfigFilter filter = new SynchronizationConfigFilter();
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		return filter;
	}
}
