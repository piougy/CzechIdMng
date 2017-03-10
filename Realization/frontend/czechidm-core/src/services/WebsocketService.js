import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

/**
 * Websocket logs
 *
 * TODO: Move websocket init here
 *
 * @author Radek Tomiška
 */
export default class WebsocketService extends AbstractService {

  getApiPath() {
    return '/notification-websockets';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.message.subject;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }
}
