package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.entity.AccTreeAccount_;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword_;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link AccUniformPasswordService}. The service is
 * used only for standard CRUD operations.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("accUniformPasswordService")
public class DefaultAccUniformPasswordService
		extends AbstractEventableDtoService<AccUniformPasswordDto, AccUniformPassword, AccUniformPasswordFilter>
		implements AccUniformPasswordService {

	private final AccUniformPasswordRepository repository;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccUniformPasswordSystemService uniformPasswordSystemService;

	@Autowired
	public DefaultAccUniformPasswordService(AccUniformPasswordRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);

		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.UNIFORMPASSWORD, getEntityClass());
	}

	@Override
	public AccUniformPasswordDto getByCode(String code) {
		return this.toDto(repository.findOneByCode(code));
	}

	@Override
	public List<AccPasswordChangeOptionDto> findOptionsForPasswordChange(IdmIdentityDto identity, BasePermission ...permissions) {
		List<AccPasswordChangeOptionDto> result = Lists.newArrayList();
		
		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter();
		filter.setIdentityId(identity.getId());
		filter.setUniformPasswordDisabled(Boolean.FALSE);
		List<AccUniformPasswordSystemDto> uniformPasswordSystems = this.uniformPasswordSystemService.find(filter, null).getContent();

		// Group uniform password system by uniform password definition
		Map<AccUniformPasswordDto, List<AccAccountDto>> accountsForUniformPassword = Maps.newHashMap();

		// Same behavior as previous versions
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setOwnership(Boolean.TRUE);
		accountFilter.setSupportChangePassword(Boolean.TRUE);
		accountFilter.setIdentityId(identity.getId());
		accountFilter.setInProtection(Boolean.FALSE);

		// Include given permissions
		List<AccAccountDto> accounts = accountService.find(accountFilter, null, permissions).getContent();
		for (AccAccountDto account : accounts) {

			// One system can be place more than one in uniform password systems
			List<AccUniformPasswordSystemDto> uniformBySystem = uniformPasswordSystems.stream().filter(pfs -> {
				return pfs.getSystem().equals(account.getSystem());
			}).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(uniformBySystem)) {
				// Simple account as option
				AccPasswordChangeOptionDto optionDto = new AccPasswordChangeOptionDto(account);
				optionDto.setNiceLabel(getNiceLabelForOption(account));
				result.add(optionDto);
				continue;
			}

			for (AccUniformPasswordSystemDto uniformPasswordSystemDto : uniformBySystem) {
				AccUniformPasswordDto definition = DtoUtils.getEmbedded(uniformPasswordSystemDto, AccUniformPasswordSystem_.uniformPassword, AccUniformPasswordDto.class, null);
				if (accountsForUniformPassword.containsKey(definition)) {
					accountsForUniformPassword.get(definition).add(account);
				} else {
					accountsForUniformPassword.put(definition, Lists.newArrayList(account));
				}
			}

		}

		// Check if exists account for uniform password and process options for them
		if (!accountsForUniformPassword.isEmpty()) {
			for (Entry<AccUniformPasswordDto, List<AccAccountDto>> entry : accountsForUniformPassword.entrySet()) {
				// There is also needed 
				AccUniformPasswordDto uniformPasswordDto = entry.getKey();
				AccPasswordChangeOptionDto optionDto = new AccPasswordChangeOptionDto(uniformPasswordDto, entry.getValue());
				optionDto.setNiceLabel(getNiceLabelForOption(uniformPasswordDto));
				optionDto.setChangeInIdm(uniformPasswordDto.isChangeInIdm());
				
				result.add(optionDto);
			}
		}

		return result;
	}

	/**
	 * Compose nice label for password option from {@link AccAccountDto}
	 *
	 * @param account
	 * @return
	 */
	private String getNiceLabelForOption(AccAccountDto account) {
		SysSystemDto systemDto = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class, null);
		if (systemDto != null) {
			return MessageFormat.format("{0} ({1})", systemDto.getCode(), account.getUid());
		}

		// Fallback for nice label
		return account.getUid();
	}

	/**
	 * Compose nice label for password option from {@link AccUniformPasswordDto}
	 *
	 * @param uniformPassword
	 * @return
	 */
	private String getNiceLabelForOption(AccUniformPasswordDto uniformPassword) {
		String description = uniformPassword.getDescription();
		if (StringUtils.isBlank(description)) {
			return MessageFormat.format("{0}", uniformPassword.getCode());
		} else {
			return MessageFormat.format("{0} ({1})", uniformPassword.getCode(), uniformPassword.getDescription());
		}
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccUniformPassword> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccUniformPasswordFilter filter) {

		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		if (filter.getChangeInIdM() != null) {
			predicates.add(builder.equal(root.get(AccUniformPassword_.changeInIdm), filter.getChangeInIdM()));
		}
		
		return predicates;
	}
}
