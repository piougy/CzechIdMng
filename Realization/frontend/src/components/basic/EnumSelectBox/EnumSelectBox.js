'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
import merge from 'object-assign';
import Select from 'react-select';
import Joi from 'joi';
//
import SelectBox from '../SelectBox/SelectBox';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent'

class EnumSelectBox extends SelectBox {

  constructor(props) {
    super(props);
  }

  getOptions (input, callback) {
    if (this.props.enum){
      let data = null;
      let enumeration = this.props.enum;
      let results = [];
      if (enumeration){
        for (let enumItem in enumeration) {
          if (typeof enumeration[enumItem] == 'symbol'){
            let item = this.itemRenderer(enumeration[enumItem], enumItem);
            results.push(item);
          }
        }
        data = {
          options: results,
          complete: true,
          };
      }
      callback(null, data);
   }
   if (this.props.options){
     let options = this.props.options;
     let results = [];
     let data;

     if (options){
       for (let item in options) {
           let itemRendered = this.itemRenderer(options[item]);
           results.push(itemRendered);
       }

       data = {
         options: results,
         complete: true,
         };
     }
     callback(null, data);
    }
  }

  itemRenderer(enumItem, key) {
    let item;
    if (enumItem && enumItem.value && !enumItem[SelectBox.ITEM_FULL_KEY]){
      item = enumItem;
      merge(item, {
        [SelectBox.NICE_LABEL]: enumItem.niceLabel ? enumItem.niceLabel : this._findNiceLabel(enumItem.value),
        [SelectBox.ITEM_FULL_KEY]: enumItem.value
      });
    } else {
      item = {value: enumItem}
      let niceLabel;
      if (this.props.enum) {
        niceLabel = this.props.enum.getNiceLabel(key);
      }
      let itemFullKey = niceLabel;
      merge(item,{[SelectBox.NICE_LABEL]:niceLabel, [SelectBox.ITEM_FULL_KEY] : itemFullKey})
    }
    return item;
  }

  _findKeyBySymbol(sym){
    if (sym){
      for (let enumItem in this.props.enum) {
        if (typeof this.props.enum[enumItem] == 'symbol' && this.props.enum[enumItem] === sym){
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
    if (typeof value == 'symbol') {
      rawValue = this._findKeyBySymbol(value);
    } else {
      rawValue = value;
    }
    //
    if (this.props.enum) {
      return this.props.enum.getNiceLabel(rawValue);
    }
    if (this.props.options) {
      for (let item in this.props.options) {
        if (this.props.options[item].value === rawValue){
          return this.props.options[item].niceLabel;
        }
      }
    }
    return null;
  }

  normalizeValue(value) {
   if (value){
      //value is array ... enum multiselect
      if (value instanceof Array && this.props.multiSelect === true && typeof value[0] == 'symbol'){
        let valueArray = [];
        for (let item of value) {
         if (typeof item == 'symbol') {
            //value is symbol
            let objectItem = this.itemRenderer(item, this._findKeyBySymbol(item));
            //add item to array
            valueArray.push(objectItem);
         }
       }
       return valueArray;
     }else if (typeof value == 'symbol') {
        //value is symbol
        return this.itemRenderer(value, this._findKeyBySymbol(value));
     }else if (typeof value == 'string') {
        //value is string any selectBox
        return this.itemRenderer({value: value});
     }else if (value instanceof Array && this.props.multiSelect === true && typeof value[0] == 'string'){
       //value is string array ... any multiselect
         let valueArray = [];
         for (let item of value) {
          if (typeof item == 'string') {
             //value is string
             let objectItem =   this.itemRenderer({value: item});
             //add item to array
             valueArray.push(objectItem);
          }
        }
        return valueArray;
      }
   }
   return value;
 }

 getValue(){
   if (!this.state.value){
     return null;
   }

   //value is array ... multiselect
  if (this.state.value instanceof Array && this.props.multiSelect === true){
    let copyValue = [];
    for (let item of this.state.value) {
      copyValue.push((this._deletePrivateField(merge({},item))).value);
    }
    return copyValue;
  } else {
    //value is not array
    let copyValue = merge({},this.state.value);
    this._deletePrivateField(copyValue);
    return copyValue.value;
  }
 }

  getSelectComponent(){
    const { ref, labelSpan, label, componentSpan, placeholder, style, readOnly, required, multiSelect, fieldLabel } = this.props;
    return <Select.Async
      ref="selectComponent"
      title={"title"}
      value={this.state.value}
      onChange={this.onChange}
      disabled={this.state.readOnly || this.state.disabled}
      ignoreCase={true}
      ignoreAccents={false}
      multi={multiSelect}
      onValueClick={this.gotoContributor}
      valueKey={SelectBox.ITEM_FULL_KEY}
      labelKey={fieldLabel}
      noResultsText={this.i18n('component.basic.SelectBox.noResultsText')}
      placeholder={placeholder}
      searchingText={this.i18n('component.basic.SelectBox.searchingText')}
      searchPromptText={this.i18n('component.basic.SelectBox.searchPromptText')}
      loadOptions={this.getOptions} />
  }
}

EnumSelectBox.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  fieldLabel: PropTypes.string,
  multiSelect: PropTypes.bool,
  enum: React.PropTypes.object,
  options: React.PropTypes.array,
  value: React.PropTypes.oneOfType([
    React.PropTypes.arrayOf(React.PropTypes.symbol),
    React.PropTypes.symbol
  ])
}

EnumSelectBox.defaultProps = {
  ...SelectBox.defaultProps
}

export default EnumSelectBox;
