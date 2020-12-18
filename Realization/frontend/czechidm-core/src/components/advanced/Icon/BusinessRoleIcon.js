import React from 'react';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { faCircle } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { i18n } from '../../../services/LocalizationService';
//
import AbstractIcon from './AbstractIcon';

/**
 * Icon for the business role. It's combined from different icons - layers are used.
 * - https://fontawesome.com/how-to-use/on-the-web/styling/layering
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class BusinessRoleIcon extends AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;
    //
    return (
      <span
        className={ this.getClassName('fa-layers fa-fw') }
        title={ i18n('entity.RoleComposition._type') }
        style={ iconSize ? {} : { fontSize: '0.9em' } }>
        <FontAwesomeIcon icon={ faKey } transform="rotate-315 up-1 right-0.3" style={{ color: '#ccc' }} />
        <FontAwesomeIcon icon={ faKey } transform="up-3.2 right--3"/>
        <FontAwesomeIcon icon={ faCircle } transform="up-9 right-5 shrink-6"/>
      </span>
    );
  }
}
