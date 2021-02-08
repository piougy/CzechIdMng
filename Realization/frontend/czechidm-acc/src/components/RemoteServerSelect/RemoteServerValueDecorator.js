import { Basic } from 'czechidm-core';

/**
 * Identity select value decorator.
 *
 * @author Radek Tomiška
 * @since 10.1.0
 */
export default class RemoteServerValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (entity.additionalOption) {
      return null;
    }
    return 'component:server';
  }

}
