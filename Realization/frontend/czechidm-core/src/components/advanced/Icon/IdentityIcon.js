import React from 'react';
//
import AbstractIcon from './AbstractIcon';
import * as Basic from '../../basic';

/**
 * Identity icon
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
export default class IdentityIcon extends AbstractIcon {

  renderIcon() {
    const { identity, ...others } = this.props;
    //
    if (!identity || !identity.disabled) {
      return (
        <Basic.Icon icon="component:enabled-identity" { ...others }/>
      );
    }
    //
    return (
      <Basic.Icon icon="component:disabled-identity" { ...others }/>
    );
  }
}
