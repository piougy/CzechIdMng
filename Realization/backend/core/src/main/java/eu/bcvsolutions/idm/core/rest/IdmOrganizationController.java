package eu.bcvsolutions.idm.core.rest;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationLookup;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.revision.IdmRevisionController;
import eu.bcvsolutions.idm.core.revision.RevisionAssembler;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = "/api/organizations/")
public class IdmOrganizationController implements IdmRevisionController {
	
	@Autowired
	private IdmOrganizationLookup organizationLookup;
	
	@Autowired
	private IdmAuditService auditService; 
	
	@Override
	@RequestMapping(value = "{identityId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("identityId") String identityId, @PathVariable("revId") Integer revId) {
		Revision<Integer, ? extends AbstractEntity> revision = this.auditService.findRevision(IdmOrganization.class, revId, Long.parseLong(identityId));
		IdmOrganization entity = (IdmOrganization) revision.getEntity();
		RevisionAssembler<IdmOrganization> assembler = new RevisionAssembler<IdmOrganization>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(this.organizationLookup.getResourceIdentifier(entity)), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = "{identityId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("identityId") String identityId) {
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends AbstractEntity>> results = this.auditService.findRevisions(IdmOrganization.class, Long.parseLong(identityId));
		RevisionAssembler<IdmOrganization> assembler = new RevisionAssembler<IdmOrganization>();
		
		for	(Revision<Integer, ? extends AbstractEntity> revision : results) {
			wrappers.add(assembler.toResource(this.getClass(), 
					String.valueOf(this.organizationLookup.getResourceIdentifier((IdmOrganization)revision.getEntity())),
					revision, revision.getRevisionNumber()));
		}
		
		ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>> resources = new ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>(
				wrappers);
		
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>>(resources, HttpStatus.OK);
	}

}
