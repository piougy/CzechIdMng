import fetch from 'isomorphic-fetch';
import AuthenticateService from './AuthenticateService';
import ConfigLoader from '../utils/ConfigLoader';

/**
 * Rest calls to BE server for plain/text
 *
 * @author Patrik Stloukal
 */
export default class PlainTextApi {

  static put(path, json, token = null) {
    return this.action('put', path, json, token);
  }

  static processHeaders(response) {
    AuthenticateService.setTokenCIDMST(response.headers.get('CIDMST'));
    return response;
  }

  /**
   * Dynamic fetch action, use it wisely
   *
   * @param  {[type]} method http method - e.g PUT / POST / DELETE
   * @param  {[type]} path  url path
   * @param  {[type]} json  request data - (PUT / POST)
   * @param  {[type]} token authentication token
   * @return {promise}
   */
  static action(method, path, json, token = null) {
    const fetchConfig = this._getFetchConfig(method ? method : 'put', json, token);
    return fetch(this.getUrl(path), fetchConfig);
  }

  static getUrl(path) {
    // we have whole path already
    if (path.lastIndexOf('http', 0) === 0 || path.lastIndexOf(ConfigLoader.getServerUrl(), 0) === 0) {
      return path;
    }
    return `${ConfigLoader.getServerUrl()}${path}`;
  }

  static _getFetchConfig(methodType, body, token = null) {
    if (token === null) {
      token = AuthenticateService.getTokenCIDMST();
    }

    const fetchConfig = {
      method: methodType,
      headers: {
        'Accept': 'text/plain, application/hal+json',
        'Content-Type': 'text/plain;charset=UTF-8'
      },
      credentials: 'include'
    };
    if (token) {
      fetchConfig.headers.CIDMST = token;
    }
    if (body) {
      fetchConfig.body = body;
    }
    return fetchConfig;
  }
}
