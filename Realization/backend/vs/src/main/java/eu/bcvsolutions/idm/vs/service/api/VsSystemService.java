package eu.bcvsolutions.idm.vs.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemDto;

/**
 * Service for virtual system
 * 
 * @author Svanda
 *
 */
public interface VsSystemService{

	/**
	 * Create virtual system. System will be included mapping by default fields
	 * @param vsSystem
	 * @return
	 */
	SysSystemDto create(VsSystemDto vsSystem);
		
}
