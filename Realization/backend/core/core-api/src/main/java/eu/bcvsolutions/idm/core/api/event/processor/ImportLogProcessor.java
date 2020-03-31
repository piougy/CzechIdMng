package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmImportLogDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Import log processors should implement this interface.
 * 
 * @author Vít Švanda
 *
 */
public interface ImportLogProcessor extends EntityEventProcessor<IdmImportLogDto> {

}
