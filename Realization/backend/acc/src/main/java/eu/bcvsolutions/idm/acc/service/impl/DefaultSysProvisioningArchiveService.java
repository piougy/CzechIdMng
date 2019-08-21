package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto.Builder;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive_;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Archived provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningArchiveService
		extends AbstractReadWriteDtoService<SysProvisioningArchiveDto, SysProvisioningArchive, SysProvisioningOperationFilter> 
		implements SysProvisioningArchiveService {
	
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private SysProvisioningAttributeService provisioningAttributeService;
	@Autowired private SysSystemService systemService;

	@Autowired
	public DefaultSysProvisioningArchiveService(SysProvisioningArchiveRepository repository) {
		super(repository);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.PROVISIONINGARCHIVE, getEntityClass());
	}
	
	@Override
	@Transactional
	public void deleteInternal(SysProvisioningArchiveDto dto) {
		Assert.notNull(dto);
		// delete attributes
		provisioningAttributeService.deleteAttributes(dto);
		//
		super.deleteInternal(dto);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW) // we want log in archive always
	public SysProvisioningArchiveDto archive(SysProvisioningOperationDto provisioningOperation) {
		Builder builder = new SysProvisioningArchiveDto.Builder(provisioningOperation);
		if(provisioningOperation.getSystemEntity() != null) {
			SysSystemEntityDto systemEntity =  DtoUtils.getEmbedded(provisioningOperation, SysProvisioningOperation_.systemEntity, (SysSystemEntityDto) null);
			if (systemEntity == null) {
				systemEntity = systemEntityService.get(provisioningOperation.getSystemEntity());
			}
			builder.setSystemEntityUid(systemEntity.getUid());
		}
		//
		SysProvisioningArchiveDto archive = builder.build();
		// preserve original operation creator
		archive.setCreator(provisioningOperation.getCreator());
		archive.setCreatorId(provisioningOperation.getCreatorId());
		archive.setOriginalCreator(provisioningOperation.getOriginalCreator());
		archive.setOriginalCreatorId(provisioningOperation.getOriginalCreatorId());
		// preserve original created => operation was created
		archive.setCreated(provisioningOperation.getCreated());
		// archive modified is used as the executed / canceled 
		archive.setModified(DateTime.now());
		// archive relation on the role-request
		archive.setRoleRequestId(provisioningOperation.getRoleRequestId());
		//
		archive = save(archive);
		//
		// log attributes used in provisioning context into provisioning attributes
		provisioningAttributeService.saveAttributes(archive);
		//
		return archive;
	}
	
	/**
	 * Optimize - system can be pre-loaded in DTO.
	 * 
	 * @param archive
	 * @return
	 */
	@Override
	public SysSystemDto getSystem(SysProvisioningArchiveDto archive) {
		SysSystemDto system = DtoUtils.getEmbedded(archive, SysProvisioningArchive_.system, (SysSystemDto) null);
		if (system == null) {
			// just for sure, self constructed operation can be given
			system = systemService.get(archive.getSystem());
		}
		//
		return system;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysProvisioningArchive> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysProvisioningOperationFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			throw new ResultCodeException(CoreResultCode.BAD_FILTER, "Filter by text is not supported.");
		}
		// System Id
		UUID systemId = filter.getSystemId();
		if (systemId != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.system).get(SysSystem_.id), systemId));
		}
		// From
		DateTime from = filter.getFrom();
		if (from != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(SysProvisioningArchive_.created), from));
		}
		// Till
		DateTime till = filter.getTill();
		if (till != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(SysProvisioningArchive_.created), till));
		}
		// Operation type
		ProvisioningEventType operationType = filter.getOperationType();
		if (operationType != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.operationType), operationType));
		}
		// Entity type
		SystemEntityType entityType = filter.getEntityType();
		if (entityType != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.entityType), entityType));
		}
		// Entity identifier
		UUID entityIdentifier = filter.getEntityIdentifier();
		if (entityIdentifier != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.entityIdentifier), entityIdentifier));
		}
		// System entity
		if (filter.getSystemEntity() != null) {
			throw new ResultCodeException(CoreResultCode.BAD_FILTER, "Filter by system entity identifier is not supported. Use system entity uid filter.");
		}
		// System entity UID
		String systemEntityUid = filter.getSystemEntityUid();
		if (StringUtils.isNotEmpty(systemEntityUid)) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.systemEntityUid), systemEntityUid));
		}
		// Operation result and his state
		OperationState resultState = filter.getResultState();
		if (resultState != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.result).get(OperationResultDto.PROPERTY_STATE), resultState));
		}
		// Batch id
		if (filter.getBatchId() != null) {
			throw new UnsupportedOperationException("Filter by batch identifier is not supported in archive.");
		}
		// Role-request ID
		UUID roleRequestId = filter.getRoleRequestId();
		if (roleRequestId != null) {
			predicates.add(builder.equal(root.get(SysProvisioningArchive_.roleRequestId), roleRequestId));
		}
		// updated attributes
		List<String> attributeUpdated = filter.getAttributeUpdated();
		if (!CollectionUtils.isEmpty(attributeUpdated)) {
			Subquery<SysProvisioningAttribute> subquery = query.subquery(SysProvisioningAttribute.class);
			Root<SysProvisioningAttribute> subRoot = subquery.from(SysProvisioningAttribute.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(SysProvisioningAttribute_.provisioningId), root.get(SysProvisioningArchive_.id)), // correlation attr
                    		subRoot.get(SysProvisioningAttribute_.name).in(attributeUpdated),
                    		builder.isFalse(subRoot.get(SysProvisioningAttribute_.removed))
                    		)
            );		
			predicates.add(builder.exists(subquery));
		}
		// removed attributes
		List<String> attributeRemoved = filter.getAttributeRemoved();
		if (!CollectionUtils.isEmpty(attributeRemoved)) {
			Subquery<SysProvisioningAttribute> subquery = query.subquery(SysProvisioningAttribute.class);
			Root<SysProvisioningAttribute> subRoot = subquery.from(SysProvisioningAttribute.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(SysProvisioningAttribute_.provisioningId), root.get(SysProvisioningArchive_.id)), // correlation attr
                    		subRoot.get(SysProvisioningAttribute_.name).in(attributeRemoved),
                    		builder.isTrue(subRoot.get(SysProvisioningAttribute_.removed))
                    		)
            );		
			predicates.add(builder.exists(subquery));
		}
		// empty provisioning
		Boolean emptyProvisioning = filter.getEmptyProvisioning();
		if (emptyProvisioning != null) {
			Subquery<SysProvisioningAttribute> subquery = query.subquery(SysProvisioningAttribute.class);
			Root<SysProvisioningAttribute> subRoot = subquery.from(SysProvisioningAttribute.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(builder.equal(subRoot.get(SysProvisioningAttribute_.provisioningId), root.get(SysProvisioningArchive_.id))) // correlation attr)
            );
			//
			Predicate provisioningPredicate = builder.exists(subquery); // has attributes
			if (emptyProvisioning) {
				provisioningPredicate = builder.and(
						builder.not(provisioningPredicate), // empty
						builder.notEqual(root.get(SysProvisioningArchive_.operationType), ProvisioningEventType.DELETE) // delete operations are not considered as empty
				);
			} else {
				// delete operations are not considered as empty or filled => show all time
				provisioningPredicate = builder.or(
						provisioningPredicate,
						builder.equal(root.get(SysProvisioningArchive_.operationType), ProvisioningEventType.DELETE)
				);
			}
			predicates.add(provisioningPredicate);
		}
		//
		return predicates;
	}
}
