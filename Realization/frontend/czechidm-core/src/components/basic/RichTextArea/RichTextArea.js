import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
import { Editor, EditorState, ContentState, RichUtils } from 'draft-js';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';

/**
 * Based on Draf.js
 * TODO: to and from html conversion (draft-js-export-html - dependency hell)
 * TODO: custom styles and controlls
 *
 */
class RichTextArea extends AbstractFormComponent {

  constructor(props) {
    super(props);
    const editorState = this.props.value ? this._createEditorState(this.props.value) : EditorState.createEmpty();
    this.state = {
      ...this.state,
      editorState,
      value: editorState.getCurrentContent().getPlainText()
    };
  }

  _handleKeyCommand(command) {
    const { editorState } = this.state;
    const newState = RichUtils.handleKeyCommand(editorState, command);
    if (newState) {
      this.onChange(newState);
      return true;
    }
    return false;
  }

  /**
   * Creates EditorState from given value
   *
   * @param  {string} value
   * @return {EditorState}
   */
  _createEditorState(value) {
    return EditorState.createWithContent(ContentState.createFromText(value));
  }

  getValidationDefinition(required) {
    const { min, max } = this.props;
    let validation = super.getValidationDefinition(min ? true : required);

    if (min && max) {
      validation = validation.concat(Joi.string().min(min).max(max).disallow(null).disallow(''));
    } else if (min) {
      validation = validation.concat(Joi.string().min(min));
    } else if (max) {
      if (!required) {
        // if set only max is necessary to set allow null and empty string
        validation = validation.concat(Joi.string().max(max).allow(null).allow(''));
      } else {
        // if set prop required it must not be set allow null or empty string
        validation = validation.concat(Joi.string().max(max));
      }
    }

    return validation;
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

  onChange(editorState) {
    let result = true;
    if (this.props.onChange) {
      result = this.props.onChange(editorState); // TODO: event value only?
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }
    //
    this.setState({
      editorState,
      value: editorState.getCurrentContent().getPlainText()
    }, () => {
      this.validate();
    });
  }

  setValue(value) {
    const editorState = this._normalizeValue(value);
    this.setState({
      editorState,
      value: editorState.getCurrentContent().getPlainText()
    });
  }

  getValue() {
    const { editorState } = this.state;
    //
    return editorState.getCurrentContent().getPlainText();
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, placeholder, style, required, helpBlock } = this.props;
    const { editorState, disabled } = this.state;
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }
    const title = this.getValidationResult() != null ? this.getValidationResult().message : null;
    //
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
            <div className="basic-richtextarea">
              <Editor
                ref="input"
                editorState={editorState}
                onChange={this.onChange}
                handleKeyCommand={this._handleKeyCommand.bind(this)}
                className="form-control"
                title={this.getValidationResult() != null ? this.getValidationResult().message : ''}
                disabled={disabled}
                placeholder={placeholder}
                rows={this.props.rows}
                style={style}
                readOnly={this.state.readOnly}
                spellCheck/>
              {
                feedback
                ||
                !showAsterix
                ||
                <span className="form-control-feedback" style={{ color: 'red', zIndex: 0 }}>*</span>
              }
            </div>
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

RichTextArea.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  rows: PropTypes.number,
  min: PropTypes.number,
  max: PropTypes.number
};

RichTextArea.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  rows: 3
};

export default RichTextArea;
