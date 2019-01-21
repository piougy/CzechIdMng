import React from 'react';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { faCircle } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import * as Basic from '../../../components/basic';


/**
 * Icon for the business role. It's combined from different icons - layers are used.
 * - https://fontawesome.com/how-to-use/on-the-web/styling/layering
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class BusinessRoleIcon extends Basic.AbstractComponent {

  constructor(props) {
    super(props);
  }
  render() {
    const {
      rendered,
      showLoading,
      color
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="fa:refresh" showLoading color={ color } />
      );
    }
    const _style = {};
    if (color) {
      _style.color = color;
    }
    //
    return (
      <span className="fa-layers fa-fw">
        <FontAwesomeIcon icon={ faKey } transform="rotate-315 up-1 right-0.3" style={{ color: '#ccc' }} />
        <FontAwesomeIcon icon={ faKey } transform="up-3.2 right--3" style={ _style }/>
        <FontAwesomeIcon icon={ faCircle } transform="up-9 right-5 shrink-6" style={ _style }/>
      </span>
    );
  }
}

BusinessRoleIcon.propTypes = {
  ...Basic.AbstractComponent.propTypes
};

BusinessRoleIcon.defaultProps = {
  ...Basic.AbstractComponent.defaultProps
};
