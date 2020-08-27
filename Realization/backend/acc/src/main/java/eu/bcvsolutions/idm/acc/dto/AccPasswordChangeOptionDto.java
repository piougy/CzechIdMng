package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.google.common.collect.Lists;

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

		this.id = uniformPassword.getId();

		this.accounts = Lists.newArrayListWithExpectedSize(accounts.size());
		for (AccAccountDto account : accounts) {
			this.accounts.add(account.getId().toString());
		}
	}

	public AccPasswordChangeOptionDto(AccAccountDto account) {
		super();

		this.id = account.getId();

		this.accounts = Lists.newArrayListWithExpectedSize(0);
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
