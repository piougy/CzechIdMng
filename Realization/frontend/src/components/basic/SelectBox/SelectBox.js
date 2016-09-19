import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Select from 'react-select';
import Joi from 'joi';
import _ from 'lodash';
//
import Icon from '../Icon/Icon';
import Tooltip from '../Tooltip/Tooltip';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import EntityManager from '../../../modules/core/redux/data/EntityManager';

const NICE_LABEL = 'niceLabel';
const ITEM_FULL_KEY = 'itemFullKey';

class SelectBox extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.getOptions = this.getOptions.bind(this);
    this.itemRenderer = this.itemRenderer.bind(this);
  }

  getRequiredValidationSchema() {
    if (this.props.multiSelect === true) {
      return Joi.array().min(1).required();
    }
    return Joi.object().required();
  }

  /**
   * Merge hard and user deffined search parameters
   */
  _createSearchParameters(inputText) {
    const { forceSearchParameters, manager } = this.props;
    // user input
    const searchParameters = manager.getDefaultSearchParameters().setSize(10).setFilter('text', inputText); // TODO: configurable search properties
    // hard filters
    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      // we dont want override setted pagination
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null);
    }
    return manager.mergeSearchParameters(searchParameters, _forceSearchParameters);
  }

  getOptions(input, callback) {
    const { manager } = this.props;
    const searchParameters = this._createSearchParameters(input);
    //
    this.context.store.dispatch(manager.fetchEntities(searchParameters, null, (json, error) => {
      if (!error) {
        const result = json;
        let data = null;
        const results = result._embedded[manager.getCollectionType()];
        if (results) {
          for (const item in results) {
            if (!results.hasOwnProperty(item)) {
              continue;
            }
            this.itemRenderer(results[item], input);
          }
          data = {
            options: result._embedded[manager.getCollectionType()],
            complete: results.length >= result.page.totalElements,
          };
        }
        callback(null, data);
      } else {
        this.addError(error);
      }
    }));
  }

  getValue() {
    if (!this.state.value) {
      if (this.props.multiSelect === true) {
        return [];
      }
      return null;
    }
    // value is array ... multiselect
    if (this.state.value instanceof Array && this.props.multiSelect === true) {
      const copyValues = [];
      for (const item of this.state.value) {
        // TODO: original ids could be in array - we dont want him - repair _searchAndSetById - setState async
        const entityId = (this._deletePrivateField(_.merge({}, item))).id;
        if (entityId) {
          copyValues.push(entityId);
        }
      }
      return copyValues;
    }
    // value is not array
    const copyValue = _.merge({}, this.state.value);
    this._deletePrivateField(copyValue);
    // result property value
    return copyValue[this.props.returnProperty];
  }

  _deletePrivateField(item) {
    if (item.hasOwnProperty(NICE_LABEL)) {
      delete item[NICE_LABEL];
    }
    if (item.hasOwnProperty(ITEM_FULL_KEY)) {
      delete item[ITEM_FULL_KEY];
    }
    return item;
  }

  isValid() {
    if (this.state.isLoading === true) {
      // value still loading ... component is invalid
      return false;
    }
    return super.isValid();
  }

  normalizeValue(value) {
    if (value) {
      // start loading
      this.setState({
        isLoading: true
      }, () => {
        // value is array ... multiselect
        if (value instanceof Array && this.props.multiSelect === true) {
          if (_.isEmpty(value)) {
            this.setState({
              isLoading: false
            });
          } else {
            for (const item of _.clone(value)) {
              if (item instanceof Object && !item.itemFullKey) {
                // value is object but doesn't have itemFullKey attribute
                this.itemRenderer(item, '');
              } else if ((typeof item === 'string') || (typeof item === 'number')) {
                // value is string, we try load entity by id
                this._searchAndSetById(item, true);
              }
            }
            this.setState({
              isLoading: false
            });
          }
        } else if (value instanceof Object && !value.itemFullKey) {
          // value is object but doesn't have itemFullKey attribute
          this.itemRenderer(value, '');
          this.setState({isLoading: false});
        } else if (typeof value === 'string' || typeof value === 'number') {
          // value is string, we try load entity by id
          this._searchAndSetById(value);
        } else {
          this.setState({isLoading: false});
        }
      });
    }
    return value;
  }

  _searchAndSetById(value, isArray) {
    const { manager } = this.props;
    if (!manager) {
      return null;
    }
    this.context.store.dispatch(manager.fetchEntityIfNeeded(value, null, (json, error) => {
      if (!error) {
        this.itemRenderer(json, '');
        let result = json;
        if (isArray === true) {
          // TODO: tohle funguje jen dilem nahody - valueArray naprimo modifikuje stav pres referenci ... pres setState by se to cyklilo z duvodu async - volani dispatch v cyklu vne tyhle metody
          let valueArray = this.state.value;
          // if is value null or if contains string (not Object) we create new array
          if (!valueArray) {
            valueArray = [];
          }
          // add item to array
          valueArray.push(json);
          result = valueArray;
        } else {
          this.setState({value: result, isLoading: false}, this.validate);
        }
      } else {
        this.addError(error);
        this.setState({value: null, isLoading: false});
      }
    }));
  }

  getFiltersDefiniton(input) {
    const filters = [];
    for (const field of this.props.searchInFields) {
      filters.push({ field, 'value': input });
    }
    return filters;
  }

  onChange(value) {
    this.setState({
      value
    }, () => {
      this.validate();
    });

    if (this.props.onChange) {
      this.props.onChange(value);
    }
  }

  focus() {
    this.refs.selectComponent.focus();
  }

  itemRenderer(item, input) {
    const niceLabel = this.props.manager.getNiceLabel(item);
    const inputLower = input.toLowerCase();
    let itemFullKey = niceLabel;
    if (inputLower) {
      if (!niceLabel.toLowerCase().indexOf(inputLower) >= 0) {
        for (const field of this.props.searchInFields) {
          if (item[field].toLowerCase().indexOf(inputLower) >= 0) {
            itemFullKey = itemFullKey + ' (' + item[field] + ')';
            break;
          }
        }
      }
    }
    _.merge(item, {[NICE_LABEL]: niceLabel, [ITEM_FULL_KEY]: itemFullKey});
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, required } = this.props;
    const labelClassName = classNames(labelSpan, 'control-label');
    const title = this.getValidationResult() != null ? this.getValidationResult().message : null;
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }

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
        <div className={componentSpan}>
          {
            (this.state.disabled === true && this.props.multiSelect !== true)
            ?
            <div ref="selectComponent" className="form-text">{this.state.value ? this.state.value[this.props.fieldLabel] : null}
              <Icon type="fa" icon="refresh" className="icon-loading" rendered={this.state.isLoading === true} showLoading/>
            </div>
            :
            <Tooltip ref="popover" placement="right" value={title}>
              <span>
                {this.getSelectComponent()}
                {
                  feedback != null
                  ?
                  feedback
                  :
                  <span>
                    {
                      showAsterix
                      ?
                      <span className="form-control-feedback" style={{ color: 'red', zIndex: 0 }}>*</span>
                      :
                      ''
                    }
                  </span>
                }
              </span>
            </Tooltip>
          }
          {this.props.children}
        </div>
      </div>
    );
  }

  getPlaceholder(placeholder) {
    if (placeholder !== null && placeholder !== undefined) {
      return placeholder;
    }
    // default placeholder
    return this.i18n('label.select', { defaultValue: 'Select ...' });
  }

  getSelectComponent() {
    const { placeholder, fieldLabel, multiSelect, clearable } = this.props;
    return (
      <Select.Async
        ref="selectComponent"
        isLoading={this.state.isLoading}
        value={this.state.value}
        onChange={this.onChange}
        disabled={this.state.readOnly || this.state.disabled}
        ignoreCase
        ignoreAccents={false}
        multi={multiSelect}
        onValueClick={this.gotoContributor}
        valueKey={ITEM_FULL_KEY}
        labelKey={fieldLabel}
        noResultsText={this.i18n('component.basic.SelectBox.noResultsText')}
        placeholder={this.getPlaceholder(placeholder)}
        searchingText={this.i18n('component.basic.SelectBox.searchingText')}
        searchPromptText={this.i18n('component.basic.SelectBox.searchPromptText')}
        loadOptions={this.getOptions}
        clearable={clearable} />
    );
  }
}

SelectBox.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  manager: PropTypes.instanceOf(EntityManager).isRequired,
  // TODO: searchInFields is not implemented now
  searchInFields: PropTypes.arrayOf(PropTypes.string).isRequired,
  fieldLabel: PropTypes.string,
  value: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.object),
    PropTypes.object,
    PropTypes.arrayOf(PropTypes.string),
    PropTypes.string
  ]),
  multiSelect: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
  * If object is selected, then this property value will be returned
  */
  returnProperty: PropTypes.string,
  /**
   * Selected options can be cleared
   */
  clearable: PropTypes.bool
};

SelectBox.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  fieldLabel: NICE_LABEL,
  multiSelect: false,
  returnProperty: 'id',
  searchInFields: [],
  clearable: true
};

SelectBox.NICE_LABEL = NICE_LABEL;
SelectBox.ITEM_FULL_KEY = ITEM_FULL_KEY;


export default SelectBox;
