package eu.bcvsolutions.idm.core.workflow.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Listener call after task created. Convert task candidates from user name to identifier (UUID)
 * 
 * @author svandav
 *
 */
public class CandidateToUuidEventListener implements ActivitiEventListener {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CandidateToUuidEventListener.class);

	@Autowired private LookupService lookupService;

	@Override
	public void onEvent(ActivitiEvent event) {
		log.debug("CandidateToUiidEventListener - recieve event [{}]", event.getType());
		switch (event.getType()) {

		case TASK_ASSIGNED:
		case TASK_CREATED:
			if (event instanceof ActivitiEntityEventImpl &&
					((ActivitiEntityEventImpl)event).getEntity() instanceof TaskEntity) {
				TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl)event).getEntity();
				if(taskEntity != null && taskEntity.getCandidates() != null){
					taskEntity.getCandidates().forEach(identityLink -> {
						String user = identityLink.getUserId();
						if(EntityUtils.isUuid(user)){
							// userId is UUID (we can continue)
							return;
						}
						IdmIdentityDto identity = (IdmIdentityDto) lookupService.getDtoLookup(IdmIdentityDto.class).lookup(user);
						if(identity != null){
							taskEntity.deleteUserIdentityLink(user, identityLink.getType());
							taskEntity.addCandidateUser(identity.getId().toString());
						}else{
							// TODO: Could be good place to throw exception (maybe in future) 
						}
					});
				}
			}
			break;
		default:
			log.debug("StartSubprocesEventListener - receive not required event [{}]", event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		// We can throw exception
		return true;
	}
}