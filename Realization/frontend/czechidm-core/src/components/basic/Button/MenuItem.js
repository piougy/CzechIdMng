import React from 'react';
import { MenuItem } from 'react-bootstrap';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Split button item.
 *
 * @author Radek Tomiška
 */
class ButtonMenuItem extends AbstractComponent {

  render() {
    const { rendered, eventKey, onClick, children, showLoading } = this.props;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return null;
    }
    return (
      <MenuItem onClick={ onClick } eventKey={ eventKey }>
        { children }
      </MenuItem>
    );
  }
}


module.exports = ButtonMenuItem;
