package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncTreeConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSyncContractConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncContractConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSyncIdentityConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog_;
import eu.bcvsolutions.idm.acc.entity.SysSyncTreeConfig;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.ExportDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Default synchronization config service
 * 
 * @author svandav
 * @author Radek Tomiška
 * @author Ondrej Husnik
 */
@Service
public class DefaultSysSyncConfigService
		extends AbstractReadWriteDtoService<AbstractSysSyncConfigDto, SysSyncConfig, SysSyncConfigFilter>
		implements SysSyncConfigService {

	private final SysSyncConfigRepository repository;
	private final SysSyncLogService syncLogService;

	@Autowired
	public DefaultSysSyncConfigService(SysSyncConfigRepository repository, SysSyncLogService synchronizationLogService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService, "Service is required.");
		//
		this.repository = repository;
		this.syncLogService = synchronizationLogService;
	}

	@Override
	@Transactional
	public AbstractSysSyncConfigDto saveInternal(AbstractSysSyncConfigDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		if (dto != null && !this.isNew(dto)) {
			AbstractSysSyncConfigDto persistedConfig = this.get(dto.getId());
			if (!dto.getClass().equals(persistedConfig.getClass())) {
				throw new ResultCodeException(AccResultCode.SYNCHRONIZATION_CONFIG_TYPE_CANNOT_BE_CANGED,
						ImmutableMap.of("old", persistedConfig.getClass().getSimpleName(), "new",
								dto.getClass().getSimpleName()));
			}
		}
		return super.saveInternal(dto);
	}
	
	@Override
	protected AbstractSysSyncConfigDto toDto(SysSyncConfig entity, AbstractSysSyncConfigDto dto,
			SysSyncConfigFilter filter) {
		AbstractSysSyncConfigDto result = super.toDto(entity, dto, filter);
		// If filter has set "Include last sync log", then we try to find last created
		// log and add it to the sync configuration DTO (for show statistics and results
		// in the table of sync -> UX)
		if (filter != null && filter.getIncludeLastLog() != null && filter.getIncludeLastLog()) {
			Assert.notNull(result.getId(), "Result identifier is required.");

			SysSyncLogFilter syncLogFilter = new SysSyncLogFilter();
			syncLogFilter.setSynchronizationConfigId(result.getId());
			List<SysSyncLogDto> logs = syncLogService
					.find(syncLogFilter, PageRequest.of(0, 1, Direction.DESC, SysSyncLog_.created.getName()))
					.getContent();
			if (!logs.isEmpty()) {
				result.setLastSyncLog(logs.get(0));
			}
		}

		return result;
	}
	
	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}

	@Override
	protected Class<? extends SysSyncConfig> getEntityClass(AbstractSysSyncConfigDto dto) {
		if (dto instanceof SysSyncContractConfigDto) {
			return SysSyncContractConfig.class;
		}
		if (dto instanceof SysSyncIdentityConfigDto) {
			return SysSyncIdentityConfig.class;
		}
		if (dto instanceof SysSyncTreeConfigDto) {
			return SysSyncTreeConfig.class;
		}
		return SysSyncConfig.class;
	}

	@Override
	protected Class<? extends AbstractSysSyncConfigDto> getDtoClass(SysSyncConfig entity) {
		if (entity instanceof SysSyncContractConfig) {
			return SysSyncContractConfigDto.class;
		}
		if (entity instanceof SysSyncIdentityConfig) {
			return SysSyncIdentityConfigDto.class;
		}
		if (entity instanceof SysSyncTreeConfig) {
			return SysSyncTreeConfigDto.class;
		}
		return SysSyncConfigDto.class;
	}

	@Override
	@Transactional
	public void deleteInternal(AbstractSysSyncConfigDto synchronizationConfig) {
		Assert.notNull(synchronizationConfig, "Synchronization configuration is required.");
		//
		// remove all synchronization logs
		SysSyncLogFilter filter = new SysSyncLogFilter();
		filter.setSynchronizationConfigId(synchronizationConfig.getId());
		syncLogService.find(filter, null).forEach(log -> {
			syncLogService.delete(log);
		});
		//
		super.deleteInternal(synchronizationConfig);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysSyncConfig> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysSyncConfigFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
					builder.like(builder.lower(
							root
								.get(SysSyncConfig_.systemMapping)
								.get(SysSystemMapping_.objectClass)
								.get(SysSchemaObjectClass_.system)
								.get(SysSystem_.name)),
							"%" + text + "%"),
					builder.like(builder.lower(root.get(SysSyncConfig_.name)), "%" + text + "%")
					));
		}
		// id 
		UUID systemId = filter.getSystemId();
		if (systemId != null) {
			predicates.add(
					builder.equal(
							root
								.get(SysSyncConfig_.systemMapping)
								.get(SysSystemMapping_.objectClass)
								.get(SysSchemaObjectClass_.system)
								.get(SysSystem_.id),
								systemId));
		}
		// name
		String name = filter.getName();
		if (StringUtils.isNotEmpty(name)) {
			predicates.add(builder.equal(root.get(SysSyncConfig_.name), name));
		}
		// differentialSync
		Boolean differentialSync = filter.getDifferentialSync();
		if(differentialSync != null) {
			predicates.add(builder.equal(root.get(SysSyncConfig_.differentialSync), differentialSync));
		}
		// systemMappingId
		UUID systemMappingId = filter.getSystemMappingId();
		if (systemMappingId != null) {
			predicates.add(
					builder.equal(
							root.get(SysSyncConfig_.systemMapping)
								.get(SysSystemMapping_.id), 
								systemMappingId));
		}
		//
		return predicates;
	}

	@Override
	public boolean isRunning(AbstractSysSyncConfigDto config) {
		if (config == null) {
			return false;
		}
		int count = ((SysSyncConfigRepository) this.getRepository())
				.runningCount(((SysSyncConfigRepository) this.getRepository()).findById(config.getId()).get());
		return count > 0;
	}

	@Override
	public AbstractSysSyncConfigDto clone(UUID id) {
		AbstractSysSyncConfigDto original = this.get(id);
		Assert.notNull(original, "Config of synchronization must be found!");

		// We do detach this entity (and set id to null)
		original.setId(null);
		DtoUtils.clearAuditFields(original);
		return original;
	}

	@Override
	public Long countBySystemMapping(SysSystemMappingDto mapping) {
		Assert.notNull(mapping, "Mapping is required.");
		Assert.notNull(mapping.getId(), "Mapping identifier is required.");
		return repository.countByCorrelationAttribute_Id(mapping.getId());
	}

	@Override
	protected AbstractSysSyncConfigDto internalExport(UUID id) {
		// For searching tree-type by code, have to be tree-type DTO embedded.
		AbstractSysSyncConfigDto dto = this.get(id);
		if (dto instanceof SysSyncContractConfigDto && ((SysSyncContractConfigDto)dto).getDefaultTreeType() != null) {
			AbstractDto treeType = (AbstractDto) dto.getEmbedded().get(SysSyncContractConfig_.defaultTreeType.getName());
			AbstractDto treeNode = (AbstractDto) dto.getEmbedded().get(SysSyncContractConfig_.defaultTreeNode.getName());
			dto.getEmbedded().clear();
			// Put tree-type to the node embedded (tree-type will be use for findByExample).
			if (treeNode != null) {
				treeNode.getEmbedded().put(SysSyncContractConfig_.defaultTreeType.getName(), treeType);
				dto.getEmbedded().put(SysSyncContractConfig_.defaultTreeType.getName(), treeType);
			}
			dto.getEmbedded().put(SysSyncContractConfig_.defaultTreeNode.getName(), treeNode);
		}
		return dto;
	}

	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		super.export(id, batch);
		AbstractSysSyncConfigDto dto = this.get(id);
		if ( dto == null) {
			return;
		}
		ExportDescriptorDto descriptorDto = getExportManager().getDescriptor(batch, dto.getClass());
		if (descriptorDto != null) {
			// Token will be excluded for the export. It means a token on the target IdM will be not changed.
			// Or will be sets to the null, if sync does not exist yet.
			descriptorDto.getExcludedFields().add(SysSyncConfig_.token.getName());
			if (dto instanceof  SysSyncContractConfigDto) {
				// Tree-type will be searching by code (advanced paring by treeType field)
				descriptorDto.getAdvancedParingFields().add(SysSyncContractConfig_.defaultTreeType.getName());
				// Tree-type will be searching by code (advanced paring by treeNode field)
				descriptorDto.getAdvancedParingFields().add(SysSyncContractConfig_.defaultTreeNode.getName());
			}
		}



	}
}
