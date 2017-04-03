import React, { PropTypes } from 'react';
import _ from 'lodash';
import Select from 'react-select';
//
import SelectBox from '../SelectBox/SelectBox';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';

class EnumSelectBox extends SelectBox {

  constructor(props) {
    super(props);
    this.useSymbol = props.useSymbol;
  }


  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
  }

  setValue(value) {
    super.setValue(this.normalizeValue(value));
  }

  getOptions() {
    if (this.props.enum) {
      const enumeration = this.props.enum;
      const results = [];
      if (enumeration) {
        for (const enumItem in enumeration) {
          if (typeof enumeration[enumItem] === 'symbol') {
            const item = this.itemRenderer(enumeration[enumItem], enumItem);
            results.push(item);
          }
        }
        return results;
      }
    }
    if (this.props.options) {
      const options = this.props.options;
      const results = [];
      for (const item in options) {
        if (!options.hasOwnProperty(item)) {
          continue;
        }
        results.push(this.itemRenderer(options[item]));
      }
      return results;
    }
  }

  itemRenderer(enumItem, key) {
    let item;
    if (enumItem && enumItem.value && !enumItem[SelectBox.ITEM_FULL_KEY]) {
      item = _.merge({}, enumItem, {
        [SelectBox.NICE_LABEL]: enumItem.niceLabel ? enumItem.niceLabel : this._findNiceLabel(enumItem.value),
        [SelectBox.ITEM_FULL_KEY]: enumItem.value
      });
    } else {
      item = _.merge({}, enumItem);
      let niceLabel;
      if (this.props.enum) {
        if (enumItem.niceLabel) {
          // enum item has nice label, then use this niceLabel
          niceLabel = enumItem.niceLabel;
        } else {
          // niceLabel dont exist, then get new by key
          niceLabel = this.props.enum.getNiceLabel(key);
        }
      }
      const itemFullKey = niceLabel;
      _.merge(item, {
        [SelectBox.NICE_LABEL]: niceLabel,
        [SelectBox.ITEM_FULL_KEY]: itemFullKey,
        value: key
      });
    }
    return item;
  }

  _findKeyBySymbol(sym) {
    const enumeration = this.props.enum;
    //
    if (sym) {
      for (const enumItem in enumeration) {
        if (typeof enumeration[enumItem] === 'symbol' && enumeration[enumItem] === sym) {
          return enumItem;
        }
      }
    }
  }

  _findNiceLabel(value) {
    if (!value) {
      return null;
    }
    let rawValue;
    if (typeof value === 'symbol') {
      rawValue = this._findKeyBySymbol(value);
    } else {
      rawValue = value;
    }
    //
    const enumeration = this.props.enum;
    const { options } = this.props;
    if (enumeration) {
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

  normalizeValue(value) {
    if (value) {
      // value is array ... enum multiselect
      if (value instanceof Array && this.props.multiSelect === true && typeof value[0] === 'symbol') {
        const valueArray = [];
        for (const item of value) {
          if (typeof item === 'symbol') {
            // value is symbol
            // add item to array
            valueArray.push(this.itemRenderer(item, this._findKeyBySymbol(item)));
          }
        }
        return valueArray;
      } else if (typeof value === 'symbol') {
        // value is symbol
        return this.itemRenderer(value, this._findKeyBySymbol(value));
      } else if (typeof value === 'string') {
        // value is string any selectBox
        this.useSymbol = false;
        return this.itemRenderer({ value });
      } else if (value instanceof Array && this.props.multiSelect === true && typeof value[0] === 'string') {
        // value is string array ... any multiselect
        this.useSymbol = false;
        const valueArray = [];
        for (const item of value) {
          if (typeof item === 'string') {
            // value is string
            // add item to array
            valueArray.push(this.itemRenderer({ value: item }));
          }
        }
        return valueArray;
      } else if (value instanceof Array && this.props.multiSelect === true && typeof value[0] === 'object') {
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
        copyValue.push(this._convertValue((this._deletePrivateField(_.merge({}, item))).value));
      }
      return copyValue;
    }
    // value is not array
    const copyValue = _.merge({}, value);
    this._deletePrivateField(copyValue);
    return this._convertValue(copyValue.value);
  }

  /**
   * Converts value to / from symbol - is dependent on input - when input is string - results is string. Enum property has to be setted.
   *
   * @param  {[type]} value selected value
   * @return {string|symbol}
   */
  _convertValue(value) {
    if (!value) {
      // nothing to convert
      return value;
    }
    if (!this.props.enum) {
      // Enum property has to be setted - when option props is given, then nothing can be done.
      return value;
    }
    //
    let convertedValue = value;
    if (this.useSymbol) {
      if (typeof value !== 'symbol') {
        convertedValue = this.props.enum.findSymbolByKey(value);
      }
    } else {
      if (typeof value === 'symbol') {
        convertedValue = this.props.enum.findKeyBySymbol(value);
      }
    }
    this.getLogger().trace('[Basic.EnumSelectBox] converted value:', convertedValue);
    return convertedValue;
  }

  onInputChange(value) {
    this.getOptions(value);
  }

  getSelectComponent() {
    const { placeholder, multiSelect, fieldLabel, searchable, clearable } = this.props;
    const { value, readOnly, disabled } = this.state;
    //
    return (
      <span>
        <Select
          ref="selectComponent"
          title={"title"}
          value={value}
          onChange={this.onChange}
          disabled={readOnly || disabled}
          ignoreCase
          clearable={clearable}
          ignoreAccents={false}
          multi={multiSelect}
          onValueClick={this.gotoContributor}
          valueKey={SelectBox.ITEM_VALUE}
          labelKey={fieldLabel}
          noResultsText={this.i18n('component.basic.SelectBox.noResultsText')}
          placeholder={this.getPlaceholder(placeholder)}
          searchingText={this.i18n('component.basic.SelectBox.searchingText')}
          searchPromptText={this.i18n('component.basic.SelectBox.searchPromptText')}
          onInputChange={this.onInputChange.bind(this)}
          options={this.getOptions()}
          searchable={searchable}/>
      </span>
    );
  }
}

EnumSelectBox.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  fieldLabel: PropTypes.string,
  multiSelect: PropTypes.bool,
  enum: PropTypes.object,
  options: PropTypes.array,
  value: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.symbol),
    PropTypes.symbol
  ]),
  searchable: PropTypes.bool,
  useSymbol: PropTypes.bool,
  clearable: PropTypes.bool
};

EnumSelectBox.defaultProps = {
  ...SelectBox.defaultProps,
  searchable: false,
  useSymbol: true,
  clearable: true
};

export default EnumSelectBox;
