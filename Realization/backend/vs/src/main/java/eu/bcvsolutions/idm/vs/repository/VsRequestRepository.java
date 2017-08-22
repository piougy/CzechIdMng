package eu.bcvsolutions.idm.vs.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;

/**
 * Repository for virtual system request
 * 
 * @author Svanda
 *
 */
public interface VsRequestRepository extends AbstractEntityRepository<VsRequest, RequestFilter> {


}
