import React from 'react';
import { faKey, faPlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Remove assigned identity role.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class IdentityRoleAddIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faKey }/>
        <FontAwesomeIcon icon={ faPlus } transform="down-5 right-8 shrink-6"/>
      </span>
    );
  }
}
