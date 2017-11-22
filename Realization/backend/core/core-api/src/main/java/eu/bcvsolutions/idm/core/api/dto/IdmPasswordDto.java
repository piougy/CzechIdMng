package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Password dto
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
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
    private LocalDate lastSuccessfulLogin;
    private int unsuccessfulAttemps = 0;

    public LocalDate getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(LocalDate lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public int getUnsuccessfulAttemps() {
        return unsuccessfulAttemps;
    }

    public void increaseUnsuccessfulAttemps() {
        unsuccessfulAttemps++;
    }

    public void resetUnsuccessfulAttemps() {
        unsuccessfulAttemps = 0;
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
}
