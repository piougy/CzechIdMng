package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

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
	
	@Autowired
	public DefaultIdmRoleCatalogueRoleService(
			IdmRoleCatalogueRoleRepository repository) {
		super(repository);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLECATALOGUEROLE, getEntityClass());
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRole(UUID roleId) {
		Assert.notNull(roleId);
		//
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleId(roleId);
		//
		return find(filter, null).getContent();
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		Assert.notNull(roleCatalogueId);
		//
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleCatalogueId(roleCatalogueId);
		//
		return find(filter, null).getContent();
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleCatalogueRole> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRoleCatalogueRoleFilter filter) {
		List<Predicate> predicates =  super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			throw new UnsupportedOperationException("Quisk test filter is not implemented for role catalogue relations.");
		}
		String roleCatalogueCode = filter.getRoleCatalogueCode();
		if (StringUtils.isNotEmpty(roleCatalogueCode)) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogueRole_.roleCatalogue).get(IdmRoleCatalogue_.code), roleCatalogueCode));
		}
		UUID roleCatalogueId = filter.getRoleCatalogueId();
		if (roleCatalogueId != null) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogueRole_.roleCatalogue).get(IdmRoleCatalogue_.id), roleCatalogueId));
		}
		UUID roleId = filter.getRoleId();
		if (roleId != null) {
			predicates.add(builder.equal(root.get(IdmRoleCatalogueRole_.role).get(IdmRoleCatalogue_.id), roleId));
		}
		//
		return predicates;
	}
}
