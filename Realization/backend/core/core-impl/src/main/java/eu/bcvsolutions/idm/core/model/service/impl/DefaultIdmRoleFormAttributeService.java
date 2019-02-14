package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleFormAttributeRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for relation between role and definition of form-attribution. Is
 * elementary part of role form "sub-definition".
 * 
 * @author Vít Švanda
 *
 */
@Service("roleFormAttributeService")
public class DefaultIdmRoleFormAttributeService
		extends AbstractEventableDtoService<IdmRoleFormAttributeDto, IdmRoleFormAttribute, IdmRoleFormAttributeFilter>
		implements IdmRoleFormAttributeService {
	
	@Autowired @Lazy
	private IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultIdmRoleFormAttributeService(IdmRoleFormAttributeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLEFORMATTRIBUTE, getEntityClass());
	}
	
	@Override
	public IdmRoleFormAttributeDto validateDto(IdmRoleFormAttributeDto dto) {
		dto = super.validateDto(dto);
		// validate overridable attribute setting
		IdmFormAttributeDto formAttribute = formAttributeService.get(dto.getFormAttribute());
		formAttribute.setRequired(dto.isRequired());
		formAttribute.setUnique(dto.isUnique());
		formAttribute.setMax(dto.getMax());
		formAttribute.setMin(dto.getMin());
		formAttribute.setRegex(dto.getRegex());
		formAttributeService.validateDto(formAttribute);

		return dto;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleFormAttribute> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRoleFormAttributeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		// Role
		UUID role = filter.getRole();
		if (role != null) {
			predicates.add(builder.equal(root.get(IdmRoleFormAttribute_.role).get(IdmRole_.id), role));
		}
		// Form definition
		UUID definition = filter.getFormDefinition();
		if (definition != null) {
			predicates.add(builder.equal(root.get(IdmRoleFormAttribute_.formAttribute)
					.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), definition));
		}
		// Form attribute
		UUID attribute = filter.getFormAttribute();
		if (attribute != null) {
			predicates.add(
					builder.equal(root.get(IdmRoleFormAttribute_.formAttribute).get(IdmFormAttribute_.id), attribute));
		}
		return predicates;
	}

	@Override
	public IdmRoleFormAttributeDto addAttributeToSubdefintion(IdmRoleDto role, IdmFormAttributeDto attribute,  BasePermission... permission) {
		Assert.notNull(role);
		Assert.notNull(attribute);
		
		IdmRoleFormAttributeDto roleFormAttributeDto = new IdmRoleFormAttributeDto();
		roleFormAttributeDto.setRole(role.getId());
		roleFormAttributeDto.setFormAttribute(attribute.getId());
		roleFormAttributeDto.setDefaultValue(attribute.getDefaultValue());
		roleFormAttributeDto.setRequired(attribute.isRequired());
		
		return this.save(roleFormAttributeDto, permission);
	}
}
