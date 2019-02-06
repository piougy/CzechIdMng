import React from 'react';
import { faBuilding, faCircle, faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { i18n } from '../../../services/LocalizationService';
//
import AbstractIcon from './AbstractIcon';

/**
 * Icon for the "main" identity contract - layers are used.
 * - https://fontawesome.com/how-to-use/on-the-web/styling/layering
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class MainContractIcon extends AbstractIcon {

  constructor(props) {
    super(props);
  }

  renderIcon() {
    return (
      <span className="fa-layers fa-fw" title={ i18n('entity.IdentityContract.main.help') }>
        <FontAwesomeIcon icon={ faBuilding } />
        <FontAwesomeIcon icon={ faCircle } style={{ color: '#008AFF' }} transform="shrink-3 up-3 right-3" />
        <FontAwesomeIcon icon={ faArrowUp } inverse transform="shrink-5 up-3 right-3" />
      </span>
    );
  }
}
