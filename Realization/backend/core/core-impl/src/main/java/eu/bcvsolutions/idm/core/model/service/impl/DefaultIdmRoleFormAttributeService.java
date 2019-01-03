package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleFormAttributeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for relation between role and definition of form-attribution. Is
 * elementary part of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
@Service("roleFormAttributeService")
public class DefaultIdmRoleFormAttributeService
		extends AbstractEventableDtoService<IdmRoleFormAttributeDto, IdmRoleFormAttribute, IdmRoleFormAttributeFilter>
		implements IdmRoleFormAttributeService {

	@Autowired
	public DefaultIdmRoleFormAttributeService(IdmRoleFormAttributeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return null;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleFormAttribute> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRoleFormAttributeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// role
		UUID role = filter.getRole();
		if (role != null) {
			predicates.add(builder.equal(root.get(IdmRoleFormAttribute_.role).get(IdmRole_.id), role));
		}
		// form definition
		UUID definition = filter.getFormDefinition();
		if (definition != null) {
			predicates.add(builder.equal(root.get(IdmRoleFormAttribute_.formAttribute)
					.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), definition));
		}
		return predicates;
	}
}
