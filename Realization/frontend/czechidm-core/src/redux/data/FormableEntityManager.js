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
          const formValuesPromises = json._embedded.formDefinitions.map(formDefinition => {
            formInstances = formInstances.set(formDefinition.code, new FormInstance(formDefinition, []));
            return this.getService().getFormValues(id, formDefinition.code);
          });
          Promise.all(formValuesPromises)
            .then((jsons) => {
              jsons.forEach(jsonA => {
                if (jsonA._embedded.formValues) {
                  const formValues = jsonA._embedded.formValues;
                  if (formValues.length > 0) {
                    const formDefinition = formInstances.get(formValues[0]._embedded.formAttribute.formDefinition.code).getDefinition();
                    formInstances = formInstances.set(formDefinition.code, new FormInstance(formDefinition, formValues));
                  }
                }
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
