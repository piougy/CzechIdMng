import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import classnames from 'classnames';
//
import * as Basic from '../../basic';
import ComponentService from '../../../services/ComponentService';

const componentService = new ComponentService();

/**
 * Tab panel item
 *
 * @author Radek Tomiška
 */
export default class TabPanelItem extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    const { className, to, active, icon, iconComponent, iconColor, showLoading, ...others } = this.props;
    const itemClassNames = classnames(
      { 'list-group-item': false },
      { 'active': active === true },
      className
    );
    // icon resolving
    let iconContent = null;
    if (iconComponent) {
      const component = componentService.getIconComponent(iconComponent);
      if (component) {
        const Icon = component.component;
        iconContent = (
          <Icon color={ iconColor }/>
        );
      }
    } else {
      let _icon = ( icon === undefined || icon === null ? 'fa:circle-o' : icon );
      if (showLoading) {
        _icon = 'refresh';
      }
      if (_icon) {
        iconContent = (
          <Basic.Icon icon={ _icon } color={ iconColor } showLoading={ showLoading }/>
        );
      }
    }

    return (
      <li className={itemClassNames}>
        <Link to={to}>
          { iconContent }
          {this.props.children}
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
