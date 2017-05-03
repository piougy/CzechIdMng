package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;

import java.util.List;
import java.util.UUID;

/**
 * Filter for role request
 *
 * @author svandav
 */
public class RoleRequestFilter extends QuickFilter {
    private UUID applicantId;
    private String applicant;
    private RoleRequestState state;
    private UUID duplicatedToRequestId;
    private List<RoleRequestState> states;

    public UUID getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(UUID applicantId) {
        this.applicantId = applicantId;
    }

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public UUID getDuplicatedToRequestId() {
        return duplicatedToRequestId;
    }

    public void setDuplicatedToRequestId(UUID duplicatedToRequestId) {
        this.duplicatedToRequestId = duplicatedToRequestId;
    }

    public List<RoleRequestState> getStates() {
        return states;
    }

    public void setStates(List<RoleRequestState> states) {
        this.states = states;
    }

}
