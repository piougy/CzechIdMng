import React from 'react';
import { faSquare } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Icon for form definition.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class FormDefinitionIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faSquare } transform="grow-4 right--2"/>
        <span className="fa-layers-text fa-inverse" style={{ left: '40%', marginTop: -1, fontSize: '0.85em', fontWeight: 900 }}>F</span>
      </span>
    );
  }
}
