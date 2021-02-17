import React from 'react';
import { MenuItem } from 'react-bootstrap';
//
import Icon from '../Icon/Icon';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Split button item.
 *
 * @author Radek Tomiška
 */
class ButtonMenuItem extends AbstractComponent {

  render() {
    const {
      rendered,
      eventKey,
      onClick,
      children,
      icon,
      showLoading,
      showLoadingIcon,
      showLoadingText
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _showLoadingText = children;
    if (showLoadingText) {
      _showLoadingText = showLoadingText;
    }
    //
    return (
      <MenuItem onClick={ onClick } eventKey={ eventKey }>
        {
          showLoading
          ?
          <span>
            {
              showLoadingIcon
              ?
              <Icon type="fa" icon="refresh" showLoading/>
              :
              null
            }
            {
              showLoadingIcon && _showLoadingText
              ?
              '\u00a0'
              :
              null
            }
            { _showLoadingText }
          </span>
          :
          <span>
            <Icon
              value={ icon }
              className="icon-left"
              style={ (children && React.Children.count(children) > 0) ? { marginRight: 5 } : {} }/>
            { children }
          </span>
        }
      </MenuItem>
    );
  }
}


module.exports = ButtonMenuItem;
