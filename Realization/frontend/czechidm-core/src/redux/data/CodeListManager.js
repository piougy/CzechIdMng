import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { CodeListService } from '../../services';
import DataManager from './DataManager';
import SearchParameters from '../../domain/SearchParameters';
import CodeListItemManager from './CodeListItemManager';

/**
 * Code lists.
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

  getCodeListContainerUiKey() {
    return 'codelist-data';
  }

  getCodeListUiKey(code) {
    return `codelist-${ code }`;
  }

  /**
   * Load codelist items to select boxes.
   * Autocomplete permission is needed.
   * When codelist is not defined, then no options is returned (without error).
   *
   * @param  {string} code
   * @param  {func} [cb=null]
   * @return {action}
   */
  fetchCodeListIfNeeded(code, cb = null) {
    const codeListContainerUiKey = this.getCodeListContainerUiKey();
    const uiKey = this.getCodeListUiKey(code);
    //
    return (dispatch, getState) => {
      let codeLists = DataManager.getData(getState(), codeListContainerUiKey) || new Immutable.Map({});
      //
      const codeList = codeLists.get(uiKey);
      if (codeList) {
        // we don't need to load code list again - cache
        if (cb) {
          cb(codeList);
        }
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        const searchParameters = new SearchParameters()
          .setName(SearchParameters.NAME_AUTOCOMPLETE)
          .setFilter('codeListId', code)
          .setSort('name', true)
          .setSize(10000);
        this.codeListItemManager.getService().search(searchParameters)
          .then(json => {
            const data = json._embedded[this.codeListItemManager.getCollectionType()] || [];
            codeLists = codeLists.set(uiKey, data);
            //
            dispatch(this.dataManager.receiveData(codeListContainerUiKey, codeLists, () => {
              if (cb) {
                // callback with loaded codelist only.
                cb(data);
              }
            }));
            dispatch(this.dataManager.stopRequest(uiKey)); // show loading is preserved under codelist key
          })
          .catch(error => {
            if (error.statusCode === 400 || error.statusCode === 403) {
              // FIXME: 204 / 404 - codelist doesn't found
              // FIXME: 403 - input only
              dispatch(this.dataManager.receiveData(uiKey, [], cb));
            } else {
              // TODO: data uiKey
              dispatch(this.receiveError(null, uiKey, error, cb));
            }
          });
      }
    };
  }

  getCodeList(state, code) {
    const codeLists = DataManager.getData(state, this.getCodeListContainerUiKey()) || new Immutable.Map({});
    const uiKey = this.getCodeListUiKey(code);
    //
    if (!codeLists.has(uiKey)) {
      return null;
    }
    return codeLists.get(uiKey);
  }

  isShowLoading(state, code) {
    return DataManager.isShowLoading(state, this.getCodeListUiKey(code));
  }

  /**
   * Clear all loaded codelists in redux state.
   *
   * @return {action}
   * @since 10.6.0
   */
  clearCodeLists() {
    return (dispatch) => {
      dispatch(this.dataManager.clearData(this.getCodeListContainerUiKey()));
    };
  }

  /**
   * Clear code list in redux state by given code.
   *
   * @param  {string} code
   * @return {action}
   * @since 10.6.0
   */
  clearCodeList(code) {
    return (dispatch, getState) => {
      const codeListContainerUiKey = this.getCodeListContainerUiKey();
      const codeLists = DataManager.getData(getState(), codeListContainerUiKey) || new Immutable.Map({});
      const uiKey = this.getCodeListUiKey(code);
      //
      if (codeLists.has(uiKey)) {
        dispatch(this.dataManager.receiveData(codeListContainerUiKey, codeLists.delete(uiKey)));
      }
    };
  }
}
