import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import FormInstance from '../../domain/FormInstance';
import * as Utils from '../../utils';

/**
 * Manager for entities with eav attributes supports
 *
 * @author Radek Tomiška
 */
export default class FormableEntityManager extends EntityManager {

  /**
   * Returns defaulteav form uiKey by entity type, if given uiKey is not defined
   * This ui key can be used for check state of entities fetching etc.
   *
   * @param  {string} uiKey - ui key for loading indicator etc
   * @param  {string|number} id - Entity identifier
   * @return {string} - Returns uiKey
   */
  getFormableUiKey(uiKey, id) {
    if (uiKey) {
      return uiKey;
    }
    return `eav-${ this.resolveUiKey(uiKey, id) }`;
  }

  /**
   * Supports attachment value (usable for attributes with persistent type 'ATTACHMENT') - enable download, preview etc.
   *
   * @since 9.4.0
   */
  supportsAttachment() {
    return this.getService().supportsAttachment();
  }

  /**
   * Load form instances (definitions + values) by given entity
   *
   * @param  {string} id entity identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchFormInstances(id, uiKey = null, cb = null) {
    uiKey = this.getFormableUiKey(uiKey, id);
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      //
      this.getService().getFormDefinitions(id)
        .then(json => {
          let formInstances = new Immutable.Map();
          // get trimmed definitions
          const formValuesPromises = [];
          json._embedded.formDefinitions.forEach(formDefinition => {
            if (!id) {
              formValuesPromises.push(this.getService().prepareFormValues(formDefinition.code));
            } else {
              formValuesPromises.push(this.getService().getFormValues(id, formDefinition.code));
            }
          });
          // load form instances
          Promise.all(formValuesPromises)
            .then((jsons) => {
              jsons.forEach(jsonA => {
                formInstances = formInstances.set(jsonA.formDefinition.code, new FormInstance(jsonA));
              });
              //
              dispatch(this.dataManager.receiveData(uiKey, formInstances));
              if (cb) {
                cb(formInstances);
              }
            })
            .catch(error => {
              // TODO: data uiKey
              dispatch(this.receiveError(null, uiKey, error, cb));
            });
        });
    };
  }

  /**
   * Saves form values
   *
   * @param  {string} id entity identifier
   * @param  {string} form definition code
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveFormValues(id, definitionCode, values, uiKey = null, cb = null) {
    uiKey = this.getFormableUiKey(uiKey, id);
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveFormValues(id, definitionCode, values)
        .then(() => {
          dispatch(this.fetchFormInstances(id, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves single form value
   *
   * @param  {string} id entity identifier
   * @param  {string} form definition code
   * @param  {IdmFormValueDto} value filled form value
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveFormValue(id, value, uiKey = null, cb = null) {
    uiKey = this.getFormableUiKey(uiKey, id);
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveFormValue(id, value)
        .then(() => {
          dispatch(this.fetchFormInstances(id, uiKey, cb));
          if (cb) {
            cb();
          }
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Form attribute - owner id, attribute id and value (uuid value) as attachment id is required
   * @param  {IdmFormValueDto} formValue
   * @return {string}
   */
  getDownloadUrl(formValue) {
    return this.getService().getDownloadUrl(formValue);
  }

  downloadPreview(formValue, uiKey, cb) {
    return (dispatch) => {
      if (uiKey) {
        dispatch(this.dataManager.requestData(uiKey));
      }
      this.getService().downloadPreview(formValue)
        .then(response => {
          if (response.status === 404 || response.status === 204) {
            return null;
          } else if (response.status === 200) {
            return response.blob();
          }
          const json = response.json();
          if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
          }
          if (Utils.Response.hasInfo(json)) {
            throw Utils.Response.getFirstInfo(json);
          }
        })
        .then(blob => {
          let imageUrl = false;
          if (blob) {
            imageUrl = URL.createObjectURL(blob);
          }
          if (uiKey) {
            dispatch(this.dataManager.receiveData(uiKey, imageUrl));
          }
          if (cb) {
            cb(imageUrl);
          }
        })
        .catch(error => {
          if (uiKey) {
            dispatch(this.receiveError(null, uiKey, error));
          }
          if (cb) {
            cb(false);
          }
        });
    };
  }
}
