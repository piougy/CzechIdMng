package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysRoleSystemService extends AbstractReadWriteDtoService<SysRoleSystemDto, SysRoleSystem, SysRoleSystemFilter> implements SysRoleSystemService {

	private final AccIdentityAccountRepository identityAccountRepository;
	private final IdmRoleService roleService;
	@Autowired
	private RequestManager requestManager;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	
	@Autowired
	public DefaultSysRoleSystemService(
			SysRoleSystemRepository repository,
			SysRoleSystemAttributeRepository roleSystemAttributeRepository, // Since 9.5.0 isn't repository needed
			AccIdentityAccountRepository identityAccountRepository,
			IdmRoleService roleService) {
		super(repository);
		//
		Assert.notNull(identityAccountRepository);
		Assert.notNull(roleService);
		//
		this.identityAccountRepository = identityAccountRepository;
		this.roleService = roleService;
	}
	
	@Override
	@Transactional
	public void delete(SysRoleSystemDto roleSystem, BasePermission... permission) {
		Assert.notNull(roleSystem);
		SysRoleSystem roleSystemEntity = this.getEntity(roleSystem.getId());
		//
		// delete attributes
		SysRoleSystemAttributeFilter filter = new SysRoleSystemAttributeFilter();
		filter.setRoleSystemId(roleSystem.getId());
		List<SysRoleSystemAttributeDto> attributes = roleSystemAttributeService.find(filter, null).getContent();
		// We must delete attribute against service NOT repository. Historical controlled values are created by service.
		for (SysRoleSystemAttributeDto attribute : attributes) {
			roleSystemAttributeService.delete(attribute);
		}
		//
		// clear identityAccounts - only link on roleSystem
		identityAccountRepository.clearRoleSystem(roleSystemEntity);
		//
		// Cancel requests and request items using that deleting DTO
		requestManager.onDeleteRequestable(roleSystem);
		
		super.delete(roleSystem, permission);
	}
	
	@Override
	public SysRoleSystemDto save(SysRoleSystemDto dto, BasePermission... permission) {
		Assert.notNull(dto, "RoleSystem cannot be null!");
		Assert.notNull(dto.getRole(), "Role cannot be null!");
		Assert.notNull(dto.getSystem(), "System cannot be null!");
		Assert.notNull(dto.getSystemMapping(), "System mapping cannot be null!");
		
		// Only Identity supports ACM by role
		SysSystemMappingDto systemMappingDto = systemMappingService.get(dto.getSystemMapping());
		if(systemMappingDto != null && SystemEntityType.IDENTITY != systemMappingDto.getEntityType()) {
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_SUPPORTS_ONLY_IDENTITY,
					ImmutableMap.of("entityType", systemMappingDto.getEntityType().name()));
		}
		
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
	    filter.setRoleId(dto.getRole());
	    filter.setSystemId(dto.getSystem());
	    
	    List<SysRoleSystemDto> roleSystems = this.find(filter, null).getContent();
		boolean isDuplicated = roleSystems.stream().filter(roleSystem -> {
			return !roleSystem.getId().equals(dto.getId());
		}).findFirst().isPresent();
		
		if(isDuplicated){
			IdmRoleDto roleDto = roleService.get(dto.getRole());
			SysSystemDto systemDto = DtoUtils.getEmbedded(dto, SysRoleSystem_.system);
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_ALREADY_EXISTS,
					ImmutableMap.of("role", roleDto.getCode(), "system", systemDto.getName()));
		}
	    
		return super.save(dto, permission);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysRoleSystem> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			SysRoleSystemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		
		if (filter.getSystemMappingId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.systemMapping).get(SysSystemMapping_.id), filter.getSystemMappingId()));
		}
		
		// Return role-system where is uses given attribute mapping
		if (filter.getAttributeMappingId() != null) {
			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);

			subquery.where(builder.and( //
					builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem),root), // Correlation attribute
					builder.equal(subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping).get(AbstractEntity_.id), filter.getAttributeMappingId())));

			predicates.add(builder.exists(subquery));
		}
		return predicates;
	}
	
}
