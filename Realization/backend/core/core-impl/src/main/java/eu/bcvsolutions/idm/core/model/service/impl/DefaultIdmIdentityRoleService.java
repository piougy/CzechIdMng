package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityRoleService;

@Service
public class DefaultIdmIdentityRoleService implements IdmIdentityRoleService {

	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	@Autowired
	private IdmRoleRepository roleRepository;
	@Autowired
	private IdmIdentityRepository identityRepository;

	@Override
	public IdmIdentityRole get(UUID id) {
		IdmIdentityRole entity = identityRoleRepository.findOne(id);
		return entity;
	}

	@Override
	public List<IdmIdentityRole> getByIds(List<String> ids) {
		if (ids == null) {
			return null;
		}
		List<IdmIdentityRole> idmRoles = new ArrayList<>();
		for (String id : ids) {
			idmRoles.add(get(UUID.fromString(id)));
		}
		return idmRoles;
	}
	
	@Override
	public IdmIdentityRole updateByDto(UUID id, IdmIdentityRoleDto dto){
		Assert.notNull(id);
		Assert.notNull(dto);
		
		IdmIdentityRole identityRole = identityRoleRepository.findOne(id);
		return identityRoleRepository.save(toEntity(dto, identityRole));
	}
	
	@Override
	public IdmIdentityRole addByDto(IdmIdentityRoleDto dto){
		Assert.notNull(dto);
		
		IdmIdentityRole identityRole = new IdmIdentityRole();
		return identityRoleRepository.save(toEntity(dto, identityRole));
	}
	
	@Override
	public void delete(UUID id){
		Assert.notNull(id);
		identityRoleRepository.delete(id);
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
