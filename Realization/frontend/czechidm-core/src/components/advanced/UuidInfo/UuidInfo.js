import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

const MAX_UUID_LENGTH = 7;

/**
 * Shows uuid - shorten by default - full view in popover for copy
 * - copy identifier after focus into clipboard automatically
 *
 * @author Radek Tomiška
 */
export default class UuidInfo extends Basic.AbstractContextComponent {

  getComponentKey() {
    return 'component.advanced.UuidInfo';
  }

  /**
   * Shortens given value, if value is type of string, returns value otherwise.
   *
   * @param  {object} value
   * @return {string|object}
   */
  shorten(value) {
    if (typeof value === 'string') {
      // FIXME: Ui.Utils.shorten ...
      const { uuidEnd } = this.props;
      if (uuidEnd) {
        return value.substr(value.length - 7, value.length);
      }
      return value.substring(0, MAX_UUID_LENGTH);
    }
    return value;
  }

  render() {
    const {
      rendered,
      showLoading,
      value,
      style,
      className,
      header,
      placement,
      buttons
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading/>
      );
    }
    // we don't have anything to render
    if (!value) {
      return null;
    }
    const classNames = classnames(
      'uuid-info',
      className
    );
    //
    return (
      <Basic.Popover
        trigger={[ 'click' ]}
        placement={ placement }
        className={
          classnames(
            'abstract-entity-info-popover',
            'uuid-info-popover-value')
        }
        value={
          <Basic.Panel
            className={
              classnames(
                'panel-success',
                { 'no-border': (!header && (buttons === null || buttons.length === 0)) }
              )
            }>
            <Basic.PanelHeader rendered={ header !== null && header !== false && header !== '' }>
              { header }
            </Basic.PanelHeader>
            <Basic.PanelBody>
              <input
                ref="input"
                type="text"
                value={ value }
                readOnly
                onClick={ () => {
                  // ~ctrl+c
                  this.refs.input.select();
                  document.execCommand('copy');
                  this.addMessage({ level: 'success', message: this.i18n('copy.message') });
                }}/>
            </Basic.PanelBody>
            <Basic.PanelFooter rendered={ buttons !== null && buttons.length > 0 }>
              { buttons }
            </Basic.PanelFooter>
          </Basic.Panel>
        }>
        {
          <span
            className={ classNames }
            style={ style }>
            <Basic.Button
              level="link"
              className="embedded"
              onClick={ e => e.preventDefault() }>
              { this.shorten(value) }
            </Basic.Button>
          </span>
        }
      </Basic.Popover>
    );
  }
}

UuidInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * uuid, entity identifier
   */
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]).isRequired,
  /**
   * Shows ending uuid characters in shorten label.
   */
  uuidEnd: PropTypes.bool,
  /**
   * Buttons are shown in popover footer
   */
  buttons: PropTypes.arrayOf(PropTypes.element),
  /**
   * Popover Header
   */
  header: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * Popover position
   */
  placement: PropTypes.oneOf(['top', 'bottom', 'right', 'left'])
};
UuidInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  uuidEnd: false,
  buttons: [],
  placement: 'bottom',
  header: null
};
