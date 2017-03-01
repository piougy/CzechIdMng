package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueRoleService;

/**
 * Default implementation for {@link IdmRoleCatalogueRoleService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmRoleCatalogueRoleService extends AbstractReadWriteEntityService<IdmRoleCatalogueRole, RoleCatalogueRoleFilter> implements IdmRoleCatalogueRoleService {
	
	private final IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
	
	@Autowired
	public DefaultIdmRoleCatalogueRoleService(
			IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository) {
		super(roleCatalogueRoleRepository);
		//
		Assert.notNull(roleCatalogueRoleRepository);
		//
		this.roleCatalogueRoleRepository = roleCatalogueRoleRepository;
	}

	@Override
	@Transactional
	public IdmRoleCatalogueRole save(IdmRoleCatalogueRole entity) {
		return this.roleCatalogueRoleRepository.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(IdmRoleCatalogueRole entity) {
		this.roleCatalogueRoleRepository.delete(entity);
	}

	@Override
	public List<IdmRoleCatalogueRole> getRoleCatalogueRoleByRole(IdmRole role) {
		return roleCatalogueRoleRepository.findAllByRole(role);
	}

	@Override
	public List<IdmRoleCatalogueRole> getRoleCatalogueRoleByCatalogue(IdmRoleCatalogue roleCatalogue) {
		return roleCatalogueRoleRepository.findAllByRoleCatalogue(roleCatalogue);
	}

	@Override
	public List<IdmRole> getRoleByRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
		List<IdmRole> roles = new ArrayList<>();
		for (IdmRoleCatalogueRole roleCatalogueRole : this.getRoleCatalogueRoleByCatalogue(roleCatalogue)) {
			roles.add(roleCatalogueRole.getRole());
		}
		return roles;
	}

	@Override
	public List<IdmRoleCatalogue> getRoleCatalogueByRole(IdmRole role) {
		List<IdmRoleCatalogue> roleCatalogues = new ArrayList<>();
		for (IdmRoleCatalogueRole roleCatalogueRole : this.getRoleCatalogueRoleByRole(role)) {
			roleCatalogues.add(roleCatalogueRole.getRoleCatalogue());
		}
		return roleCatalogues;
	}

}
