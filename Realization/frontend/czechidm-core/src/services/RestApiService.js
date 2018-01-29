import fetch from 'isomorphic-fetch';
import AuthenticateService from './AuthenticateService';
import ConfigLoader from '../utils/ConfigLoader';

/**
 * Encapsulates rest calls to BE server
 *
 * @author Vít Švanda
 * @author Jan Helbich
 * @author Radek Tomiška
 */
export default class RestApiService {

  static get(path, token = null) {
    return this.action('get', path, null, token);
  }

  static post(path, json, token = null) {
    return this.action('post', path, json, token);
  }

  static put(path, json, token = null) {
    return this.action('put', path, json, token);
  }

  static patch(path, json, token = null) {
    return this.action('PATCH', path, json, token);
  }

  static delete(path, token = null) {
    return this.action('DELETE', path, null, token);
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

  static upload(path, formData) {
    return fetch(this.getUrl(path), {
      method: 'post',
      headers: {
        'CIDMST': AuthenticateService.getTokenCIDMST()
      },
      credentials: 'include',
      body: formData
    });
  }

  static download(path) {
    return fetch(this.getUrl(path), {
      method: 'get',
      headers: {
        'CIDMST': AuthenticateService.getTokenCIDMST()
      },
      credentials: 'include'
    });
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
        'Accept': 'application/hal+json',
        'Content-Type': 'application/hal+json;charset=UTF-8'
      },
      credentials: 'include'
    };
    if (token) {
      fetchConfig.headers.CIDMST = token;
    }
    if (body) {
      fetchConfig.body = JSON.stringify(body);
    }
    return fetchConfig;
  }
}
