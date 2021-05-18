import * as Basic from '../../basic';

/**
 * Contract select option decorator.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class IdentityContractOptionDecorator extends Basic.SelectBox.OptionDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (!entity) {
      // default
      return 'component:contract';
    }
    if (entity.main) {
      // disabled (+ _disabled by not disableable select box)
      return 'component:main-contract';
    }
    // enabled
    return 'component:contract';
  }

}
