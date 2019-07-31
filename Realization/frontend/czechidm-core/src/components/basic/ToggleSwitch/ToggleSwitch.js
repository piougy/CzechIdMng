import React from 'react';
import classNames from 'classnames';
import Joi from 'joi';
import Switch from 'react-toggle-switch';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';

/**
 * ToggleSwitch component
 *
 * @author Vít Švanda
 * @extends AbstractFormComponent
 */
class ToggleSwitch extends AbstractFormComponent {

  onChange(event) {
    if (this.props.onChange) {
      this.props.onChange(event);
    }
    this.setState({
      value: !this.state.value
    }, () => {
      this.validate();
    });
  }

  /**
   * Focus input checkbox
   */
  focus() {
    this.refs.toggleswitch.focus();
  }

  getRequiredValidationSchema() {
    return Joi.boolean().valid(true);
  }

  _isChecked(value) {
    if (value === null) {
      return false;
    }
    if (value === undefined) {
      return false;
    }
    if (value === true) {
      return true;
    }
    if ((typeof value === 'string') && value.toLowerCase() === 'true') {
      return true;
    }
    //
    return false;
  }

  getBody() {
    const { labelSpan, label, componentSpan } = this.props;
    const { value, readOnly, disabled } = this.state;
    const className = classNames(
      labelSpan,
      componentSpan
    );

    return (
      <div className={className}>
        <div className="checkbox">
          <Tooltip trigger={['click', 'hover']} ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
            <div style={{display: 'flex', alignItems: 'middle'}}>
              <Switch
                onClick={this.onChange.bind(this)}
                on={this._isChecked(value)}
                ref="toggleswitch"
                theme="graphite-small"
                disabled={ readOnly || disabled }
                readOnly={ readOnly }
              />
              <span style={{marginLeft: '5px', marginTop: '3px'}}>
                {label}
              </span>
              { this.renderHelpIcon() }
            </div>
          </Tooltip>
          {this.props.children}
          { this.renderHelpBlock() }
        </div>
      </div>
    );
  }
}

ToggleSwitch.propTypes = {
  ...AbstractFormComponent.propTypes
};

ToggleSwitch.defaultProps = {
  ...AbstractFormComponent.defaultProps
};


export default ToggleSwitch;
