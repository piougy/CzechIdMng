import fetch from 'isomorphic-fetch';
import AuthenticateService from './AuthenticateService';
import ConfigLoader from '../utils/ConfigLoader';

export default class RestApiService {

  static get(path, token = null) {
    const fetchConfig = this._getFetchConfig('get', null, token);
    return fetch(this.getUrl(path), fetchConfig);
  }

  static post(path, json, token = null) {
    const fetchConfig = this._getFetchConfig('post', json, token);
    return fetch(this.getUrl(path), fetchConfig);
  }

  static put(path, json, token = null) {
    const fetchConfig = this._getFetchConfig('put', json, token);
    return fetch(this.getUrl(path), fetchConfig);
  }

  static patch(path, json) {
    const fetchConfig = this._getFetchConfig('PATCH', json); // uppercase is needed
    return fetch(this.getUrl(path), fetchConfig);
  }

  static delete(path) {
    const fetchConfig = this._getFetchConfig('DELETE');
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
        'Accept': 'application/json',
        'Content-Type': 'application/json'
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
