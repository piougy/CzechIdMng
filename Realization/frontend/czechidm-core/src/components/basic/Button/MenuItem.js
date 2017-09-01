import { MenuItem } from 'react-bootstrap';
import React from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Split button item
 *
 * @author Radek Tomiška
 */
class ButtonMenuItem extends AbstractComponent {
  constructor(props) {
    super(props);
  }

  render() {
    const {rendered, eventKey, onClick, children, showLoading} = this.props;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return null;
    }
    return (<MenuItem onClick={onClick} eventKey={eventKey} children={children}/>);
  }
}


module.exports = ButtonMenuItem;
