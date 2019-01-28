import React from 'react';
import { faBookmark, faKey } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import { Advanced } from 'czechidm-core';

/**
 * Role icon - override role icon in ecxample module.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class ExampleRoleIcon extends Advanced.AbstractIcon {

  constructor(props) {
    super(props);
  }

  renderIcon() {
    return (
      <span className="fa-layers fa-fw">
        <FontAwesomeIcon icon={ faBookmark } />
        <FontAwesomeIcon icon={ faKey } transform="shrink-7 up-2" style={{ color: '#fff' }}/>
      </span>
    );
  }
}
