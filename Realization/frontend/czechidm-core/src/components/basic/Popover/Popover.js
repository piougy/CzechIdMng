import React from 'react';
import PropTypes from 'prop-types';
import { OverlayTrigger, Popover } from 'react-bootstrap';
import _ from 'lodash';
import classnames from 'classnames';
import { HashRouter as Router } from 'react-router-dom';
import { Provider } from 'react-redux';
//
import IdmContext from '../../../context/idm-context';
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Icon from '../Icon/Icon';
import Tooltip from '../Tooltip/Tooltip';

/**
 * Overlay with popover
 *
 * @author Radek Tomiška
 */
export default class BasicPopover extends AbstractContextComponent {

  /**
   * Close popover
   */
  close() {
    if (this.refs.popover) {
      this.refs.popover.hide();
    }
  }

  render() {
    const {
      id,
      rendered,
      children,
      value,
      text,
      title,
      placement,
      showLoading,
      trigger,
      delayShow,
      level,
      className,
      icon,
      rootClose,
      ...others
    } = this.props;
    if (!rendered || (!children)) {
      return null;
    }
    let _id = id;
    if (!_id) {
      _id = _.uniqueId('tooltip_');
    }
    // text - value alias
    const _value = value || text;
    //
    const classNames = classnames(
      'basic-popover',
      `popover-${ level }`,
      className
    );

    return (
      <OverlayTrigger
        ref="popover"
        trigger={ trigger }
        rootClose={ rootClose }
        placement={ placement }
        overlay={
          <Popover
            id={_id}
            className={classNames}
            title={
              !title
              ?
              null
              :
              <div>
                {
                  !icon
                  ||
                  <div className="basic-popover-icon"><Icon icon={icon}/></div>
                }
                <div className={icon ? 'basic-popover-title' : ''}>
                  { title }
                </div>
              </div>
            }>
            {
              showLoading
              ?
              <Icon value="Refresh" showLoading/>
              :
              ( // @todo-upgrade-10 - I had to wrapp the value to the Redux provider and Router,
                // because React-bootstrap Popover uses for generating value new instance of React.
                // So Redux and Router wrapping doesn't work!
              <Provider store={this.context.store}>
                <IdmContext.Provider value={{store: this.context.store}}>
                  <Router>
                    {_value}
                  </Router>
                </IdmContext.Provider>
              </Provider>
              )

            }
          </Popover>
        }
        delayShow={ delayShow }
        { ...others }>
        { children }
      </OverlayTrigger>
    );
  }
}

BasicPopover.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * Popover identifier
   */
  id: PropTypes.string,
  /**
   * Popover level / css / class
   */
  level: PropTypes.oneOf(['default', 'warning']),
  /**
   * Popover position
   */
  placement: PropTypes.oneOf(['top', 'bottom', 'right', 'left']),
  /**
   * Popover value / text
   */
  title: PropTypes.string,
  /**
   * Popover value / text
   */
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Popover value / text - alias to value
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Specify which action or actions trigger popover visibility
   */
  trigger: PropTypes.arrayOf(PropTypes.oneOf(['click', 'hover', 'focus'])),
  /**
   * A millisecond delay amount before showing the Popover once triggered.
   */
  delayShow: PropTypes.number,
  /**
   * Specify whether the overlay should trigger onHide when the user clicks outside the overlay
   */
  rootClose: PropTypes.bool
};

BasicPopover.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  level: 'default',
  placement: 'bottom',
  trigger: ['hover', 'focus', 'click'],
  delayShow: Tooltip.defaultProps.delayShow,
  rootClose: true
};
