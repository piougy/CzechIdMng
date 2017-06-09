package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueRoleService;

/**
 * Default implementation for {@link IdmRoleCatalogueRoleService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service("rRoleCatalogueRoleService")
public class DefaultIdmRoleCatalogueRoleService 
		extends AbstractReadWriteDtoService<IdmRoleCatalogueRoleDto,IdmRoleCatalogueRole, RoleCatalogueRoleFilter> 
		implements IdmRoleCatalogueRoleService {
	
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
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRole(UUID roleId) {
		return toDtos(roleCatalogueRoleRepository.findAllByRole_Id(roleId), true);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		return toDtos(roleCatalogueRoleRepository.findAllByRoleCatalogue_Id(roleCatalogueId), true);
	}
}
