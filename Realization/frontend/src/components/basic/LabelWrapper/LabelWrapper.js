

import React, { PropTypes } from 'react';
import classNames from 'classnames';
import merge from 'object-assign';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';
import Tooltip from '../Tooltip/Tooltip';

class LabelWrapper extends AbstractFormComponent {

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
    //this.refs.input.focus();
  }

  onChange(event){
    //super.onChange(event);
  }

  getBody(feedback) {
    const { type, ref, labelSpan, label, componentSpan, placeholder, style, required, help } = this.props;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value){
      showAsterix = true;
    }
    let title = this.getValidationResult() != null ? this.getValidationResult().message : null;

    let render =  (
      <div className={showAsterix ? 'has-feedback' : ''}>
        {
          !label
          ||
          <label
            for={ref}
            className={labelClassName}>
            {label}
          </label>
        }
        <div className={componentSpan} style={{ whiteSpace: 'nowrap' }}>
          <Tooltip ref="popover" placement="right" value={title}>
            <span>
              {this.props.children}
              {feedback != null ? feedback : showAsterix ? (<span className="form-control-feedback" style={{color: 'red', zIndex : 0}}>*</span>):''}
            </span>
          </Tooltip>
          <HelpIcon content={help} style={{ marginLeft: '3px' }}/>
        </div>
      </div>
    );
    return render;
  }
}

LabelWrapper.propTypes = {
  ...AbstractFormComponent.propTypes,
  type: React.PropTypes.string,
  placeholder: React.PropTypes.string,
  help: React.PropTypes.string
}

LabelWrapper.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  type: 'text'
}


export default LabelWrapper;
