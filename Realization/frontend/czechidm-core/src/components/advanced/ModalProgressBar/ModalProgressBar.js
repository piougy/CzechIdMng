import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import ProgressBar from '../ProgressBar/ProgressBar';

export default class ModalProgressBar extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, show, counter, count, text, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    //
    return (
      <Basic.Modal
        show={show}
        showLoading={showLoading}
        bsSize="large"
        backdrop="static"
        {...others}>
        <Basic.Modal.Header text={text}/>
        <Basic.Modal.Body>
          <ProgressBar max={count} now={counter} style={{ marginBottom: 0}} rendered={!showLoading}/>
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
};
ModalProgressBar.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  show: false,
  counter: 0,
  count: 0,
  text: 'Probíhá zpracování' // TODO: locale
};
