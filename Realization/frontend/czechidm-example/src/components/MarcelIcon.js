import React from 'react';
import { faUserTie } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import { Advanced } from 'czechidm-core';

/**
 * Marcel icon :)
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class MarcelIcon extends Advanced.AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faUserTie } />
        <span className="fa-layers-counter" style={{ fontSize: '1.4em' }}>M</span>
      </span>
    );
  }
}
