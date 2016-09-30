import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import ToogleFilterButton from './ToogleFilterButton';
import FilterButtons from './FilterButtons';
import FilterTextField from './FilterTextField';
import FilterBooleanSelectBox from './FilterBooleanSelectBox';
import FilterEnumSelectBox from './FilterEnumSelectBox';
import FilterSelectBox from './FilterSelectBox';
import FilterDateTimePicker from './FilterDateTimePicker';

// TODO: Move to better place and use in child components
const DEFAUT_RELATION = 'LIKE';
const DEFAUT_ENUM_RELATION = 'EQ';

export default class Filter extends Basic.AbstractContextComponent {

  useFilter(event) {
    const { onSubmit } = this.props;
    if (onSubmit) {
      onSubmit(event);
    } else if (event) {
      event.preventDefault();
    }
  }

  render() {
    const { rendered, showloading } = this.props;
    if (!rendered || showloading) {
      return false;
    }
    return (
      <form onSubmit={this.useFilter.bind(this)} className="advanced-filter">
        {this.props.children}
      </form>
    );
  }

}

Filter.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Submit function
   */
  onSubmit: PropTypes.func.isRequired
};
Filter.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
};

Filter.DEFAUT_RELATION = DEFAUT_RELATION;
Filter.DEFAUT_ENUM_RELATION = DEFAUT_ENUM_RELATION;
Filter.ToogleButton = ToogleFilterButton;
Filter.FilterButtons = FilterButtons;
Filter.TextField = FilterTextField;
Filter.BooleanSelectBox = FilterBooleanSelectBox;
Filter.EnumSelectBox = FilterEnumSelectBox;
Filter.SelectBox = FilterSelectBox;
Filter.DateTimePicker = FilterDateTimePicker;
