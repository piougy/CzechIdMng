import { Managers } from 'czechidm-core';
import { ExampleService } from '../services';

/**
 * Example product manager
 *
 * @author Radek Tomiška
 */
export default class ExampleManager {

  constructor() {
    this.service = new ExampleService();
    this.dataManager = new Managers.DataManager();
  }

  getModule() {
    return 'example';
  }

  getService() {
    return this.service;
  }

  /**
   * Example client error
   *
   * @param  {string} parameter - some value
   * @return {object} - redux action
   */
  clientError(parameter) {
    return (dispatch) => {
      this.getService().clientError(parameter)
      .then(() => {
        // nothing - just error handling example
      })
      .catch(error => {
        dispatch(this.dataManager.receiveError(null, null, error));
      });
    };
  }

  /**
   * Example server error
   *
   * @param  {string} parameter - some value
   * @return {object} - redux action
   */
  serverError(parameter) {
    return (dispatch) => {
      this.getService().serverError(parameter)
      .then(() => {
        // nothing - just error handling example
      })
      .catch(error => {
        dispatch(this.dataManager.receiveError(null, null, error));
      });
    };
  }
}
