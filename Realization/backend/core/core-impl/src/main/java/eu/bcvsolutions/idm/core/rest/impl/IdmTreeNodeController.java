package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "-nodes")
public class IdmTreeNodeController extends DefaultReadWriteEntityController<IdmTreeNode, TreeNodeFilter> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmTreeNodeController.class);
	private final IdmTreeNodeService treeNodeService;
	private final IdmTreeTypeService treeTypeService;
	private final IdmAuditService auditService;
	private final FormService formService;
	//
	private final IdmFormDefinitionController formDefinitionController;
	//
	private Random r = new Random();
	private List<IdmTreeNode> children = new ArrayList<>();
	
	@Autowired
	public IdmTreeNodeController(
			LookupService entityLookupService, 
			IdmTreeNodeService treeNodeService,
			IdmTreeTypeService treeTypeService,
			IdmAuditService auditService,
			IdmFormDefinitionController formDefinitionController,
			FormService formService) {
		super(entityLookupService, treeNodeService);
		//
		Assert.notNull(treeNodeService);
		Assert.notNull(treeTypeService);
		Assert.notNull(auditService);
		Assert.notNull(formDefinitionController);
		Assert.notNull(formService);
		//
		this.treeNodeService = treeNodeService;
		this.treeTypeService = treeTypeService;
		this.auditService = auditService;
		this.formDefinitionController = formDefinitionController;
		this.formService = formService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_CREATE + "') or hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "{treeNodeId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<?> findRevision(@PathVariable("treeNodeId") String treeNodeId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		IdmTreeNode revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, treeNode.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}

		return new ResponseEntity<>(toResource(revision, assembler), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "{treeNodeId}/revisions", method = RequestMethod.GET)
	public Resources<?> findRevisions(@PathVariable("treeNodeId") String treeNodeId, Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmTreeNode.class.getSimpleName(), UUID.fromString(treeNodeId), pageable);
		return toResources(results, assembler, IdmTreeNode.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(
			@RequestParam(value = "treeTypeId", required = false) String treeTypeId,
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {
		// TODO: pageable
		Page<IdmTreeNode> roots = this.treeNodeService.findRoots(UUID.fromString(treeTypeId), null);
		return toResources(roots, assembler, IdmTreeNode.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(
			@RequestParam(value = "parent") @NotNull String parent, 
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {	
		// TODO: pageable
		Page<IdmTreeNode> children = this.treeNodeService.findChildrenByParent(UUID.fromString(parent), null);
		return toResources(children, assembler, IdmTreeNode.class, null);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	public ResponseEntity<?> getFormDefinitions(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmTreeNode.class, assembler);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	public Resources<?> getFormValues(
			@PathVariable @NotNull String backendId, 
			@RequestParam(name = "definitionCode") String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmTreeNode.class, definitionCode);
		}
		//
		return formDefinitionController.getFormValues(entity, formDefinition, assembler);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestParam(name = "definitionCode") String definitionCode,
			@RequestBody @Valid List<IdmTreeNodeFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmTreeNode entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmTreeNode.class, definitionCode);
		}
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues, assembler);
	}
	
	@Override
	protected TreeNodeFilter toFilter(MultiValueMap<String, Object> parameters) {
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setTreeTypeId(getParameterConverter().toUuid(parameters, "treeTypeId"));
		filter.setTreeNode(getParameterConverter().toUuid(parameters, "treeNodeId"));
		filter.setDefaultTreeType(getParameterConverter().toBoolean(parameters, "defaultTreeType"));
 		filter.setRecursively(getParameterConverter().toBoolean(parameters, "recursively", true));
		return filter;
	}
	
	/**
	 * Test tree generate
	 * 
	 * @param count
	 * @return
	 */
	@Deprecated
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/test/create-test-tree/{count}", method = RequestMethod.POST)
	public ResponseEntity<?> createTestTree(@PathVariable("count") Integer count) {	
		LOG.info("Generating new tree with [{}] nodes", count);
		//
		children.clear();
		String treeTypeCode = "t-" + System.currentTimeMillis();
		IdmTreeType treeType = new IdmTreeType();
		treeType.setCode(treeTypeCode);
		treeType.setName(treeTypeCode);
		treeType = treeTypeService.save(treeType);
		//
		long startTime = System.currentTimeMillis();		
		int counter = generateChildren(count, 0, treeType, null);
		LOG.info("[{}] nodes generated: {}ms", counter, (System.currentTimeMillis() - startTime));
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	private int generateChildren(int total, int counter, IdmTreeType treeType, IdmTreeNode parent) {
		int childrenCount = r.nextInt(250) + 1;
		for(int i = 0; i < childrenCount; i++) {
			IdmTreeNode node = new IdmTreeNode();
			node.setTreeType(treeType);
			node.setParent(parent);
			node.setName((parent == null ? "" : parent.getName() + "-") + i);
			node.setCode("n-" + i + "-" + System.currentTimeMillis());
			node = treeNodeService.save(node);
			if(children.size() < 25) {
				children.add(node);
			}
			if ((i + counter + 1) >= total) {
				return i + counter + 1;
			}
			if((i + counter + 1) % 1000 == 0) {
				LOG.info("[{}] nodes generated ...", (i + counter + 1));
			}
		}
		IdmTreeNode firstChild = children.remove(0);
		counter = generateChildren(total, counter + childrenCount, treeType, firstChild);
		if (counter >= total) {
			return counter;
		}
		return counter;		
	}
	
}
