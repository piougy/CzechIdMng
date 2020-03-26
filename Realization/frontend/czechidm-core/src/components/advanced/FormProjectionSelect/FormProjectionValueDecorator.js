import * as Basic from '../../basic';
import { FormProjectionManager } from '../../../redux';

const formProjectionManager = new FormProjectionManager();

/**
 * Form projection select value decorator.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FormProjectionValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (!entity) {
      // default
      return 'component:identity';
    }
    return formProjectionManager.getLocalization(entity, 'icon', 'component:identity');
  }

}
