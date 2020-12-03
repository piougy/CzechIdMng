package eu.bcvsolutions.idm.core.api.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Password dto
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 *
 */

@Relation(collectionRelation = "passwords")
public class IdmPasswordDto extends AbstractDto implements ValidableEntity  {

	private static final long serialVersionUID = 1L;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
    private UUID identity;
    private LocalDate validTill;
    private LocalDate validFrom;
    private boolean mustChange = false;
    private ZonedDateTime lastSuccessfulLogin;
    private int unsuccessfulAttempts;
    private ZonedDateTime blockLoginDate = null;
    private boolean passwordNeverExpires = false;
    @JsonProperty(access = Access.WRITE_ONLY)
	private String verificationSecret;

    public ZonedDateTime getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(ZonedDateTime lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public int getUnsuccessfulAttempts() {
        return unsuccessfulAttempts;
    }

    public void setUnsuccessfulAttempts(int unsuccessfulAttempts) {
        this.unsuccessfulAttempts = unsuccessfulAttempts;
    }

    public void increaseUnsuccessfulAttempts() {
        this.unsuccessfulAttempts++;
    }

    public void resetUnsuccessfulAttempts() {
        unsuccessfulAttempts = 0;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    @Override
    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    @Override
    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public boolean isMustChange() {
        return mustChange;
    }

    public void setMustChange(boolean mustChange) {
        this.mustChange = mustChange;
    }

	public ZonedDateTime getBlockLoginDate() {
		return blockLoginDate;
	}

	public void setBlockLoginDate(ZonedDateTime blockLoginDate) {
		this.blockLoginDate = blockLoginDate;
	}

	public boolean isPasswordNeverExpires() {
		return passwordNeverExpires;
	}

	public void setPasswordNeverExpires(boolean passwordNeverExpires) {
		this.passwordNeverExpires = passwordNeverExpires;
	}
	
	/**
	 * Two factor authentication secret.
	 * 
	 * @param verificationToken secret
	 * @since 10.7.0
	 */
	public void setVerificationSecret(String verificationSecret) {
		this.verificationSecret = verificationSecret;
	}
	
	/**
	 * Two factor authentication secret.
	 * 
	 * @return secret
	 * @since 10.7.0
	 */
	public String getVerificationSecret() {
		return verificationSecret;
	}
}
