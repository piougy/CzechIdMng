import { MenuItem } from 'react-bootstrap';
import React from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

class ButtonMenuItem extends AbstractComponent {
  constructor(props) {
    super(props);
  }

  render() {
    const {rendered} = this.props;
    if (!rendered) {
      return null;
    }
    return (<MenuItem {...this.props}/>);
  }
}


module.exports = ButtonMenuItem;
