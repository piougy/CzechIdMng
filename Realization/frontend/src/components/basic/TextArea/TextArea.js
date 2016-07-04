'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
import merge from 'object-assign';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';

class TextArea extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  getRequiredValidationSchema(){
    return Joi.string().required();
  }

  /**
   * Focus input field
   */
  focus() {
    this.refs.input.focus();
  }

  getBody(feedback){
    const { labelSpan, label, componentSpan, placeholder, style, required } = this.props;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value){
      showAsterix = true;
    }
    let title = this.getValidationResult() != null ? this.getValidationResult().message : null;

    return (
      <div className={ showAsterix ?'has-feedback':''}>
        {
          !label
          ||
          <label
            className={labelClassName}>
            {label}
          </label>
        }

        <div className={componentSpan}>
          <Tooltip ref="popover" placement="right" value={title}>
            <span>
              <textarea
                ref="input"
                className={className}
                title={this.getValidationResult() != null ? this.getValidationResult().message : ''}
                disabled={this.state.disabled}
                placeholder={placeholder}
                rows={this.props.rows}
                style={style}
                readOnly={this.state.readOnly}
                onChange={this.onChange}
                value={this.state.value || ''}/>
              {feedback != null ? feedback : showAsterix ? (<span className="form-control-feedback" style={{color: 'red', zIndex : 0}}>*</span>):''}
            </span>
          </Tooltip>
        </div>
      </div>
    );
  }
}

TextArea.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: React.PropTypes.string,
  rows: React.PropTypes.number
}

TextArea.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  rows: 3
}

export default TextArea;
