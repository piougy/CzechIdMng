import React from 'react';
import _ from 'lodash';
import classNames from 'classnames';
//
import * as Basic from '../../basic';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import UuidFormAttributeRenderer from './UuidFormAttributeRenderer';
import Dropzone from '../Dropzone/Dropzone';
import { AttachmentService } from '../../../services';
import PersistentTypeEnum from '../../../enums/PersistentTypeEnum';

const attachmentService = new AttachmentService();

/**
 * Attachment form value component
 * - supports single file only for now
 * - TODO: support multiple files
 * - TODO: validation support (now is validation on input - title is missing)
 *
 * @author Radek Tomiška
 */
export default class AttachmentFormAttributeRenderer extends UuidFormAttributeRenderer {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      isLoading: false,
      showValidationError: false,
      previewUrl: null,
      previewLoading: false,
      cleared: false
    };
  }

  componentDidMount() {
    this._loadPreview();
  }

  /**
   * Load attachment preview.
   * Support only png, jpg and jpeg mime types
   */
  _loadPreview() {
    const { formableManager, values } = this.props;
    const formValue = this._getSingleValue(values);
    //
    if (!formableManager || !formableManager.supportsAttachment()) {
      return;
    }
    if (!formValue || !formValue.id || !formValue.ownerId || formValue.persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ATTACHMENT)) {
      return;
    }
    // download preview
    this.setState({
      previewLoading: true
    }, () => {
      this.context.store.dispatch(formableManager.downloadPreview(formValue, null, (previewUrl) => {
        this.setState({
          previewUrl,
          previewLoading: false
        });
      }));
    });
  }

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  isValid() {
    if (this.state.isLoading === true) {
      // value still loading ... component is invalid
      return false;
    }
    const isValid = super.isValid();
    this.setState({
      showValidationError: !isValid
    });
    //
    return isValid;
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.uuidValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.uuidValue;
    if (formValue.value) {
      // fill string value - will be used as "saved" placeholder
      formValue.shortTextValue = this.refs.dropzone.getFile() !== null ? this.refs.dropzone.getFile().name : null;
    } else {
      formValue.shortTextValue = null;
    }
    // TODO: validations for uuid
    return formValue;
  }

  /**
   * TODO: single value is used now only => supper multiple files
   */
  _getSingleValue(_values) {
    let formValue = null;
    if (_values && _.isArray(_values)) {
      if (_values.length > 0) {
        formValue = _values[0];
      }
    } else {
      formValue = _values;
    }
    return formValue;
  }

  /**
   * Upload temp file to server - returned attachment uuid will be used as value
   *
   * @return {[type]} [description]
   */
  _onDrop() {
    this.setState({
      isLoading: true,
      showValidationError: false
    }, () => {
      const { formableManager } = this.props;
      const formData = new FormData();
      const file = this.refs.dropzone.getFile();
      formData.append( 'fileName', file.name);
      // TODO: mimetype
      formData.append( 'data', file );
      attachmentService
        .upload(formData)
        .then((uploadedAttachment) => {
          // RT: just preparation - is possible to save attachment into eav directly
          if (false && formableManager && formableManager.supportsAttachment()) {
            const formValue = this.toFormValue();
            formValue.uuidValue = uploadedAttachment.id;
            formValue.shortTextValue = uploadedAttachment.name;
            //
            this.context.store.dispatch(formableManager.saveFormValue(formValue.ownerId, formValue, null, (savedFormValue, error) => {
              this.setState({
                isLoading: false
              }, () => {
                if (error) {
                  this.addError(error);
                } else {
                  this.addMessage({
                    level: 'success', message: this.i18n('message.success.upload', { record: file.name })
                  });
                  this.refs[AbstractFormAttributeRenderer.INPUT].setValue(uploadedAttachment.id);
                }
              });
            }));
          } else {
            this.setState({
              isLoading: false
            }, () => {
              /*
              RT: commented - user was confused, i don't know if should be removed at all?
              this.addMessage({
                level: 'info', message: this.i18n('message.success.upload', { record: file.name })
              });*/
              this.refs[AbstractFormAttributeRenderer.INPUT].setValue(uploadedAttachment.id);
            });
          }
        })
        .catch(uploadError => {
          this.setState({
            isLoading: false
          });
          this.addError(uploadError);
        });
    });
  }

  _onClear(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation(); // buton is in dropzone => prevent to open it
    }
    //
    this.setState({
      previewUrl: null,
      cleared: true
    }, () => {
      this.refs.dropzone.clearFiles();
      this.refs[AbstractFormAttributeRenderer.INPUT].setValue(null);
    });
  }

  getContent(originalValues) {
    const { values, formableManager } = this.props;
    const { showValidationError, previewUrl, previewLoading, cleared } = this.state;
    const showOriginalValue = originalValues ? true : false;
    const _values = showOriginalValue ? originalValues : values;
    const formValue = this._getSingleValue(_values);
    const result = [];
    let isReadOnlyRendered = false;
    //
    if (formValue === null || !formValue.uuidValue || cleared) {
      const value = super.getPlaceholder();
      if (value) {
        result.push(<span>{ value }</span>);
      }
    } else if (formableManager && formableManager.supportsAttachment()) {
      const downloadUrl = formableManager.getDownloadUrl(formValue); // just for sure - manager has to return url everytime
      //
      if (downloadUrl) {
        isReadOnlyRendered = true;
        result.push(
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'center', margin: -30 }}>
            <div style={{ textAlign: 'center', padding: 15 }}>
              <a
                href={ downloadUrl }
                target="_blank"
                rel="noreferrer noopener"
                onClick={ e => e.stopPropagation() }>
                <Basic.Div
                  rendered={ previewUrl === false }
                  className="text-center img-thumbnail"
                  style={{ backgroundColor: '#FCF8E3', height: 100, width: 100, paddingTop: 36 }}
                  title={ this.i18n('label.download') }>
                  <Basic.Icon value="fa:ban" className="fa-2x" title={ this.i18n('button.preview.disabled') } />
                </Basic.Div>
                <Basic.Div rendered={ previewUrl !== false }>
                  <Basic.Div
                    rendered={ previewLoading || previewUrl === null }
                    className="text-center img-thumbnail"
                    style={{ backgroundColor: '#DFF0D8', height: 100, width: 100, paddingTop: 36 }}>
                    <Basic.Icon
                      value="fa:refresh"
                      showLoading
                      color="#FFFFFF"
                      className="fa-2x" />
                  </Basic.Div>
                  <Basic.Div rendered={ !previewLoading && previewUrl !== null } style={{ position: 'relative' }}>
                    <img src={ previewUrl } className="img-thumbnail" style={{ height: 100, padding: 0 }} title={ this.i18n('label.download') } />
                    <Basic.Button
                      type="button"
                      level="danger"
                      title={ this.i18n('button.clear.title') }
                      titlePlacement="left"
                      titleDelayShow={ 0 }
                      className="btn-xs"
                      style={{ position: 'absolute', right: 5, bottom: 5 }}
                      onClick={ this._onClear.bind(this) }
                      rendered={ showOriginalValue ? false : !this.isReadOnly() }>
                      <Basic.Icon type="fa" icon="trash"/>
                    </Basic.Button>
                  </Basic.Div>
                </Basic.Div>
                <div style={{ marginTop: 5 }} title={ this.i18n('label.download') }>
                  { formValue.shortTextValue }
                </div>
              </a>
            </div>
            <Basic.Div style={{ flex: 1 }}>
              <Basic.Div rendered={ !showOriginalValue && !this.isReadOnly()}>
                { this.i18n('component.basic.Dropzone.infoTextSingle') }
              </Basic.Div>
              <Basic.Div rendered={ !showOriginalValue && this.isReadOnly()}>
                { `(${ this.i18n('label.readOnly') })` }
              </Basic.Div>
            </Basic.Div>
          </div>
        );
      } else {
        result.push(
          <span>{ formValue.shortTextValue }</span>
        );
      }
    } else {
      result.push(
        <span>{ formValue.shortTextValue }</span>
      );
    }
    if (this.isReadOnly()) {
      if (!isReadOnlyRendered) {
        result.push(<div>{ `(${ this.i18n('label.readOnly') })` }</div>);
      }
    } else if (result.length === 0) {
      result.push(<span>{ this.i18n('component.basic.Dropzone.infoTextSingle') }</span>);
    }
    if (showValidationError) {
      // TODO: tooltip with validation message
      result.unshift(<Basic.Icon icon="warning-sign" className="form-control-feedback" style={{ zIndex: 0 }} />);
    } else if (this.isRequired()) {
      result.push(<span className="form-control-feedback" style={{ color: 'red', zIndex: 0 }}>*</span>);
    }
    //
    return result;
  }

  renderSingleInput(originalValues) {
    const { values, validationErrors, className, style } = this.props;
    const { isLoading, showValidationError } = this.state;
    const showOriginalValue = originalValues ? true : false;
    //
    const dropyoneStyle = {};
    const _showError = showValidationError || (validationErrors && validationErrors.length > 0);
    if (_showError) {
      dropyoneStyle.borderColor = '#a94442'; // FIXME: move to less, use variable
    }
    const _className = classNames(
      className, {
        'has-feedback': _showError,
        'has-error': _showError
      });
    //
    return (
      <Basic.LabelWrapper
        label={ this.getLabel(null, showOriginalValue) }
        className={ _className }
        helpBlock={ this.getHelpBlock() }
        style={ style }>
        <Dropzone
          ref="dropzone"
          multiple={ false }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          onDrop={ this._onDrop.bind(this) }
          showLoading={ isLoading }
          required={ this.isRequired() }
          style={ dropyoneStyle }>
          { this.getContent(originalValues) }
        </Dropzone>

        {/* Attachment uuid is stored in hiden input */}
        <Basic.TextField
          ref={ AbstractFormAttributeRenderer.INPUT }
          value={ this.toInputValue(showOriginalValue ? originalValues : values) }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          validation={ this.getInputValidation() }
          required={ this.isRequired() }
          hidden/>
      </Basic.LabelWrapper>
    );
  }
}
