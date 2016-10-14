import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';

/**
 * "<hr>" in navigation
 */
export default class NavigationSeperator extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    const { text, ...others } = this.props;
    return (
      <li className="nav-separator">
        {text}
      </li>
    );
  }
}

NavigationSeperator.propTypes = {
  text: PropTypes.string
};

NavigationSeperator.defaultProps = {
};
