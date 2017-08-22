package eu.bcvsolutions.idm.vs.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.vs.entity.VsRequestBatch;
import eu.bcvsolutions.idm.vs.repository.filter.RequestBatchFilter;

/**
 * Repository for virtual system request batch
 * 
 * @author Svanda
 *
 */
public interface VsRequestBatchRepository extends AbstractEntityRepository<VsRequestBatch, RequestBatchFilter> {


}
