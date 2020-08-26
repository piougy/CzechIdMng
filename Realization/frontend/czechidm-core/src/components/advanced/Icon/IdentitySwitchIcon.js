import React from 'react';
//
import AbstractIcon from './AbstractIcon';
import * as Basic from '../../basic';

/**
 * Identity icon - switch uiser.
 *
 * @author Radek Tomiška
 * @since 10.5.0
 */
export default class IdentitySwitchIcon extends AbstractIcon {

  renderIcon() {
    return (
      <Basic.Icon icon="fa:user-secret"/>
    );
  }
}
