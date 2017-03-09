import EntityManager from './EntityManager';
import FormInstance from '../../domain/FormInstance';

/**
 * Manager for entities with eav attributes supports
 *
 * @author Radek Tomiška
 */
export default class FormableEntityManager extends EntityManager {

  /**
   * Load form instance (definition + values) by given entity
   *
   * @param  {string} id entity identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchFormInstance(id, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));

      const formDefinitionPromise = this.getService().getFormDefinition(id);
      const formValuesPromise = this.getService().getFormValues(id);

      Promise.all([formDefinitionPromise, formValuesPromise])
        .then((jsons) => {
          const formDefinition = jsons[0];
          const formValues = jsons[1]._embedded.formValues;

          const formInstance = new FormInstance(formDefinition, formValues);

          dispatch(this.dataManager.receiveData(uiKey, formInstance));
          if (cb) {
            cb(formInstance);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves form values
   *
   * @param  {string} id entity identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveFormValues(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveFormValues(id, values)
      .then(() => {
        dispatch(this.fetchFormInstance(id, uiKey, cb));
      })
      .catch(error => {
        dispatch(this.receiveError(null, uiKey, error, cb));
      });
    };
  }
}
