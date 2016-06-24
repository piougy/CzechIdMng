'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
//
import * as Basic from '../../basic';

export default class ModalProgressBar extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, show, counter, count, text, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    let label = (<span style={{color: '#000'}}>{this.i18n('component.basic.ProgressBar.start')}</span>);
    if (counter > 0) {
      label = this.i18n('component.basic.ProgressBar.processed') + ' %(now)s / %(max)s';
    }

    return (
      <Basic.Modal
        show={show}
        showLoading={showLoading}
        bsSize="large"
        backdrop="static"
        {...others}>
        <Basic.Modal.Header text={text}/>
        <Basic.Modal.Body>
          <Basic.ProgressBar min={0} max={count} now={counter} label={label} active style={{ marginBottom: 0}} rendered={!showLoading}/>
        </Basic.Modal.Body>
      </Basic.Modal>
    );
  }
}

ModalProgressBar.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * ProgressBar is shown
   */
  show: PropTypes.bool,
  /**
   * Main title - modal header text
   */
  text: PropTypes.string,
  /**
   * Current processed counter
   */
  counter: PropTypes.number,
  /**
   * Size / maximum
   */
  count: PropTypes.number
}
ModalProgressBar.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  show: false,
  counter: 0,
  count: 0,
  text: 'Probíhá zpracování' // TODO: locale
}
