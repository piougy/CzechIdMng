import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import * as Domain from '../../../domain';
import { CodeListItemManager } from '../../../redux';

const codeListItemManager = new CodeListItemManager();

/**
* Code list select
* - render enum select box with available code list items (options)
* - decorator only - if code list is not available (204), then text box is shown
* - multiSelect - used only if code list definition is available
*
* TODO: creatable?
* TODO: big code list - pagination
* TODO: use redux data, when force search parameters are empty?
* TODO: readme
*
* @author Radek Tomiška
* @since 9.4.0
*/
export default class CodeListSelect extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      showLoading: true,
      options: []
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._loadOptions();
  }

  componentWillReceiveProps(nextProps) {
    super.componentWillReceiveProps(nextProps);
    //
    const { items } = nextProps;
    if (items !== this.props.items) {
      this._loadOptions(nextProps);
    }
  }

  getComponentKey() {
    return 'component.advanced.CodeListSelect';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getValue() {
    const { options } = this.state;
    //
    if (options.length > 0 ) {
      return this.refs.inputEnum.getValue();
    }
    return this.refs.inputText.getValue();
  }

  setValue(value, cb) {
    this.setState({ value }, () => {
      this.refs.inputEnum.setValue(value);
      this.refs.inputText.setValue(value);
      // FIXME: cb is called before both inputs are set, we are not usign this now, but should be fixed.
      if (cb) {
        cb();
      }
    });
  }

  isValid() {
    const { options } = this.state;
    //
    if (options.length > 0 ) {
      return this.refs.inputEnum.isValid();
    }
    return this.refs.inputText.isValid();
  }

  validate(showValidationError, cb) {
    const { readOnly, rendered } = this.props;
    const { options } = this.state;
    //
    if (readOnly || !rendered) {
      return true;
    }
    if (options.length > 0 ) {
      return this.refs.inputEnum.validate(true, cb);
    }
    return this.refs.inputText.validate(true, cb);
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        this.refs.inputEnum.setState({ showValidationError: json.showValidationError}, cb);
        this.refs.inputText.setState({ showValidationError: json.showValidationError}, cb);
      } else if (cb) {
        cb();
      }
    });
  }

  /**
   * Focus input field
   */
  focus() {
    // TODO ...
    // this.refs.input.focus();
  }

  /**
   * Tree node field label
   *
   * @return  {string}
   */
  getLabel() {
    const { label } = this.props;
    if (label !== undefined) {
      return label;
    }
    return this.i18n('entity.CodeList._type');
  }

  /**
   * Select box field placeholder
   *
   * @return  {string}
   */
  getPlaceholder() {
    const { placeholder } = this.props;
    if (placeholder !== undefined) {
      return placeholder;
    }
    return null;
  }

  /**
   * Select box field help block
   *
   * @return  {string}
   */
  getHelpBlock() {
    const { helpBlock } = this.props;
    if (helpBlock !== undefined) {
      return helpBlock;
    }
    return null;
  }

  _loadOptions(props = null) {
    const _props = props ? props : this.props;
    const { code, forceSearchParameters, useFirst, items } = _props;
    if (!_props.rendered) {
      // component is not rendered ... loading is not needed
      return;
    }
    if (items !== null && items !== undefined) {
      // set options from preloaded externally
      this._setOptions(items, useFirst);
      return;
    }
    //
    this.setState({
      showLoading: true
    }, () => {
      let searchParameters = new Domain.SearchParameters().setFilter('codeListId', code).setSort('name', false).setSize(10000);
      let _forceSearchParameters = null;
      if (forceSearchParameters) {
        _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
      }
      searchParameters = codeListItemManager.mergeSearchParameters(searchParameters, _forceSearchParameters);
      this.context.store.dispatch(codeListItemManager.fetchEntities(searchParameters, `${this.getUiKey()}-${code}`, (json, error) => {
        if (!error) {
          const data = json._embedded[codeListItemManager.getCollectionType()] || [];
          //
          this._setOptions(data, useFirst);
        } else {
          if (error.statusCode === 400 || error.statusCode === 403) {
            // FIXME: 204 / 404 - codelist doesn't found
            // FIXME: 403 - input only?
            this.addErrorMessage({
              level: 'error',
              key: 'error-code-list-load',
              hidden: true
            }, error);
          } else {
            this.addErrorMessage({
              level: 'error',
              key: 'error-code-list-load'
            }, error);
          }
          this.setState({
            options: [],
            showLoading: false
          }, () => {
            // TODO: enum refresh - normalize item has to be called.
            const value = this.state.value;
            this.refs.inputEnum.setValue(value);
            this.refs.inputText.setValue(value);
          });
        }
      }));
    });
  }

  _setOptions(options, useFirst) {
    let value = this.state.value;
    const _options = [];
    //
    let valueIsPresent = false;
    // constuct operation
    options.forEach(item => {
      if (value && item.code === value) {
        valueIsPresent = true;
      }
      _options.push({
        value: item.code,
        niceLabel: codeListItemManager.getNiceLabel(item)
      });
    });
    // filled value is not in the code list - append ar start
    if (value && !valueIsPresent) {
      _options.unshift({
        value,
        niceLabel: this.i18n(value)
      });
    }
    if (!value && useFirst && _options.length > 0) {
      value = _options[0].value;
    }
    //
    this.setState({
      options: _options,
      showLoading: false
    }, () => {
      // TODO: enum refresh - normalize item has to be called.
      this.refs.inputEnum.setValue(value);
      this.refs.inputText.setValue(value);
    });
  }

  render() {
    const { hidden, required, rendered, validationErrors, multiSelect } = this.props;
    const { options, value, disabled, readOnly } = this.state;
    const showLoading = this.props.showLoading || this.state.showLoading;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <span>
        <Basic.EnumSelectBox
          ref="inputEnum"
          value={ value }
          label={ this.getLabel() }
          placeholder={ this.getPlaceholder() }
          helpBlock={ this.getHelpBlock() }
          readOnly={ readOnly || disabled }
          required={ required }
          validationErrors={ validationErrors }
          hidden={ hidden || (options.length === 0 && !showLoading) }
          showLoading={ showLoading }
          options={ options }
          multiSelect={ multiSelect }/>
        <Basic.TextField
          ref="inputText"
          value={ value }
          label={ this.getLabel() }
          placeholder={ this.getPlaceholder() }
          helpBlock={ this.getHelpBlock() }
          readOnly={ readOnly || disabled }
          required={ required }
          validationErrors={ validationErrors }
          hidden={ showLoading || hidden || options.length > 0 }/>
      </span>
    );
  }
}

CodeListSelect.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  /**
   * CodeList code
   */
  code: PropTypes.string.isRequired,
  /**
   * Ui key - identifier for loading data
   */
  uiKey: PropTypes.string,
  /**
   * Selectbox label
   */
  label: PropTypes.string,
  /**
   * Selectbox placeholder
   */
  placeholder: PropTypes.string,
  /**
   * Selectbox help block
   */
  helpBlock: PropTypes.string,
  /**
   * "Hard filters".
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Use the first searched value, if value is empty
   */
  useFirst: PropTypes.bool,
  /**
   * Preloaded codelist items - prevent to fetch options internally
   */
  items: PropTypes.arrayOf(PropTypes.object),
  /**
   * The component is in multi select mode - available just if code list definition is available (simple input is rendered otherwise).
   */
  multiSelect: PropTypes.bool
};

CodeListSelect.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  uiKey: 'code-list-select',
  useFirst: false,
  items: null,
  multiSelect: false
};
