package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.core.Relation;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Password change option that unite all {@link AccAccountDto} by {@link AccUniformPasswordDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Relation(collectionRelation = "uniformPasswordOptions")
public class AccPasswordChangeOptionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private UUID id; // Id there exists as unique key for frontend selecbox. ID can contains id of accounts or id of uniform password
	private String niceLabel;
	private List<String> accounts;
	private boolean changeInIdm;

	public AccPasswordChangeOptionDto() {
		super();
	}

	public AccPasswordChangeOptionDto(AccUniformPasswordDto uniformPassword, List<AccAccountDto> accounts) {
		super();

		this.setNiceLabel(uniformPassword);
		this.id = uniformPassword.getId();

		this.accounts = Lists.newArrayList();
		for (AccAccountDto account : accounts) {
			this.accounts.add(account.getId().toString());
		}
	}

	public AccPasswordChangeOptionDto(AccAccountDto account) {
		super();

		this.setNiceLabel(account);

		this.id = account.getId();

		this.accounts = Lists.newArrayList();
		this.accounts.add(account.getId().toString());
	}
	
	public List<String> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}

	public String getNiceLabel() {
		return this.niceLabel;
	}

	public void setNiceLabel(String niceLabel) {
		this.niceLabel = niceLabel;
	}

	public void setNiceLabel(AccAccountDto account) {
		SysSystemDto systemDto = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class, null);
		if (systemDto != null) {
			this.niceLabel = MessageFormat.format("{0} ({1})", systemDto.getCode(), account.getUid());
			return;
		}

		// Fallback for nice label
		this.niceLabel = account.getUid();
	}

	public void setNiceLabel(AccUniformPasswordDto uniformPassword) {
		String description = uniformPassword.getDescription();
		if (StringUtils.isBlank(description)) {
			this.niceLabel = MessageFormat.format("{0}", uniformPassword.getCode());
		} else {
			this.niceLabel = MessageFormat.format("{0} ({1})", uniformPassword.getCode(), uniformPassword.getDescription());
		}
	}

	public boolean isChangeInIdm() {
		return changeInIdm;
	}

	public void setChangeInIdm(boolean changeInIdm) {
		this.changeInIdm = changeInIdm;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
