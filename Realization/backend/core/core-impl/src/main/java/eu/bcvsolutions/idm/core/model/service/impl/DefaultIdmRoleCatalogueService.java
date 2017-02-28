package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;

/**
 * Implementation of @IdmRoleCatalogueService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmRoleCatalogueService extends AbstractReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>  implements IdmRoleCatalogueService {
	
	private final IdmRoleCatalogueRepository roleCatalogueRepository;
	private final IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
	private final DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	
	@Autowired
	public DefaultIdmRoleCatalogueService(
			IdmRoleCatalogueRepository roleCatalogueRepository,
			DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService,
			IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository) {
		super(roleCatalogueRepository);
		//
		Assert.notNull(baseTreeService);
		Assert.notNull(roleCatalogueRoleRepository);
		//
		this.roleCatalogueRepository = roleCatalogueRepository;
		this.baseTreeService = baseTreeService;
		this.roleCatalogueRoleRepository = roleCatalogueRoleRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleCatalogue getByName(String name) {
		return roleCatalogueRepository.findOneByName(name);
	}
	
	@Override
	public IdmRoleCatalogue save(IdmRoleCatalogue entity) {
		// validate role
		this.validate(entity);
		return super.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(IdmRoleCatalogue roleCatalogue) {
		Assert.notNull(roleCatalogue);
		//
		if (!findChildrenByParent(roleCatalogue.getId()).isEmpty()) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of("roleCatalogue", roleCatalogue.getName()));
		}
		// remove row from intersection table
		roleCatalogueRoleRepository.deleteAllByRoleCatalogue(roleCatalogue);
		//
		super.delete(roleCatalogue);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findRoots() {
		return this.roleCatalogueRepository.findChildren(null, null).getContent();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findChildrenByParent(UUID parent) {
		return this.roleCatalogueRepository.findChildren(parent, null).getContent();
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
			roleCatalogues = this.findChildrenByParent(parent.getId());
		} else { // get roots
			roleCatalogues = this.findRoots();
		}
		//
		if (this.baseTreeService.validateUniqueName(roleCatalogues, roleCatalogue)) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_BAD_NICE_NAME, ImmutableMap.of("name", roleCatalogue.getName()));
		}
	}
	
}
