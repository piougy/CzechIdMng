package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Wrapper for store echo in password filter. Wrapper also contains two flags {@link AccPasswordFilterEchoItemDto#valid} and {@link AccPasswordFilterEchoItemDto#changed}
 * these two flag is for check validation and already changed password.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccPasswordFilterEchoItemDto implements BaseDto {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private String password;
	private ZonedDateTime validateDate; // Datetime when password was validated
	private ZonedDateTime changeDate; // Datetime when password was changed - echo is counted from the time
	private UUID accountId;
	private boolean validityChecked = false; // Flag for password that was already successfully validate
	private boolean changed = false; // Flag for password that was already successfully processed by change operation in IdM

	public AccPasswordFilterEchoItemDto() {
		super();
	}

	public AccPasswordFilterEchoItemDto(String password, UUID accountId) {
		super();
		this.password = password;
		this.accountId = accountId;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}
	
	public boolean isValidityChecked() {
		return validityChecked;
	}

	public void setValidityChecked(boolean validityChecked) {
		this.validityChecked = validityChecked;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public ZonedDateTime getValidateDate() {
		return validateDate;
	}

	public void setValidateDate(ZonedDateTime validateDate) {
		this.validateDate = validateDate;
	}

	public ZonedDateTime getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(ZonedDateTime changeDate) {
		this.changeDate = changeDate;
	}

	/**
	 * Check if stored echo record is still in timeout interval.
	 *
	 * @param timeout
	 * @return
	 */
	public boolean isEchoValid(long timeout) {
		ZonedDateTime changeDate = this.getChangeDate();
		if (changeDate == null) {
			return false;
		}

		return ZonedDateTime.now().isBefore(changeDate.plusSeconds(timeout));
	}

	@Override
	@JsonIgnore
	public Serializable getId() {
		// Nothing, method from BaseDto
		return null;
	}

	@Override
	public void setId(Serializable id) {
		// Nothing, method from BaseDto
	}

}
