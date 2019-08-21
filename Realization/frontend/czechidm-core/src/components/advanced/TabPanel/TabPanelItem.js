import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Tab panel item
 *
 * @author Radek Tomiška
 */
export default class TabPanelItem extends Basic.AbstractContextComponent {

  render() {
    const { className, to, active, icon, iconColor, showLoading, ...others } = this.props;
    const itemClassNames = classnames(
      { 'list-group-item': false },
      { 'active': active === true },
      className
    );
    // icon resolving
    let iconContent = null;
    let _icon = ( icon === undefined || icon === null ? 'fa:circle-o' : icon );
    if (showLoading) {
      _icon = 'refresh';
    }
    if (_icon) {
      iconContent = (
        <Basic.Icon icon={ _icon } color={ iconColor } showLoading={ showLoading }/>
      );
    }
    //
    return (
      <li className={ itemClassNames }>
        <Link to={to}>
          { iconContent }
          { this.props.children }
        </Link>
      </li>
    );
  }
}

TabPanelItem.propTypes = {
  to: PropTypes.string,
  title: PropTypes.string,
  active: PropTypes.bool
};
