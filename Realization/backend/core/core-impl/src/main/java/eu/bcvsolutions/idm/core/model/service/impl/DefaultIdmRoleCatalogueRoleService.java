package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default implementation for {@link IdmRoleCatalogueRoleService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service("roleCatalogueRoleService")
public class DefaultIdmRoleCatalogueRoleService 
		extends AbstractReadWriteDtoService<IdmRoleCatalogueRoleDto,IdmRoleCatalogueRole, IdmRoleCatalogueRoleFilter> 
		implements IdmRoleCatalogueRoleService {
	
	private final IdmRoleCatalogueRoleRepository repository;
	
	@Autowired
	public DefaultIdmRoleCatalogueRoleService(
			IdmRoleCatalogueRoleRepository repository) {
		super(repository);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<IdmRoleCatalogueRole> findEntities(IdmRoleCatalogueRoleFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRole(UUID roleId) {
		return toDtos(repository.findAllByRole_Id(roleId), true);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		return toDtos(repository.findAllByRoleCatalogue_Id(roleCatalogueId), true);
	}
}
