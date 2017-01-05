import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';


class ScriptArea extends AbstractFormComponent {

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
    this.refs.input.focus();
  }

  onChange(newValue) {
    if (this.props.onChange) {
      this.props.onChange(newValue);
    } else {
      this.setState({
        value: newValue
      }, () => {
        this.validate();
      });
    }
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, required, mode, helpBlock, height } = this.props;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }

    // Workaround - Import for AceEditor must be here. When was on start, then not working tests (error is in AceEditor);
    let AceEditor;
    AceEditor = require('react-ace').default;
    require('brace/mode/groovy');
    require('brace/theme/github');
    const title = this.getValidationResult() != null ? this.getValidationResult().message : null;
    return (
      <div className={ showAsterix ? 'has-feedback' : ''}>
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
              <AceEditor
                ref="input"
                mode={mode}
                width={null}
                height={height}
                className={className}
                title={this.getValidationResult() != null ? this.getValidationResult().message : ''}
                readOnly={this.state.readOnly}
                enableBasicAutocompletion
                enableLiveAutocompletion
                theme="github"
                onChange={this.onChange}
                value={this.state.value || ''}
                tabSize={4}
                fontSize={14}
                spellcheck
                showGutter
                editorProps={{$blockScrolling: true}}
              />
              {
                feedback
                ||
                !showAsterix
                ||
                <span className="form-control-feedback" style={{color: 'red', zIndex: 0}}>*</span>
              }
            </span>
          </Tooltip>
          {
            !helpBlock
            ||
            <span className="help-block" style={{ whiteSpace: 'normal' }}>{helpBlock}</span>
          }
        </div>
      </div>
    );
  }
}

ScriptArea.propTypes = {
  ...AbstractFormComponent.propTypes,
  helpBlock: PropTypes.string,
  mode: PropTypes.string,
  height: PropTypes.string
};

ScriptArea.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  mode: 'groovy',
  height: '10em'
};

export default ScriptArea;
