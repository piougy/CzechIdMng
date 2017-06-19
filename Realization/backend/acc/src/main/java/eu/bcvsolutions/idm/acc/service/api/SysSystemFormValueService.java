package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.eav.service.api.FormValueService;

/**
 * Service for control form value on system
 * @author svandav
 *
 */
public interface SysSystemFormValueService extends FormValueService<SysSystem, SysSystemFormValue>, CloneableService<SysSystemFormValue> {

}
