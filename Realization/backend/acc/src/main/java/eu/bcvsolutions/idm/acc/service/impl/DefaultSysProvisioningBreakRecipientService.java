package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakRecipient;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakRecipientRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

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
}
