import PropTypes from 'prop-types';
import React from 'react';
//
import * as Basic from '../../basic';


/**
* Codeable field - code / name.
*
* TODO: onChange listener
* TODO: component span?
*
* @author Radek Tomiška
* @since 10.8.0
*/
export default class CodeableField extends Basic.AbstractFormComponent {

  constructor(props) {
    super(props);
    //
    this.state = {
      oldCode: null
    };
  }

  /**
   * Set value from object, which should contains
   *
   * @param {object}
   */
  setValue(value) {
    const { codeProperty, nameProperty } = this.props;
    //
    let code = null;
    let name = null;
    if (value) {
      code = value[codeProperty];
      name = value[nameProperty];
    }
    this.setState({
      oldCode: code
    }, () => {
      this.refs.code.setValue(code);
      this.refs.name.setValue(name);
    });
  }

  /**
   * Return complex value as object with valid from, till and face as nested properties.
   *
   * @return {object}
   */
  getValue() {
    const { codeProperty, nameProperty } = this.props;
    //
    return {
      [codeProperty]: this.refs.code.getValue(),
      [nameProperty]: this.refs.name.getValue()
    };
  }

  isValid() {
    return this.refs.code.isValid() && this.refs.name.isValid();
  }

  validate(showValidationError, cb) {
    const { readOnly, rendered } = this.props;
    //
    if (readOnly || !rendered) {
      return true;
    }
    return this.refs.code.validate(true, cb) || this.refs.name.validate(true, cb);
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        this.refs.code.setState({ showValidationError: json.showValidationError }, cb);
        this.refs.name.setState({ showValidationError: json.showValidationError }, cb);
      } else if (cb) {
        cb();
      }
    });
  }

  focus(focusInvalid = false) {
    if (!focusInvalid) {
      // default
      this.refs.code.focus();
    } else if (!this.refs.code.isValid()) {
      // code is not valid
      this.refs.code.focus();
    } else if (!this.refs.name.isValid()) {
      // name is not valid
      this.refs.name.focus();
    } else {
      // focus code otherwise
      this.refs.code.focus();
    }
  }

  _onChangeCode(event) {
    // check guarded depents on new entity name
    const name = this.refs.name.getValue();
    const code = event.currentTarget.value;
    //
    if (!name || this.state.oldCode === name) {
      this.setState({
        oldCode: code,
      }, () => {
        this.refs.name.setValue(code);
      });
    }
  }

  render() {
    const {
      codeLabel,
      nameLabel,
      codePlaceholder,
      namePlaceholder,
      codeHelpBlock,
      nameHelpBlock,
      codeReadOnly,
      nameReadOnly
    } = this.props;
    const {formReadOnly} = this.state;
    //
    return (
      <Basic.Row>
        <Basic.Col lg={ 4 }>
          <Basic.TextField
            ref="code"
            label={ codeLabel || this.i18n('entity.code.label') }
            placeholder={ codePlaceholder || this.i18n('entity.code.placeholder') }
            helpBlock={ codeHelpBlock }
            max={ 255 }
            required
            onChange={ this._onChangeCode.bind(this) }
            readOnly={ formReadOnly || codeReadOnly }/>
        </Basic.Col>
        <Basic.Col lg={ 8 }>
          <Basic.TextField
            ref="name"
            label={ nameLabel || this.i18n('entity.name.label') }
            placeholder={ namePlaceholder }
            helpBlock={ nameHelpBlock }
            required
            min={ 0 }
            max={ 255 }
            readOnly={ formReadOnly || nameReadOnly }/>
        </Basic.Col>
      </Basic.Row>
    );
  }
}

CodeableField.propTypes = {
  /**
   * Property code - property name in value object.
   */
  codeProperty: PropTypes.string,
  /**
   * Property name - property name in value object.
   */
  nameProperty: PropTypes.string,
  /**
   * Property code - field label.
   */
  codeLabel: PropTypes.string,
  /**
   * Property name - field label.
   */
  nameLabel: PropTypes.string,
  /**
   * Property code - field placeholder.
   */
  codePlaceholder: PropTypes.string,
  /**
   * Property name - field placeholder.
   */
  namePlaceholder: PropTypes.string,
  /**
   * Property code - field helpBlock.
   */
  codeHelpBlock: PropTypes.string,
  /**
   * Property name - field helpBlock.
   */
  nameHelpBlock: PropTypes.string,
  /**
   * Property code - readOnly field.
   */
  codeReadOnly: PropTypes.bool,
  /**
   * Property name - readOnly field.
   */
  nameReadOnly: PropTypes.bool
};
CodeableField.defaultProps = {
  codeProperty: 'code',
  nameProperty: 'name',
  codeReadOnly: false,
  nameReadOnly: false
};
