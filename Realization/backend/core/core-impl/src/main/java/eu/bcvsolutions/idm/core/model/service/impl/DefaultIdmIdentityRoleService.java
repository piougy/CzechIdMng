package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventProcessorManager;
import eu.bcvsolutions.idm.core.model.dto.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Operations with identity roles - usable in wf
 * 
 * @author svanda
 *
 */
@Service
public class DefaultIdmIdentityRoleService extends AbstractReadWriteEntityService<IdmIdentityRole, IdentityRoleFilter>
		implements IdmIdentityRoleService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityRoleService.class);

	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmRoleRepository roleRepository;
	private final IdmIdentityRepository identityRepository;
	private final EntityEventProcessorManager entityEventProcessorService;

	@Autowired
	public DefaultIdmIdentityRoleService(
			IdmIdentityRoleRepository identityRoleRepository,
			IdmRoleRepository roleRepository,
			IdmIdentityRepository identityRepository,
			EntityEventProcessorManager entityEventProcessorService) {
		super(identityRoleRepository);
		//
		Assert.notNull(roleRepository);
		Assert.notNull(identityRepository);
		Assert.notNull(entityEventProcessorService);
		//
		this.identityRoleRepository = identityRoleRepository;
		this.roleRepository = roleRepository;
		this.identityRepository = identityRepository;
		this.entityEventProcessorService = entityEventProcessorService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityRole> getByIds(List<String> ids) {
		if (ids == null) {
			return null;
		}
		List<IdmIdentityRole> idmRoles = new ArrayList<>();
		for (String id : ids) {
			idmRoles.add(get(id));
		}
		return idmRoles;
	}

	@Override
	@Transactional
	public IdmIdentityRole updateByDto(String id, IdmIdentityRoleDto dto) {
		Assert.notNull(id);
		Assert.notNull(dto);

		IdmIdentityRole identityRole = identityRoleRepository.findOne(UUID.fromString(id));
		return this.save(toEntity(dto, identityRole));
	}

	@Override
	@Transactional
	public IdmIdentityRole addByDto(IdmIdentityRoleDto dto) {
		Assert.notNull(dto);
		//
		IdmIdentityRole identityRole = new IdmIdentityRole();
		return this.save(toEntity(dto, identityRole));
	}

	@Override
	@Transactional
	public IdmIdentityRole save(IdmIdentityRole entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getRole());
		Assert.notNull(entity.getIdentity());
		//
		LOG.debug("Saving role [{}] for identity [{}]", entity.getRole().getName(), entity.getIdentity().getUsername());
		return entityEventProcessorService.process(new IdentityRoleEvent(IdentityRoleEventType.SAVE, entity)).getContent();
	}

	@Override
	@Transactional
	public void delete(IdmIdentityRole entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getRole());
		Assert.notNull(entity.getIdentity());
		//
		LOG.debug("Deleting role [{}] for identity [{}]", entity.getRole().getName(), entity.getIdentity().getUsername());
		entityEventProcessorService.process(new IdentityRoleEvent(IdentityRoleEventType.DELETE, entity));
	}

	private IdmIdentityRole toEntity(IdmIdentityRoleDto identityRoleDto, IdmIdentityRole identityRole) {
		if (identityRoleDto == null || identityRole == null) {
			return null;
		}
		IdmRole role = null;
		IdmIdentity identity = null;
		if (identityRoleDto.getRole() != null) {
			role = roleRepository.findOne(identityRoleDto.getRole());
		}
		if (identityRoleDto.getIdentity() != null) {
			identity = identityRepository.findOne(identityRoleDto.getIdentity());
		}

		identityRole.setRole(role);
		identityRole.setIdentity(identity);
		identityRole.setValidFrom(identityRoleDto.getValidFrom());
		identityRole.setValidTill(identityRoleDto.getValidTill());
		identityRole.setOriginalCreator(identityRoleDto.getOriginalCreator());
		identityRole.setOriginalModifier(identityRoleDto.getOriginalModifier());
		return identityRole;
	}
}
