package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakRecipient;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakRecipient_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakRecipientRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation for {@link SysProvisioningBreakRecipientService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningBreakRecipientService extends
		AbstractReadWriteDtoService<SysProvisioningBreakRecipientDto, SysProvisioningBreakRecipient, SysProvisioningBreakRecipientFilter>
		implements SysProvisioningBreakRecipientService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultSysProvisioningBreakRecipientService.class);

	
	private final IdmIdentityService identityService;
	
	@Autowired
	public DefaultSysProvisioningBreakRecipientService(
			SysProvisioningBreakRecipientRepository repository,
			IdmIdentityService identityService) {
		super(repository);
		//
		Assert.notNull(identityService);
		//
		this.identityService = identityService;
	}
	
	@Override
	public SysProvisioningBreakRecipientDto save(SysProvisioningBreakRecipientDto dto, BasePermission... permission) {
		if (dto.getRole() != null && dto.getIdentity() != null) {
			LOG.error("For recipient exists settings for role and identity. Allowed is only one property!");
			throw new ProvisioningException(AccResultCode.PROVISIONING_BREAK_RECIPIENT_CONFLICT);
		}
		return super.save(dto, permission);
	}

	@Override
	public List<SysProvisioningBreakRecipientDto> findAllByBreakConfig(UUID provisioningBreakConfig) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setBreakConfigId(provisioningBreakConfig);
		//
		return this.find(filter, null).getContent();
	}

	@Override
	public void deleteAllByBreakConfig(UUID provisioningBreakConfig) {
		for (SysProvisioningBreakRecipientDto recipient : findAllByBreakConfig(provisioningBreakConfig)) {
			this.delete(recipient);
		}
	}

	@Override
	public List<IdmIdentityDto> getAllRecipients(UUID provisioningBreakConfig) {
		List<IdmIdentityDto> recipients = new ArrayList<>();
		//
		for (SysProvisioningBreakRecipientDto recipient : findAllByBreakConfig(provisioningBreakConfig)) {
			if (recipient.getIdentity() != null) {
				IdmIdentityDto identityDto = identityService.get(recipient.getIdentity());
				if (identityDto != null) {
					recipients.add(identityDto);
				} else {
					LOG.error("Identity for id: [{}] was not found, please check provisionign break configuration id: [{}]", recipient.getIdentity(), recipient.getBreakConfig());
				}
			} else if (recipient.getRole() != null) {
				recipients.addAll(identityService.findAllByRole(recipient.getRole()));
			} else {
				LOG.error("Provisioning break recipient id: [{}] hasn't set role or identity. Provisioning break config: [{}]", recipient.getId(), recipient.getBreakConfig());
			}
		}
		//
		return recipients;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEM, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysProvisioningBreakRecipient> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, SysProvisioningBreakRecipientFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getBreakConfigId() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakRecipient_.breakConfig).get(AbstractEntity_.id), filter.getBreakConfigId()));
		}
		//
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakRecipient_.identity).get(AbstractEntity_.id), filter.getIdentityId()));
		}
		//
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakRecipient_.role).get(AbstractEntity_.id), filter.getRoleId()));
		}
		//
		return predicates;
	}
}
