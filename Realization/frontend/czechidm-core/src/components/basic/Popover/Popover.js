import React, { PropTypes } from 'react';
import { OverlayTrigger, Popover } from 'react-bootstrap';
import _ from 'lodash';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

export default class BasicPopover extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { id, rendered, children, value, text, title, placement, showLoading, trigger, delayShow, level, className, icon, ...others } = this.props;
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
      'popover-' + level,
      className
    );

    return (
      <OverlayTrigger
        ref="popover"
        trigger={trigger}
        placement={placement}
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
              _value

            }
          </Popover>
        }
        delayShow={delayShow}
        {...others}>
        { children }
      </OverlayTrigger>
    );
  }
}

BasicPopover.propTypes = {
  ...AbstractComponent.propTypes,
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
  trigger: PropTypes.oneOf(['click', 'hover', 'focus']),
  /**
   * A millisecond delay amount before showing the Popover once triggered.
   */
  delayShow: PropTypes.number
};

BasicPopover.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default',
  placement: 'bottom',
  trigger: ['hover', 'focus']
};
