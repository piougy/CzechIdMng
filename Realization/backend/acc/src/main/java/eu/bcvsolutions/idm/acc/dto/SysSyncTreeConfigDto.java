package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

/**
 * <i>SysSyncTreeConfig</i> is responsible for keep specific information about
 * tree synchronization configuration.
 * 
 * @author Radek Tomiška
 * @since 10.4.0 
 */
@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncTreeConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;
	//
	private boolean startAutoRoleRec = true;

	/**
	 * Start recalculation after end synchronization for automatic roles.
	 * 
	 * @return start
	 */
	public boolean isStartAutoRoleRec() {
		return startAutoRoleRec;
	}

	/**
	 * Start recalculation after end synchronization for automatic roles.
	 * 
	 * @param startAutoRoleRec start
	 */
	public void setStartAutoRoleRec(boolean startAutoRoleRec) {
		this.startAutoRoleRec = startAutoRoleRec;
	}

}
