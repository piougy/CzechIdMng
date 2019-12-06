import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import AuthenticateService from './AuthenticateService';

/**
 * Service for entity with eav attributes endpoint
 *
 * @author Radek Tomiška
 */
export default class FormableEntityService extends AbstractService {

  /**
   * Supports attachment value (usable for attributes with persistent type 'ATTACHMENT') - enable download, preview etc.
   *
   * @since 9.4.0
   */
  supportsAttachment() {
    return false;
  }

  /**
   * Returns form definitions to given entity
   *
   * @param  {string} id entity identifier
   * @return {promise}
   */
  getFormDefinitions(id) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/form-definitions`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns filled form values
   *
   * @param  {string} id entity identifier
   * @param  {string} form definition code
   * @return {promise}
   */
  getFormValues(id, definitionCode) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/form-values?definitionCode=${ encodeURIComponent(definitionCode) }`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves form values
   *
   * @param  {string} id identity identifier
   * @param  {string} form definition code
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveFormValues(id, definitionCode, values) {
    return RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/form-values?definitionCode=${ encodeURIComponent(definitionCode) }`, values)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves single form value
   *
   * @param  {string} id identity identifier
   * @param  {string} form definition code
   * @param  {IdmFormValueDto} value filled form value
   * @return {promise}
   */
  saveFormValue(id, value) {
    return RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/form-value`, value)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Form attribute - owner id, attribute id and value (uuid value) as attachment id is required
   *
   * @param  {IdmFormValueDto} formValue
   * @return {string}
   */
  getDownloadUrl(formValue, token = null) {
    if (!formValue || !formValue.ownerId || !formValue.id) {
      return null;
    }
    if (token === null) {
      token = AuthenticateService.getTokenCIDMST();
    }
    //
    const ownerId = encodeURIComponent(formValue.ownerId);
    const valueId = encodeURIComponent(formValue.id);
    //
    return RestApiService.getUrl(`${ this.getApiPath() }/${ ownerId }/form-values/${ valueId }/download?cidmst=${ token }`);
  }

  /**
   * Get preview image for the given form value - works only for ATTACHMENT persistent type from BE
   *
   * @param  {IdmFormValueDto} formValue
   * @return {Promise}
   */
  downloadPreview(formValue) {
    const ownerId = encodeURIComponent(formValue.ownerId);
    const valueId = encodeURIComponent(formValue.id);
    //
    return RestApiService.download(`${ this.getApiPath() }/${ ownerId }/form-values/${ valueId }/preview`);
  }
}
