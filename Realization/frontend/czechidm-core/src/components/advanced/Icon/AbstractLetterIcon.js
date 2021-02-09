import React from 'react';
import { faSquare } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Icon with simple leter decorator.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class AbstractLetterIcon extends AbstractIcon {

  /**
   * Get / render letter.
   */
  renderLetter() {
    return 'I';
  }

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faSquare } transform="grow-4 right--2"/>
        <span className="fa-layers-text fa-icon-letter">{ this.renderLetter() }</span>
      </span>
    );
  }
}
