package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.rest.impl.IdmLongRunningTaskController;

/**
 * Tree type structures
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "-types")
public class IdmTreeTypeController extends DefaultReadWriteEntityController<IdmTreeType, QuickFilter> {
	
	@Autowired
	private IdmLongRunningTaskController longRunningTaskController;
	
	@Autowired
	public IdmTreeTypeController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/default", method = RequestMethod.GET)
	public ResponseEntity<?> getDefaultTreeType(PersistentEntityResourceAssembler assembler) {
		IdmTreeType defaultTreeType = entityLookupService.getEntityService(IdmTreeType.class, IdmTreeTypeService.class).getDefaultTreeType();
		if (defaultTreeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", "default"));
		}		
		return new ResponseEntity<>(toResource(defaultTreeType, assembler), HttpStatus.OK);
	}
	
	/**
	 * Returns all configuration properties for given tree type.
	 * 
	 * @param backendId
	 * @return list of granted authorities
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/configurations", method = RequestMethod.GET)
	public List<ConfigurationDto> getConfigurations(@PathVariable String backendId) {
		IdmTreeType treeType = getEntity(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return entityLookupService.getEntityService(IdmTreeType.class, IdmTreeTypeService.class).getConfigurations(treeType);
	}
	
	/**
	 * Rebuild (drop and create) all indexes for given treeType.
	 * 
	 * @param backendId tree type id
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/index/rebuild", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_WRITE + "')")
	public ResponseEntity<?> rebuildIndex(@PathVariable String backendId, PersistentEntityResourceAssembler assembler) {
		IdmTreeType treeType = getEntity(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		UUID longRunningTaskId = entityLookupService.getEntityService(IdmTreeNode.class, IdmTreeNodeService.class).rebuildIndexes(treeType);
		//
		return longRunningTaskController.get(longRunningTaskId.toString(), assembler);
	}
}
