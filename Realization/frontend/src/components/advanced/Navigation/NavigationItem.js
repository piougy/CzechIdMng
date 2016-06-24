'use strict';

import React, { PropTypes } from 'react'
import { Link }  from 'react-router';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Single navigation item
 */
export default class NavigationItem extends Basic.AbstractComponent {

  constructor(props, context) {
    super(props, context);
  }

  render () {
    const { id, className, to, icon, active, title, titlePlacement, text, rendered, showLoading, ...others } = this.props;
    const itemClassNames = classnames(className, { active: active });
    const linkClassNames = classnames({ active: active });
    //
    if (!rendered) {
      return null;
    }

    return (
      <li className={itemClassNames} {...others}>
        <Basic.Tooltip id={`${id}-tooltip`} placement={titlePlacement} value={title} delayShow={200}>
          <Link to={to} className={linkClassNames}>
            <Basic.Icon icon={icon ? icon : showLoading ? 'refresh' : null} showLoading={showLoading}/>
            {text}
          </Link>
        </Basic.Tooltip>
      </li>
    );
  }
}

NavigationItem.propTypes = {
  ...Basic.AbstractComponent.propTypes,
  id: PropTypes.string,
  to: PropTypes.string,
  title: PropTypes.string,
  icon: PropTypes.string,
  active: PropTypes.bool
}

NavigationItem.defaultProps = {
  ...Basic.AbstractComponent.defaultProps,
  active: false,
  icon: null
}
