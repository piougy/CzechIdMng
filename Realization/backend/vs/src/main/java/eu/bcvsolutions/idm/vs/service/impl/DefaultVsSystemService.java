package eu.bcvsolutions.idm.vs.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemDto;

/**
 * Service for virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsSystemService implements VsSystemService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsSystemService.class);

	private final SysSystemService systemService;

	@Autowired
	public DefaultVsSystemService(SysSystemService systemService) {
		Assert.notNull(systemService);
		//
		this.systemService = systemService;
	}
	
	@Override
	public SysSystemDto create(VsSystemDto vsSystem){
		Assert.notNull(vsSystem, "Vs system dto cannot be null (for create new virtual system)");
		Assert.notNull(vsSystem.getName(), "Vs system name cannot be null (for create new virtual system)");

		// TODO
		SysSystemDto system = new SysSystemDto();
		system.setName(vsSystem.getName());
		
		system = this.systemService.save(system, IdmBasePermission.CREATE);
		
		return system;
	}

}
