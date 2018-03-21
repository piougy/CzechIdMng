package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

/**
 * Dto for concept role request
 *
 * @author svandav
 */
@Relation(collectionRelation = "conceptRoleRequests")
public class IdmConceptRoleRequestDto extends AbstractDto implements Loggable {

    private static final long serialVersionUID = 1L;
    public static final String WF_PROCESS_FIELD = "wfProcessId";

    @Embedded(dtoClass = IdmRoleRequestDto.class)
    private UUID roleRequest;
    @Embedded(dtoClass = IdmIdentityContractDto.class)
    private UUID identityContract;
    @Embedded(dtoClass = IdmRoleDto.class)
    private UUID role;
    @Embedded(dtoClass = IdmIdentityRoleDto.class)
    private UUID identityRole; // For update and delete operations
    private UUID roleTreeNode; // this attribute can't be renamed (backward compatibility) - AutomaticRole reference
    private LocalDate validFrom;
    private LocalDate validTill;
    private ConceptRoleRequestOperation operation;
    @JsonProperty(access = Access.READ_ONLY)
    private RoleRequestState state;
    @JsonProperty(access = Access.READ_ONLY)
    private String wfProcessId;
    @JsonProperty(access = Access.READ_ONLY)
    private String log;

	public UUID getRoleRequest() {
        return roleRequest;
    }

    public void setRoleRequest(UUID roleRequest) {
        this.roleRequest = roleRequest;
	}

    public UUID getIdentityContract() {
        return identityContract;
    }

    public void setIdentityContract(UUID identityContract) {
        this.identityContract = identityContract;
    }

    public UUID getRole() {
        return role;
    }

    public void setRole(UUID role) {
        this.role = role;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public ConceptRoleRequestOperation getOperation() {
        return operation;
    }

    public void setOperation(ConceptRoleRequestOperation operation) {
        this.operation = operation;
    }

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public String getWfProcessId() {
        return wfProcessId;
    }

    public void setWfProcessId(String wfProcessId) {
        this.wfProcessId = wfProcessId;
    }

    public UUID getIdentityRole() {
        return identityRole;
    }

    public void setIdentityRole(UUID identityRole) {
        this.identityRole = identityRole;
    }

    /**
     * Automatic role
     * 
     * @return
     * @deprecated since 7.8.0 - use {@link #getAutomaticRole()}
     */
    @Deprecated
    public UUID getRoleTreeNode() {
        return this.getAutomaticRole();
    }

    /**
     * Automatic role
     * 
     * @param roleTreeNode
     * @deprecated since 7.8.0- use {@link #setAutomaticRole(UUID)}
     */
    @Deprecated
    public void setRoleTreeNode(UUID roleTreeNode) {
        this.setAutomaticRole(roleTreeNode);
    }
    
    public UUID getAutomaticRole() {
    	return this.roleTreeNode;
    }
    
    public void setAutomaticRole(UUID automaticRole) {
    	this.roleTreeNode = automaticRole;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String addToLog(String text) {
        if (text != null) {
            StringBuilder builder = new StringBuilder();
            if (this.log != null) {
                builder.append(this.log);
                builder.append("\n" + IdmRoleRequestDto.LOG_SEPARATOR + "\n");
            }
            builder.append(text);
            this.setLog(builder.toString());
        }
        return this.getLog();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((identityContract == null) ? 0 : identityContract.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((roleRequest == null) ? 0 : roleRequest.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((validFrom == null) ? 0 : validFrom.hashCode());
        result = prime * result + ((validTill == null) ? 0 : validTill.hashCode());
        result = prime * result + ((wfProcessId == null) ? 0 : wfProcessId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IdmConceptRoleRequestDto)) {
            return false;
        }
        IdmConceptRoleRequestDto other = (IdmConceptRoleRequestDto) obj;
        if (identityContract == null) {
            if (other.identityContract != null) {
                return false;
            }
        } else if (!identityContract.equals(other.identityContract)) {
            return false;
        }
        if (identityRole == null) {
            if (other.identityRole != null) {
                return false;
            }
        } else if (!identityRole.equals(other.identityRole)) {
            return false;
        }
        if (operation != other.operation) {
            return false;
        }
        if (role == null) {
            if (other.role != null) {
                return false;
            }
        } else if (!role.equals(other.role)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (validFrom == null) {
            if (other.validFrom != null) {
                return false;
            }
        } else if (!validFrom.equals(other.validFrom)) {
            return false;
        }
        if (validTill == null) {
            if (other.validTill != null) {
                return false;
            }
        } else if (!validTill.equals(other.validTill)) {
            return false;
        }
        if (wfProcessId == null) {
            if (other.wfProcessId != null) {
                return false;
            }
        } else if (!wfProcessId.equals(other.wfProcessId)) {
            return false;
        }
        return true;
    }
 }