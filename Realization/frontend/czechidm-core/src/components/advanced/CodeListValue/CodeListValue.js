import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { CodeListManager, CodeListItemManager } from '../../../redux';

const codeListManager = new CodeListManager();
const codeListItemManager = new CodeListItemManager();

/**
* Code list Value
* - code decorator only
*
* Look out: code list has to be loaded externally: this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded(code));
*
* TODO: show loading from data
* TODO: multi value
*
* @author Radek Tomiška
* @since 9.4.0
*/
export class CodeListValue extends Basic.AbstractContextComponent {

  renderItem(value, options) {
    if (!options || !value) {
      return value;
    }
    const item = options.find(i => i.code === value);
    if (!item) {
      return value;
    }
    return codeListItemManager.getNiceLabel(item);
  }

  render() {
    const { rendered, showLoading, _showLoading, value, options } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (showLoading || _showLoading) {
      return (
        <Basic.Icon icon="refresh" showLoading />
      );
    }
    //
    return (
      <span>
        { this.renderItem(value, options) }
      </span>
    );
  }
}

CodeListValue.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * CodeList code
   */
  code: PropTypes.string.isRequired
};

CodeListValue.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};


function select(state, component) {
  //
  return {
    options: codeListManager.getCodeList(state, component.code),
    _showLoading: codeListManager.isShowLoading(state, component.code),
  };
}

export default connect(select)(CodeListValue);
