import IdentitySelectFormAttributeRenderer from './IdentitySelectFormAttributeRenderer';

/**
 * Identity select component with support select disabled identity.
 *
 * @author Radek Tomiška
 * @since 10.5.0
 */
export default class IdentityAllowDisabledSelectFormAttributeRenderer extends IdentitySelectFormAttributeRenderer {

  /**
   * Supports select disbaled identity.
   *
   * @return {Boolean} false => supports select disbaled identity.
   */
  isDisableable() {
    return false;
  }
}
