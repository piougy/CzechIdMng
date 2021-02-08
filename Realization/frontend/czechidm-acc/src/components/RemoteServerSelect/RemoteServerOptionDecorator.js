import { Basic } from 'czechidm-core';

/**
 * Remote server select option decorator.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class RemoteServerOptionDecorator extends Basic.SelectBox.OptionDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(/* entity*/) {
    return 'component:server';
  }

}
