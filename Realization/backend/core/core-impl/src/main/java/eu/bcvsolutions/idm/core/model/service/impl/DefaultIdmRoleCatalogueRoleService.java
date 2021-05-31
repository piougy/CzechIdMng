package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
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
	private IdmRoleCatalogueService roleCatalogueService;
	
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
		Assert.notNull(roleId, "Role identifier is required.");
		//
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleId(roleId);
		//
		return find(filter, null).getContent();
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogueRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		Assert.notNull(roleCatalogueId, "Role catalogue identifier is required.");
		//
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleCatalogueId(roleCatalogueId);
		//
		return find(filter, null).getContent();
	}
	
	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Batch cannot be null!");
		
		IdmRoleCatalogueRoleDto catalogRole = this.get(id);
		if (catalogRole != null) {
			UUID roleCatalogue = catalogRole.getRoleCatalogue();
			if (roleCatalogue != null) {
				roleCatalogueService.export(roleCatalogue, batch);
			}
		}
		super.export(id, batch);
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
