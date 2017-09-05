import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import FormInstance from '../../domain/FormInstance';

/**
 * Manager for entities with eav attributes supports
 *
 * @author Radek Tomiška
 */
export default class FormableEntityManager extends EntityManager {

  /**
   * Load form instances (definitions + values) by given entity
   *
   * @param  {string} id entity identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchFormInstances(id, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      //
      this.getService().getFormDefinitions(id)
        .then(json => {
          let formInstances = new Immutable.Map();
          // get trimmed definitions
          const formValuesPromises = json._embedded.formDefinitions.map(formDefinition => {
            return this.getService().getFormValues(id, formDefinition.code);
          });
          // load form instances
          Promise.all(formValuesPromises)
            .then((jsons) => {
              jsons.forEach(jsonA => {
                formInstances = formInstances.set(jsonA.formDefinition.code, new FormInstance(jsonA.formDefinition, jsonA.values));
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
  saveFormValues(id, definitionCode, values, uiKey, cb = null) {
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
}
