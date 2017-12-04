package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
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
	private final IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
	private final DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	private final IdmRoleCatalogueForestContentService forestContentService;
	private final IdmConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public DefaultIdmRoleCatalogueService(
			IdmRoleCatalogueRepository repository,
			DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService,
			IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository,
			IdmRoleCatalogueForestContentService forestContentService,
			IdmConfigurationService configurationService,
			LongRunningTaskManager longRunningTaskManager,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		Assert.notNull(baseTreeService);
		Assert.notNull(roleCatalogueRoleRepository);
		Assert.notNull(forestContentService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		//
		this.repository = repository;
		this.baseTreeService = baseTreeService;
		this.roleCatalogueRoleRepository = roleCatalogueRoleRepository;
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
		this.validate(toEntity(roleCatalogue, repository.findOne(roleCatalogue.getId())));
		// update - we need to reindex first
		IdmForestIndexEntity index = forestContentService.updateIndex(IdmRoleCatalogue.FOREST_TREE_TYPE, roleCatalogue.getId(), roleCatalogue.getParent());
		roleCatalogue = super.saveInternal(roleCatalogue);
		return setForestIndex(roleCatalogue, index);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmRoleCatalogueDto roleCatalogue) {
		Page<IdmRoleCatalogue> nodes = repository.findChildren(roleCatalogue.getId(), new PageRequest(0, 1));
		if (nodes.getTotalElements() != 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of("roleCatalogue", roleCatalogue.getCode()));
		}
		// remove row from intersection table
		roleCatalogueRoleRepository.deleteAllByRoleCatalogue_Id(roleCatalogue.getId());
		//
		forestContentService.deleteIndex(roleCatalogue.getId());
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
		Assert.notNull(propertyName);
		//
		return String.format("%s%s", CONFIGURATION_PREFIX, propertyName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueDto> findAllByRole(UUID roleId) {
		List<IdmRoleCatalogue> roleCatalogues = new ArrayList<>();
		for (IdmRoleCatalogueRole roleCatalogueRole : roleCatalogueRoleRepository.findAllByRole_Id(roleId)) {
			roleCatalogues.add(roleCatalogueRole.getRoleCatalogue());
		}
		return toDtos(roleCatalogues, true);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueDto> findAllParents(UUID catalogueId) {
		IdmRoleCatalogue catalogue = this.getEntity(catalogueId);
		List<IdmRoleCatalogue> roleCatalogues = repository.findAllParents(catalogue, null);
		return toDtos(roleCatalogues, true);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleCatalogue> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleCatalogueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmRoleCatalogue_.name)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(root.get(IdmRoleCatalogue_.code)), "%" + filter.getText().toLowerCase() + "%")
							)
					);
		}
		if (filter.getCode() != null) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogue_.code), filter.getCode()));
		}
		if (filter.getName() != null) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogue_.name), filter.getName()));
		}
		if (filter.getParent() != null) {
			// recursively by default
			Subquery<IdmRoleCatalogue> subquery = query.subquery(IdmRoleCatalogue.class);
			Root<IdmRoleCatalogue> subRoot = subquery.from(IdmRoleCatalogue.class);
			subquery.select(subRoot);
			Path<IdmForestIndexEntity> forestIndexPath = subRoot.get(IdmRoleCatalogue_.forestIndex);
			subquery.where(builder.and(
				builder.equal(subRoot.get(AbstractEntity_.id), filter.getParent()),
				// This is here because of the structure of forest index. We need to select only subtree and not the element itself.
				// In order to do that, we must shrink the boundaries of query so it is true only for subtree of given node.
				// Remember that between clause looks like this a >= x <= b, where a and b are boundaries, in our case lft+1 and rgt-1.
				
				builder.between(root.get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft),
					builder.sum(forestIndexPath.get(IdmForestIndexEntity_.lft), 1L),
					builder.diff(forestIndexPath.get(IdmForestIndexEntity_.rgt), 1L))));
			predicates.add(builder.exists(subquery));
		}
		return predicates;
	}
	
	/**
	 * Method validate roleCatalogue before save (create/update).
	 * 
	 * @param roleCatalogue
	 */
	private void validate(IdmRoleCatalogue roleCatalogue) {
		Assert.notNull(roleCatalogue);
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
