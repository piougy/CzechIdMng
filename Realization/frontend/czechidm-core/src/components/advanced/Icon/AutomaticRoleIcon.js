import React from 'react';
import { faKey, faMagic } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Automatically assigned role icon.
 *
 * @author Radek Tomiška
 * @since 10.7.1
 */
export default class AutomaticRoleIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faKey } transform="down--4 shrink-4" style={{ color: '#ccc' }}/>
        <FontAwesomeIcon icon={ faMagic } transform="down-3 right-6 shrink-4"/>
      </span>
    );
  }
}
