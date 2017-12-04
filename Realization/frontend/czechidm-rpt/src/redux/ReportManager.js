import Immutable from 'immutable';
//
import { Managers } from 'czechidm-core';
import { ReportService } from '../services';

/**
 * Report manager
 *
 * @author Radek Tomiška
 */
export default class ReportManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new ReportService();
    this.dataManager = new Managers.DataManager();
  }

  getModule() {
    return 'rpt';
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'Report';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'reports';
  }

  /**
   * Loads registeered reports
   *
   * @return {action}
   */
  fetchSupportedReports(cb = null) {
    const uiKey = ReportManager.UI_KEY_SUPPORTED_REPORTS;
    //
    return (dispatch) => {
      /* const loaded = Managers.DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {*/
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getSupportedReports()
        .then(json => {
          let reports = new Immutable.Map();
          if (json._embedded && json._embedded.reportExecutors) {
            json._embedded.reportExecutors.forEach(item => {
              reports = reports.set(item.id, item);
            });
          }
          dispatch(this.dataManager.receiveData(uiKey, reports));
          if (cb) {
            cb(reports, null);
          }
        })
        .catch(error => {
          if (error.statusCode === 403) {
            // logger user doesn't have permissions for creating reports
          } else {
            //
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error, cb));
          }
        });
      // }
    };
  }
}

ReportManager.UI_KEY_SUPPORTED_REPORTS = 'rpt-supported-reports';
