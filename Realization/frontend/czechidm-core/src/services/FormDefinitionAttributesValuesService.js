import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * @author Roman Kučera
 */
class FormDefinitionAttributesValuesService extends AbstractService {

  getApiPath() {
    return '/form-definition-values';
  }

  getAttributesValues() {
    return RestApiService.get(this.getApiPath())
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

}

export default FormDefinitionAttributesValuesService;
