import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import HelpIcon from '../HelpIcon/HelpIcon';
import Tooltip from '../Tooltip/Tooltip';

class TextField extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this._setMinAndMaxValidation();
  }

  _setMinAndMaxValidation() {
    const { min, max } = this.props;
    // minimal one must be set or nothing..
    if (min || max) {
      let validation = Joi.string();
      if (min && max) {
        validation = validation.concat(Joi.string().min(min).max(max));
      } else if (min) {
        validation = validation.concat(Joi.string().min(min));
      } else if (max) {
        if (!this.props.required) {
          // if set only max is necessary to set allow null and empty string
          validation = validation.concat(Joi.string().max(max).allow(null).allow(''));
        } else {
          // if set prop required it must not be set allow null or empty string
          validation = validation.concat(Joi.string().max(max));
        }
      }
      this.state = { validation };
    }
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  /**
   * Focus input field
   */
  focus() {
    this.refs.input.focus();
  }

  onChange(event) {
    super.onChange(event);
    this.refs.popover.show();
  }

  getBody(feedback) {
    const { type, labelSpan, label, componentSpan, placeholder, style, required, help } = this.props;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }
    const validationResult = this.getValidationResult();
    const title = validationResult != null ? validationResult.message : null;
    const component = (
      <input
        ref="input"
        type={type}
        className={className}
        disabled={this.state.disabled}
        placeholder={placeholder}
        onChange={this.onChange.bind(this)}
        value={this.state.value || ''}
        style={style}
        readOnly={this.state.readOnly}/>
    );

    return (
      <div className={showAsterix ? 'has-feedback' : ''}>
        {
          !label
          ||
          <label
            className={labelClassName}>
            {label}
          </label>
        }
        <div className={componentSpan} style={{ whiteSpace: 'nowrap' }}>
          <Tooltip ref="popover" placement="right" value={title}>
            <span>
              {component}
              {
                feedback
                ||
                !showAsterix
                ||
                <span className="form-control-feedback" style={{color: 'red', zIndex: 0}}>*</span>
              }
            </span>
          </Tooltip>
          <HelpIcon content={help} style={{ marginLeft: '3px' }}/>
        </div>
      </div>
    );
  }
}

TextField.propTypes = {
  ...AbstractFormComponent.propTypes,
  type: PropTypes.string,
  placeholder: PropTypes.string,
  help: PropTypes.string,
  min: PropTypes.number,
  max: PropTypes.number
};

TextField.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  type: 'text'
};

export default TextField;
