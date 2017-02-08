import React from 'react';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * ProgressBar with default label from localization
 *
 * @author Radek Tomiška
 */
export default class ProgressBar extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * Resolves default label from localization
   *
   * @return {string}
   */
  _resolveLabel() {
    const { label, now, min, max } = this.props;
    //
    if (label) { // label was given
      return label;
    }
    //
    // when progress is smal, then label needs to be inversed - will be visible
    //
    // resolve default label from localization
    if (now === 0 || max === 0) {
      // start label
      return this.i18n('component.basic.ProgressBar.start');
    }
    return this.i18n('component.basic.ProgressBar.label', { min, max, now });
  }

  render() {
    const { rendered, label, className, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    // add component className
    const classNames = classnames(
      'advanced-progress-bar',
      className
    );
    //
    return (
      <span className={classNames}>
        <Basic.ProgressBar
          label={this._resolveLabel()}
          {...others}/>
      </span>
    );
  }
}

ProgressBar.propTypes = {
  ...Basic.DateValue.propTypes
};

ProgressBar.defaultProps = {
  ...Basic.DateValue.defaultProps
};
