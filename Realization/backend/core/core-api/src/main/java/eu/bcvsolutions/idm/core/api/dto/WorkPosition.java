package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * IdentityContract with additional infos:
 * - contains prime identity's working position
 * - contains all work position's parents in tree structure and contract's working position as path
 *
 * @author Radek Tomiška
 */
public class WorkPosition extends AbstractDto {

    private static final long serialVersionUID = 6839506093315671159L;
    private UUID identity;
    private UUID contract;
    private List<UUID> path;

    public WorkPosition() {
    }

    public WorkPosition(UUID identity, UUID contract) {
        super(contract);
        this.identity = identity;
        this.contract = contract;
    }

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    public UUID getContract() {
        return contract;
    }

    public void setContract(UUID contract) {
        this.contract = contract;
    }

    public List<UUID> getPath() {
        return path;
    }

    public void setPath(List<UUID> path) {
        this.path = path;
    }
}
