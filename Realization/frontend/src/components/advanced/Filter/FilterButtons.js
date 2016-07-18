import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';


export default class FilterButtons extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  _cancelFilter(event) {
    const { cancelFilter } = this.props;
    if (event) {
      event.preventDefault();
    }
    if (cancelFilter) {
      cancelFilter(event);
    }
  }

  render() {
    const { rendered, showLoading, ...others } = this.props;
    if (!rendered || showLoading) {
      return null;
    }
    //
    return (
      <span>
        <Basic.Button level="link" onClick={this._cancelFilter.bind(this)}>
          {this.i18n('button.filter.cancel')}
        </Basic.Button>
        <Basic.Button level="primary" type="submit">
          {this.i18n('button.filter.use')}
        </Basic.Button>
      </span>
    );
  }
}

FilterButtons.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Callback, when filter is canceled
   * @type {function} function(event)
   */
  cancelFilter: PropTypes.func
};
FilterButtons.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};
