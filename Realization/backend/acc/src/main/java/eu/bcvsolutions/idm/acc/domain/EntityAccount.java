package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Interface for all relations between entity and account (for synchronization purpose)
 * @author svandav
 *
 */
public interface EntityAccount {

	AccAccount getAccount();

	boolean isOwnership();

	SysRoleSystem getRoleSystem();

	AbstractEntity getEntity();

}