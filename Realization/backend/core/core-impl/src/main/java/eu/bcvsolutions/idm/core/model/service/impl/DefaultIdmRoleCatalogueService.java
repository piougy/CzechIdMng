package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RebuildRoleCatalogueIndexTaskExecutor;

/**
 * Implementation of @IdmRoleCatalogueService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmRoleCatalogueService extends AbstractReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>  implements IdmRoleCatalogueService {
	
	private final IdmRoleCatalogueRepository repository;
	private final IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
	private final DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	private final ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	private final IdmConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public DefaultIdmRoleCatalogueService(
			IdmRoleCatalogueRepository repository,
			DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService,
			IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository,
			ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService,
			IdmConfigurationService configurationService,
			LongRunningTaskManager longRunningTaskManager) {
		super(repository);
		//
		Assert.notNull(baseTreeService);
		Assert.notNull(roleCatalogueRoleRepository);
		Assert.notNull(forestIndexService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		//
		this.repository = repository;
		this.baseTreeService = baseTreeService;
		this.roleCatalogueRoleRepository = roleCatalogueRoleRepository;
		this.forestIndexService = forestIndexService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleCatalogue getByName(String name) {
		return repository.findOneByName(name);
	}
	
	@Override
	@Transactional
	public IdmRoleCatalogue save(IdmRoleCatalogue entity) {
		// validate role
		this.validate(entity);
		//
		if (isNew(entity)) {
			// create new
			return createIndex(super.save(entity));
		} else {
			// update - we need to reindex first
			return super.save(updateIndex(entity));
		}
	}
	
	@Override
	@Transactional
	public void delete(IdmRoleCatalogue roleCatalogue) {
		Assert.notNull(roleCatalogue);
		//
		if (findDirectChildren(roleCatalogue, new PageRequest(0, 1)).getTotalElements() != 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of("roleCatalogue", roleCatalogue.getName()));
		}
		// remove row from intersection table
		roleCatalogueRoleRepository.deleteAllByRoleCatalogue(roleCatalogue);
		//
		super.delete(deleteIndex(roleCatalogue));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogue> findRoots(Pageable pageable) {
		return repository.findRoots(pageable);
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
		//
		IdmRoleCatalogue parent = roleCatalogue.getParent();
		List<IdmRoleCatalogue> roleCatalogues = null;
		if (parent != null) { // get same level
			roleCatalogues = this.findDirectChildren(parent, null).getContent();
		} else { // get roots
			roleCatalogues = this.findRoots(null).getContent();
		}
		//
		if (this.baseTreeService.validateUniqueName(roleCatalogues, roleCatalogue)) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_BAD_NICE_NAME, ImmutableMap.of("name", roleCatalogue.getName()));
		}
	}

	@Override
	public void rebuildIndexes(String forestTreeType) {
		rebuildIndexes();
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
	@Transactional
	public IdmRoleCatalogue createIndex(IdmRoleCatalogue content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public IdmRoleCatalogue updateIndex(IdmRoleCatalogue content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public IdmRoleCatalogue deleteIndex(IdmRoleCatalogue content) {
		return forestIndexService.dropIndex(content);
	}

	/**
	 * Role catalogue has only one static type 
	 * 
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogue> findRoots(String forestTreeType, Pageable pageable) {
		return repository.findRoots(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogue> findDirectChildren(IdmRoleCatalogue parent, Pageable pageable) {
		return repository.findDirectChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleCatalogue> findAllChildren(IdmRoleCatalogue parent, Pageable pageable) {
		return repository.findAllChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findAllParents(IdmRoleCatalogue content, Sort sort) {
		return repository.findAllParents(content, sort);
	}
	
	@Override
	public String getConfigurationPropertyName(String propertyName) {
		Assert.notNull(propertyName);
		//
		return String.format("%s%s", CONFIGURATION_PREFIX, propertyName);
	}
	
}
