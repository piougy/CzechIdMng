import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import HelpIcon from '../HelpIcon/HelpIcon';
import Tooltip from '../Tooltip/Tooltip';

class LabelWrapper extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  /**
   * Focus input field
   */
  focus() {
    // this.refs.input.focus();
  }

  onChange() {
    // super.onChange(event);
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, required, help } = this.props;
    //
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }
    const title = this.getValidationResult() != null ? this.getValidationResult().message : null;

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
              {this.props.children}
              {
                feedback
                ||
                showAsterix
                ?
                <span className="form-control-feedback" style={{color: 'red', zIndex: 0}}>*</span>
                :
                ''
              }
            </span>
          </Tooltip>
          <HelpIcon content={help} style={{ marginLeft: '3px' }}/>
        </div>
      </div>
    );
  }
}

LabelWrapper.propTypes = {
  ...AbstractFormComponent.propTypes,
  type: PropTypes.string,
  placeholder: PropTypes.string,
  help: PropTypes.string
};

LabelWrapper.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  type: 'text'
};


export default LabelWrapper;
