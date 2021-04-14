import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Select from 'react-select';
//
import SelectBox from '../SelectBox/SelectBox';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import EnumValueDecorator from './EnumValueDecorator';
import EnumOptionDecorator from './EnumOptionDecorator';

/**
 * Select box with enumetation or static options
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class EnumSelectBox extends SelectBox {

  constructor(props) {
    super(props);
    this.useSymbol = props.useSymbol;
  }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    super.UNSAFE_componentWillReceiveProps(nextProps);
    //
    if (!this.props.options && nextProps.options) {
      // TODO: only init options is supported now ... implement update options and enum
      const options = this.getOptions(nextProps);
      const value = this.getValue();
      if (value) { // only if input value is present (refresh is not needed otherwise)
        let option = null;
        if (options) {
          option = options.find(o => { return o.value === value; });
        }
        if (option) { // only if option was found (refresh is not needed otherwise)
          super.setValue(option);
        }
      }
    }
  }

  setValue(value) {
    if (this.props.useObject) {
      const options = this.getOptions();
      let option = null;
      if (options) {
        option = this.getOptions().find(o => { return o.value === value; });
      }
      super.setValue(option || this.normalizeValue(value));
    } else {
      super.setValue(value);
    }
  }

  getOptions(props = null) {
    const _props = props || this.props;
    const results = [];

    if (_props.enum) {
      let enumeration = _props.enum;
      if (enumeration) {
        if (enumeration.__esModule && enumeration.default) {
          enumeration = enumeration.default;
        }
        //
        for (const enumItem in enumeration) {
          if (_.isSymbol(enumeration[enumItem])) {
            const item = this.itemRenderer(enumeration[enumItem], enumItem);
            results.push(item);
          }
        }
      }
    } else if (_props.options) {
      const options = _props.options;
      for (const item in options) {
        if (!options.hasOwnProperty(item)) {
          continue;
        }
        results.push(this.itemRenderer(options[item]));
      }
    }
    // add empty option at start
    if (_props.clearable && results.length > 0 && !_props.multiSelect) {
      const emptyOption = this.getEmptyOption(_props.emptyOptionLabel);
      if (emptyOption) {
        results.unshift(emptyOption);
      }
    }

    return results;
  }

  itemRenderer(enumItem, key) {
    let item;
    if (enumItem && enumItem.value && !enumItem[SelectBox.ITEM_FULL_KEY]) {
      item = _.merge({}, enumItem, {
        [SelectBox.NICE_LABEL]: enumItem.niceLabel ? enumItem.niceLabel : this._findNiceLabel(enumItem.value),
        [SelectBox.ITEM_FULL_KEY]: enumItem.value,
        disabled: this._isDisabled(enumItem.value),
        _iconKey: this._findIcon(enumItem.value),
        description: enumItem.description
      });
    } else { // item is rendered already
      item = _.merge({}, enumItem);
      let niceLabel;
      if (this.props.enum) {
        if (enumItem.niceLabel) {
          // enum item has nice label, then use this niceLabel
          niceLabel = enumItem.niceLabel;
        } else {
          // niceLabel dont exist, then get new by key
          let enumeration = this.props.enum;
          if (enumeration.__esModule && enumeration.default) {
            enumeration = enumeration.default;
          }
          niceLabel = enumeration.getNiceLabel(key);
        }
      } else if (typeof enumItem === 'string') {
        niceLabel = enumItem;
        key = key || enumItem;
      }
      const itemFullKey = niceLabel;
      _.merge(item, {
        [SelectBox.NICE_LABEL]: niceLabel,
        [SelectBox.ITEM_FULL_KEY]: itemFullKey,
        value: key,
        disabled: this._isDisabled(key),
        _iconKey: this._findIcon(key),
        description: (typeof item === 'object' ? item.description : null)
      });
    }
    return item;
  }

  _findKeyBySymbol(sym) {
    let enumeration = this.props.enum;
    if (enumeration && enumeration.__esModule && enumeration.default) {
      enumeration = enumeration.default;
    }
    //
    if (sym) {
      for (const enumItem in enumeration) {
        if (typeof enumeration[enumItem] === 'symbol' && enumeration[enumItem] === sym) {
          return enumItem;
        }
      }
    }
    if (_.isObject(sym) && sym.value) {
      return sym.value;
    }
    //
    return sym;
  }

  _findNiceLabel(value) {
    if (!value) {
      return null;
    }
    let rawValue;
    if (_.isSymbol(value)) {
      rawValue = this._findKeyBySymbol(value);
    } else {
      rawValue = value;
    }
    //
    let enumeration = this.props.enum;
    const { options } = this.props;
    if (enumeration) {
      if (enumeration.__esModule && enumeration.default) {
        enumeration = enumeration.default;
      }
      return enumeration.getNiceLabel(rawValue);
    }
    if (options) {
      for (const item in options) {
        if (options[item].value === rawValue) {
          return options[item].niceLabel;
        }
        if (options[item] === rawValue) {
          return options[item];
        }
      }
    }
    return null;
  }

  _findIcon(value) {
    if (!value) {
      return null;
    }
    let rawValue;
    if (_.isSymbol(value)) {
      rawValue = this._findKeyBySymbol(value);
    } else {
      rawValue = value;
    }
    //
    let enumeration = this.props.enum;
    if (enumeration && enumeration.__esModule && enumeration.default) {
      enumeration = enumeration.default;
    }
    const { options } = this.props;
    //
    if (enumeration && enumeration.getIcon) {
      return enumeration.getIcon(rawValue);
    }
    if (options) {
      for (const item in options) {
        if (options[item].value === rawValue) {
          return options[item]._iconKey;
        }
      }
    }
    return null;
  }

  _isDisabled(value) {
    if (!value) {
      return false;
    }
    let rawValue;
    if (_.isSymbol(value)) {
      rawValue = this._findKeyBySymbol(value);
    } else {
      rawValue = value;
    }
    //
    let enumeration = this.props.enum;
    const { options } = this.props;
    if (enumeration) {
      if (enumeration.__esModule && enumeration.default) {
        enumeration = enumeration.default;
      }
      return enumeration.isDisabled(rawValue);
    }
    if (options) {
      for (const item in options) {
        if (options[item].value === rawValue) {
          return options[item].disabled === true;
        }
        if (options[item] === rawValue) {
          return false;
        }
      }
    }
    return false;
  }

  normalizeValue(value) {
    if (value) {
      // value is array ... enum multiselect
      if (value instanceof Array && this.props.multiSelect === true && _.isSymbol(value[0])) {
        const valueArray = [];
        for (const item of value) {
          if (_.isSymbol(value)) {
            // value is symbol
            if (this.useSymbol === null) {
              this.useSymbol = true;
            }
            // add item to array
            valueArray.push(this.itemRenderer(item, this._findKeyBySymbol(item)));
          }
        }
        return valueArray;
      }
      if (_.isSymbol(value)) {
        // value is symbol
        // value is string any selectBox
        if (this.useSymbol === null) {
          this.useSymbol = true;
        }
        return this.itemRenderer(value, this._findKeyBySymbol(value));
      }
      if (typeof value === 'string') {
        // value is string any selectBox
        if (this.useSymbol === null) {
          this.useSymbol = false;
        }
        return this.itemRenderer({ value });
      }
      if (value instanceof Array && this.props.multiSelect === true && typeof value[0] === 'string') {
        // value is string array ... any multiselect
        if (this.useSymbol === null) {
          this.useSymbol = false;
        }
        const valueArray = [];
        for (const item of value) {
          if (typeof item === 'string') {
            // value is string
            // add item to array
            valueArray.push(this.itemRenderer({ value: item }));
          }
        }
        return valueArray;
      }
      if (value instanceof Array && this.props.multiSelect === true && typeof value[0] === 'object') {
        const valueArray = [];
        for (const item of value) {
          valueArray.push(this.itemRenderer(item, this._findKeyBySymbol(item)));
        }
        return valueArray;
      }
    }
    return value;
  }

  getValue() {
    const { value } = this.state;
    //
    if (!value) {
      return null;
    }
    // value is array ... multiselect
    if (value instanceof Array && this.props.multiSelect === true) {
      const copyValue = [];
      for (const item of value) {
        copyValue.push(this._convertValue(_.merge({}, item)));
      }
      return copyValue;
    }
    // value is not array
    return this._convertValue(_.merge({}, value));
  }

  /**
   * Converts value to / from symbol or object. Is dependent on input and props:
   * - when input is string - results is string. Enum property has to be setted.
   * - when useObject is true, then returns whole object
   *
   * @param  {[type]} value selected value
   * @return {string|symbol}
   */
  _convertValue(item) {
    if (!item) {
      // nothing to convert
      return item;
    }
    if (this.props.useObject) {
      // or useObject - keeping value intouched
      return item;
    }
    let enumeration = this.props.enum;
    if (!enumeration) {
      // Enum property has to be setted - when option props is given, then nothing can be done.
      return item.value;
    }
    if (enumeration.__esModule && enumeration.default) {
      enumeration = enumeration.default;
    }
    const value = item.value;
    //
    let convertedValue = value;
    if (this.useSymbol === null || this.useSymbol) {
      if (!_.isSymbol(value)) {
        convertedValue = enumeration.findSymbolByKey(value);
      }
    } else if (_.isSymbol(value)) {
      convertedValue = enumeration.findKeyBySymbol(value);
    }
    this.getLogger().trace('[Basic.EnumSelectBox] converted value:', convertedValue);
    return convertedValue;
  }

  onInputChange(value) {
    this.getOptions(value);
  }

  getPlaceholder(placeholder) {
    if (placeholder !== null && placeholder !== undefined) {
      return placeholder;
    }
    // default placeholder
    return this.i18n('label.select', { defaultValue: 'Select ...' });
  }

  getSelectComponent() {
    const {
      placeholder,
      multiSelect,
      fieldLabel,
      searchable,
      clearable,
      showLoading,
      title,
      optionComponent,
      valueComponent
    } = this.props;
    const { value, readOnly, disabled } = this.state;
    //
    return (
      <span>
        <Select
          ref="selectComponent"
          title={ title }
          value={ value }
          onChange={ this.onChange }
          disabled={ readOnly || disabled }
          ignoreCase
          clearable={ clearable }
          ignoreAccents={ false }
          multi={ multiSelect }
          closeOnSelect={ !multiSelect }
          onSelectResetsInput={ false }
          valueKey={ SelectBox.ITEM_VALUE }
          labelKey={ fieldLabel }
          noResultsText={ this.i18n('component.basic.SelectBox.noResultsText') }
          placeholder={ this.getPlaceholder(placeholder) }
          searchingText={ this.i18n('component.basic.SelectBox.searchingText') }
          searchPromptText={ this.i18n('component.basic.SelectBox.searchPromptText') }
          onInputChange={ this.onInputChange.bind(this) }
          options={ this.getOptions() }
          searchable={ searchable }
          isLoading={ showLoading }
          optionComponent={ optionComponent }
          valueComponent={ valueComponent }/>
      </span>
    );
  }
}

EnumSelectBox.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  fieldLabel: PropTypes.string,
  multiSelect: PropTypes.bool,
  enum: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.func
  ]),
  options: PropTypes.arrayOf(PropTypes.object),
  value: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.func),
    PropTypes.string,
    PropTypes.symbol
  ]),
  searchable: PropTypes.bool,
  useSymbol: PropTypes.bool,
  useObject: PropTypes.bool,
  clearable: PropTypes.bool,
  /**
   * Option decorator - generalize OptionDecorator
   */
  optionComponent: PropTypes.func,
  /**
   * Value decorator - generalize ValueDecorator
   */
  valueComponent: PropTypes.func
};

EnumSelectBox.defaultProps = {
  ...SelectBox.defaultProps,
  searchable: false,
  useSymbol: null,
  useObject: false,
  clearable: true,
  optionComponent: EnumOptionDecorator,
  valueComponent: EnumValueDecorator
};

export default EnumSelectBox;
