package eu.bcvsolutions.idm.acc.dto;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class AccountFilter extends SystemEntityFilter {
	
	private Long systemEntityId;
	
	public Long getSystemEntityId() {
		return systemEntityId;
	}
	
	public void setSystemEntityId(Long systemEntityId) {
		this.systemEntityId = systemEntityId;
	}
}
