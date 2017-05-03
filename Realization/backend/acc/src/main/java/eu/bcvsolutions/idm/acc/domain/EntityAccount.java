package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Interface for all relations between entity and account (for synchronization purpose)
 * @author svandav
 *
 */
public interface EntityAccount extends Identifiable {

	AccAccount getAccount();

	boolean isOwnership();

	AbstractEntity getEntity();

}