package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ExportDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueForestContentService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RebuildRoleCatalogueIndexTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Implementation of @IdmRoleCatalogueService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service("roleCatalogueService")
public class DefaultIdmRoleCatalogueService 
		extends AbstractEventableDtoService<IdmRoleCatalogueDto, IdmRoleCatalogue, IdmRoleCatalogueFilter> 
		implements IdmRoleCatalogueService {
	
	private final IdmRoleCatalogueRepository repository;
	private final IdmRoleCatalogueRoleService roleCatalogueRoleService;
	private final DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	private final IdmRoleCatalogueForestContentService forestContentService;
	private final IdmConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public DefaultIdmRoleCatalogueService(
			IdmRoleCatalogueRepository repository,
			DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService,
			IdmRoleCatalogueRoleService roleCatalogueRoleService,
			IdmRoleCatalogueForestContentService forestContentService,
			IdmConfigurationService configurationService,
			LongRunningTaskManager longRunningTaskManager,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		Assert.notNull(baseTreeService, "Service is required.");
		Assert.notNull(roleCatalogueRoleService, "Service is required.");
		Assert.notNull(forestContentService, "Service is required.");
		Assert.notNull(configurationService, "Service is required.");
		Assert.notNull(longRunningTaskManager, "Manager is required.");
		//
		this.repository = repository;
		this.baseTreeService = baseTreeService;
		this.roleCatalogueRoleService = roleCatalogueRoleService;
		this.forestContentService = forestContentService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLECATALOGUE, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleCatalogueDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}
	
	@Override
	@Transactional
	public IdmRoleCatalogueDto saveInternal(IdmRoleCatalogueDto roleCatalogue) {
		if (isNew(roleCatalogue)) {
			this.validate(toEntity(roleCatalogue));
			// create new
			roleCatalogue = super.saveInternal(roleCatalogue);
			IdmForestIndexEntity index = forestContentService.createIndex(IdmRoleCatalogue.FOREST_TREE_TYPE, roleCatalogue.getId(), roleCatalogue.getParent());
			return setForestIndex(roleCatalogue, index);
		}
		this.validate(toEntity(roleCatalogue, repository.findById(roleCatalogue.getId()).orElse(null)));
		// update - we need to reindex first
		IdmForestIndexEntity index = forestContentService.updateIndex(IdmRoleCatalogue.FOREST_TREE_TYPE, roleCatalogue.getId(), roleCatalogue.getParent());
		roleCatalogue = super.saveInternal(roleCatalogue);
		return setForestIndex(roleCatalogue, index);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmRoleCatalogueDto roleCatalogue) {
		Page<IdmRoleCatalogue> nodes = repository.findChildren(roleCatalogue.getId(), PageRequest.of(0, 1));
		if (nodes.getTotalElements() != 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of("roleCatalogue", roleCatalogue.getCode()));
		}
		//
		// remove row from intersection table
		roleCatalogueRoleService
			.findAllByRoleCatalogue(roleCatalogue.getId())
			.forEach(roleCatalogueRole -> {
				roleCatalogueRoleService.delete(roleCatalogueRole);
			});
		//
		forestContentService.deleteIndex(roleCatalogue.getId());
		//
		super.deleteInternal(roleCatalogue);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogueDto> findRoots(Pageable pageable) {
		return toDtoPage(repository.findRoots(pageable));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogueDto> findChildrenByParent(UUID parentId, Pageable pageable) {
		return toDtoPage(repository.findChildren(parentId, pageable));
	}
	
	@Override
	public UUID rebuildIndexes() {
		RebuildRoleCatalogueIndexTaskExecutor rebuildTask = AutowireHelper.createBean(RebuildRoleCatalogueIndexTaskExecutor.class);
		UUID longRunningTaskId = longRunningTaskManager.execute(rebuildTask).getExecutor().getLongRunningTaskId();
		// wee need long running task related to index rebuild immediately
		configurationService.setValue(getConfigurationPropertyName(IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD), longRunningTaskId.toString());
		return longRunningTaskId;
	}
	
	@Override
	public String getConfigurationPropertyName(String propertyName) {
		Assert.notNull(propertyName, "Property name is required.");
		//
		return String.format("%s%s", CONFIGURATION_PREFIX, propertyName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueDto> findAllByRole(UUID roleId) {
		return roleCatalogueRoleService
			.findAllByRole(roleId)
			.stream()
			.map(roleCatalogueRole -> DtoUtils.getEmbedded(roleCatalogueRole, IdmRoleCatalogueRole_.roleCatalogue, IdmRoleCatalogueDto.class))
			.collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueDto> findAllParents(UUID catalogueId) {
		IdmRoleCatalogue catalogue = this.getEntity(catalogueId);
		List<IdmRoleCatalogue> roleCatalogues = repository.findAllParents(catalogue, null);
		return toDtos(roleCatalogues, true);
	}
	
	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Batch cannot be null!");
		// We need to export all catalog items (direct to the root)
		IdmRoleCatalogueDto catalog = this.get(id);
		if (catalog != null) {
			UUID parent = catalog.getParent();
			if (parent != null) {
				this.export(parent, batch);
			}
		}
		super.export(id, batch);

		// Authoritative mode is not set here only parent field could be sets.
		ExportDescriptorDto descriptor = getExportManager().getDescriptor(batch, IdmRoleCatalogueDto.class);
		if (descriptor != null) {
			descriptor.getParentFields().add(IdmRoleCatalogue_.parent.getName());
		}
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleCatalogue> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleCatalogueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmRoleCatalogue_.name)), "%" + text + "%"),
							builder.like(builder.lower(root.get(IdmRoleCatalogue_.code)), "%" + text + "%")
							)
					);
		}
		String code = filter.getCode();
		if (StringUtils.isNotEmpty(code)) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogue_.code), code));
		}
		String name = filter.getName();
		if (StringUtils.isNotEmpty(name)) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogue_.name), name));
		}
		UUID parent = filter.getParent();
		if (parent != null) {
			// recursively
			if (filter.isRecursively()) {
				Subquery<IdmRoleCatalogue> subquery = query.subquery(IdmRoleCatalogue.class);
				Root<IdmRoleCatalogue> subRoot = subquery.from(IdmRoleCatalogue.class);
				subquery.select(subRoot);
				Path<IdmForestIndexEntity> forestIndexPath = subRoot.get(IdmRoleCatalogue_.forestIndex);
				subquery.where(builder.and(
					builder.equal(subRoot.get(AbstractEntity_.id), parent),
					// This is here because of the structure of forest index. We need to select only subtree and not the element itself.
					// In order to do that, we must shrink the boundaries of query so it is true only for subtree of given node.
					// Remember that between clause looks like this a >= x <= b, where a and b are boundaries, in our case lft+1 and rgt-1.
					
					builder.between(root.get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft),
						builder.sum(forestIndexPath.get(IdmForestIndexEntity_.lft), 1L),
						builder.diff(forestIndexPath.get(IdmForestIndexEntity_.rgt), 1L))));
				predicates.add(builder.exists(subquery));
			} else {
				predicates.add(builder.equal(root.get(IdmRoleCatalogue_.parent).get(IdmRoleCatalogue_.id), parent));
			}
		}
		// roots
		Boolean roots = filter.getRoots();
		if (roots != null) {
			if (roots) {
				predicates.add(builder.isNull(root.get(IdmRoleCatalogue_.parent)));
			} else {
				predicates.add(builder.isNotNull(root.get(IdmRoleCatalogue_.parent)));
			}
		}
		return predicates;
	}
	
	/**
	 * Method validate roleCatalogue before save (create/update).
	 * 
	 * @param roleCatalogue
	 */
	private void validate(IdmRoleCatalogue roleCatalogue) {
		Assert.notNull(roleCatalogue, "Role catalogue is required.");
		//
		// test role catalogue to parent and children
		if (this.baseTreeService.validateTreeNodeParents(roleCatalogue)) {
			throw new TreeNodeException(CoreResultCode.ROLE_CATALOGUE_BAD_PARENT,  "Role catalog [" + roleCatalogue.getName() + "] have bad parent.");
		}
	}
	
	private IdmRoleCatalogueDto setForestIndex(IdmRoleCatalogueDto roleCatalogue, IdmForestIndexEntity index) {
		if (index != null) {
			roleCatalogue.setLft(index.getLft());
			roleCatalogue.setRgt(index.getRgt());
		}
		return roleCatalogue;
	}	

}
