import EntityManager from './EntityManager';
import { CodeListService } from '../../services';
import DataManager from './DataManager';
import SearchParameters from '../../domain/SearchParameters';
import CodeListItemManager from './CodeListItemManager';

/**
 * Code lists
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class CodeListManager extends EntityManager {

  constructor() {
    super();
    this.service = new CodeListService();
    this.dataManager = new DataManager();
    this.codeListItemManager = new CodeListItemManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'CodeList';
  }

  getCollectionType() {
    return 'codeLists';
  }

  getIdentifierAlias() {
    return 'code';
  }

  getCodeListUiKey(code) {
    return `codelist-${ code }`;
  }

  fetchCodeListIfNeeded(code) {
    const uiKey = `codelist-${ code }`;
    //
    return (dispatch, getState) => {
      const codeList = DataManager.getData(getState(), uiKey);
      if (codeList) {
        // we dont need to load code list again - cache
      } else {
        // TODO: new store?
        dispatch(this.dataManager.requestData(uiKey));
        const searchParameters = new SearchParameters().setFilter('codeListId', code).setSort('name', false).setSize(10000);
        this.codeListItemManager.getService().search(searchParameters)
          .then(json => {
            const data = json._embedded[this.codeListItemManager.getCollectionType()] || [];
            dispatch(this.dataManager.receiveData(uiKey, data));
          })
          .catch(error => {
            if (error.statusCode === 400 || error.statusCode === 403) {
              // FIXME: 204 / 404 - codelist doesn't found
              // FIXME: 403 - input only
              dispatch(this.dataManager.receiveData(uiKey, []));
            } else {
              // TODO: data uiKey
              dispatch(this.receiveError(null, uiKey, error));
            }
          });
      }
    };
  }

  getCodeList(state, code) {
    return DataManager.getData(state, this.getCodeListUiKey(code));
  }

  isShowLoading(state, code) {
    return DataManager.isShowLoading(state, this.getCodeListUiKey(code));
  }
}
