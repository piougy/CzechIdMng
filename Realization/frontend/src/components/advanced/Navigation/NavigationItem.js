'use strict';

import React, { PropTypes } from 'react'
import { Link }  from 'react-router';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Single navigation item
 */
export default class NavigationItem extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  render () {
    const { id, className, to, icon, iconColor, active, title, titlePlacement, text, rendered, showLoading, ...others } = this.props;
    const itemClassNames = classnames(className, { active: active });
    const linkClassNames = classnames({ active: active });
    //
    if (!rendered) {
      return null;
    }

    if (!to) {
      this.getLogger().error(`[Advanced.NavigationItem] item [${id}] in module descriptor has to be repaired. Target link is undefined and will be hidden.`);
      return null;
    }

    return (
      <li className={itemClassNames} {...others}>
        <Basic.Tooltip id={`${id}-tooltip`} placement={titlePlacement} value={title} delayShow={200}>
          {
            <Link to={to} className={linkClassNames}>
              <Basic.Icon icon={showLoading ? 'refresh' : icon ? icon : 'fa:circle-o'} color={iconColor} showLoading={showLoading}/>
              {text}
            </Link>
          }
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
