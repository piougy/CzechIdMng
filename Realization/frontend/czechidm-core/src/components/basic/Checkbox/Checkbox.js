import React from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';

class Checkbox extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  onChange(event) {
    if (this.props.onChange) {
      this.props.onChange(event);
    }
    this.setState({
      value: event.currentTarget.checked
    }, () => {
      this.validate();
    });
  }

  /**
   * Focus input checkbox
   */
  focus() {
    this.refs.checkbox.focus();
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
          {/* focus can not be added for checkbox - event colision when checkbox  */}
          <Tooltip trigger={['click', 'hover']} ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
            <label>
              <input
                type="checkbox"
                ref="checkbox"
                disabled={ readOnly || disabled }
                onChange={ this.onChange }
                checked={ this._isChecked(value) }
                readOnly={ readOnly }/>
              <span>
                {label}
              </span>
              { this.renderHelpIcon() }
            </label>
          </Tooltip>
          {this.props.children}
          { this.renderHelpBlock() }
        </div>
      </div>
    );
  }
}

Checkbox.propTypes = {
  ...AbstractFormComponent.propTypes
};

Checkbox.defaultProps = {
  ...AbstractFormComponent.defaultProps
};


export default Checkbox;
