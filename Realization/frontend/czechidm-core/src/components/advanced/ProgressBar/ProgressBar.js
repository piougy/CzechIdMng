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
    return this.i18n('component.basic.ProgressBar.label', { min, max: (max === null ? '?' : max), now: (now === null ? '?' : now) });
  }

  render() {
    const { rendered, active, label, className, bars, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    // add component className
    const classNames = classnames(
      'advanced-progress-bar',
      className
    );
    if (bars) {
      const stackedBars = [];
      bars.forEach(bar => {
        stackedBars.push(
          <Basic.ProgressBar isChild min={bar.min} max={this.props.max} now={bar.now} bsStyle={bar.bsStyle} active={active} />
        );
      });
      return (
        <span className={classNames}>
          <Basic.ProgressBar style={ { marginBottom: 0} }>
            { stackedBars }
          </Basic.ProgressBar>
          <div className="text-center">
            { this._resolveLabel() }
          </div>
        </span>
      );
    }
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
