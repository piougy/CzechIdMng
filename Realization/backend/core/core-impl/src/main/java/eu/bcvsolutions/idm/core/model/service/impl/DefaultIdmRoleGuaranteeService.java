package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ExportDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role guarantees
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleGuaranteeService 
		extends AbstractEventableDtoService<IdmRoleGuaranteeDto, IdmRoleGuarantee, IdmRoleGuaranteeFilter> 
		implements IdmRoleGuaranteeService {
	
	@Autowired
	public DefaultIdmRoleGuaranteeService(IdmRoleGuaranteeRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLEGUARANTEE, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmRoleGuaranteeDto> findByRole(UUID roleId, Pageable pageable, BasePermission... permission) {
		Assert.notNull(roleId, "Role identifier is required.");
		//
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter();
		filter.setRole(roleId);
		//
		return find(filter, pageable, permission);
	}
	
	@Override
	protected IdmRoleGuaranteeDto internalExport(UUID id) {
		IdmRoleGuaranteeDto dto = this.get(id);

		// Advanced pairing
		// We cannot clear all embedded data, because we need to export DTO for
		// connected guarantee.
		BaseDto guaranteeDto = dto.getEmbedded().get(IdmRoleGuarantee_.guarantee.getName());
		dto.getEmbedded().clear();
		dto.getEmbedded().put(IdmRoleGuarantee_.guarantee.getName(), guaranteeDto);

		return dto;
	}
	
	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Export batch must exist!");
		super.export(id, batch);
		
		// Advanced pairing
		ExportDescriptorDto descriptorDto = getExportManager().getDescriptor(batch, this.getDtoClass());
		descriptorDto.getAdvancedParingFields().add(IdmRoleGuarantee_.guarantee.getName());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleGuaranteeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// role
		UUID role = filter.getRole();
		if (role != null) {
			predicates.add(builder.equal(root.get(IdmRoleGuarantee_.role).get(IdmRole_.id), role));
		}
		//
		// guarantee
		UUID guarantee = filter.getGuarantee();
		if (guarantee != null) {
			predicates.add(builder.equal(root.get(IdmRoleGuarantee_.guarantee).get(IdmIdentity_.id), guarantee));
		}	
		// type
		String type = filter.getType();
		if (StringUtils.isNotEmpty(type)) {
			predicates.add(builder.equal(root.get(IdmRoleGuarantee_.type), type));
		}	
		//
		return predicates;
	}
}
