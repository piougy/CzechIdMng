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

  constructor(props) {
    super(props);
  }

  renderIcon() {
    return (
      <span className="fa-layers fa-fw fa-6x">
        <FontAwesomeIcon icon={ faUserTie } />
        <span className="fa-layers-counter" style={{ fontSize: '0.6em' }}>Marcel</span>
      </span>
    );
  }
}
