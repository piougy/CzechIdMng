import React from 'react';
import { faUserTie, faPlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Remove assigned identity role.
 *
 * @author Ondrej Husnik
 * @since 10.8.0
 */
export default class ContractGuaranteeAddIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faUserTie }/>
        <FontAwesomeIcon icon={ faPlus } transform="up-3 right-9 shrink-6"/>
      </span>
    );
  }
}
