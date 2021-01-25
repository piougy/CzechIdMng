import fetch from 'isomorphic-fetch';
import ConfigLoader from '../utils/ConfigLoader';

let lastToken = null;

/**
 * Encapsulates rest calls to BE server
 *
 * @author Vít Švanda
 * @author Jan Helbich
 * @author Radek Tomiška
 */
export default class RestApiService {

  static get(path, token = null, signal = null) {
    return this.action('get', path, null, token, signal);
  }

  static post(path, json, token = null, signal = null) {
    return this.action('post', path, json, token, signal);
  }

  static put(path, json, token = null, signal = null) {
    return this.action('put', path, json, token, signal);
  }

  static patch(path, json, token = null, signal = null) {
    return this.action('PATCH', path, json, token, signal);
  }

  static delete(path, token = null, signal = null) {
    return this.action('DELETE', path, null, token, signal);
  }

  static processHeaders(response) {
    // Only tokens from success responses will be set to userContext
    if (response.status >= 400) {
      return response;
    }
    RestApiService.setTokenCIDMST(response.headers.get('CIDMST'));
    return response;
  }

  /**
   * Dynamic fetch action, use it wisely
   *
   * @param method http method - e.g PUT / POST / DELETE
   * @param path  url path
   * @param json  request data - (PUT / POST)
   * @param token authentication token
   * @param signal - Signal from AbortController. Allow abort of the request.
   * @return {promise}
   */
  static action(method, path, json, token = null, signal = null) {
    const fetchConfig = this._getFetchConfig(method || 'put', json, token, signal);
    //
    return fetch(this.getUrl(path), fetchConfig).then(this.processHeaders);
  }

  static upload(path, formData) {
    return fetch(this.getUrl(path), {
      method: 'post',
      headers: {
        CIDMST: RestApiService.getTokenCIDMST()
      },
      credentials: 'include',
      body: formData
    });
  }

  static download(path) {
    return fetch(this.getUrl(path), {
      method: 'get',
      headers: {
        CIDMST: RestApiService.getTokenCIDMST()
      },
      credentials: 'include'
    });
  }

  static getUrl(path) {
    // we have whole path already
    if (path.lastIndexOf('http', 0) === 0 || path.lastIndexOf(ConfigLoader.getServerUrl(), 0) === 0) {
      return path;
    }
    return `${ ConfigLoader.getServerUrl() }${ path }`;
  }

  static _getFetchConfig(methodType, body, token = null, signal = null) {
    if (token === null) {
      token = RestApiService.getTokenCIDMST();
    }

    const fetchConfig = {
      method: methodType,
      headers: {
        Accept: 'application/hal+json',
        'Content-Type': 'application/hal+json;charset=UTF-8'
      },
      credentials: 'include'
    };
    if (token) {
      fetchConfig.headers.CIDMST = token;
    }
    // Signal from AbortController. Allow abort of the request.
    if (signal) {
      fetchConfig.signal = signal;
    }
    if (body) {
      fetchConfig.body = JSON.stringify(body);
    }
    return fetchConfig;
  }

  /**
   * Get redux store from local storage.
   *
   * @return {object} redux store from local storage
   * @since 10.8.0
   */
  static getStorage() {
    if (!localStorage['czechidm-storage']) {
      return null;
    }
    return JSON.parse(localStorage['czechidm-storage']);
  }

  /**
   * Clear local storage.
   *
   * @since 10.8.0
   */
  static clearStorage() {
    delete localStorage['czechidm-storage'];
  }

  /**
   * Get redux security store from local storage.
   *
   * @return {object} redux security store from local storage
   * @since 10.8.0
   */
  static getSecurityStore() {
    const storage = RestApiService.getStorage();
    if (!storage || !storage.security) {
      return null;
    }
    return storage.security;
  }

  /**
   * Returns logged user context (username, roles etc.).
   *
   * @return {object} userContext
   * @since 10.8.0
   */
  static getUserContext() {
    if (!RestApiService.getSecurityStore()) {
      return null;
    }
    return RestApiService.getSecurityStore().userContext;
  }

  /**
   * Returns logged user CIDMST token.
   * Support extend token automatically.
   *
   * @return {string} token
   * @since 10.8.0
   */
  static getTokenCIDMST() {
    const userContext = RestApiService.getUserContext();
    if (!userContext || !userContext.isAuthenticated) {
      return null;
    }
    // last token is newer => higher priority
    return lastToken || userContext.tokenCIDMST;
  }

  /**
   * Return automatically extended token.
   * @return {string} token
   * @since 10.8.0
   */
  static getLastToken() {
    return lastToken;
  }

  /**
   * Set last CIDMST token (e.g. from api response).
   *
   * @since 10.8.0
   */
  static setTokenCIDMST(token) {
    if (!token || token === null) {
      return;
    }
    lastToken = token;
  }
}
