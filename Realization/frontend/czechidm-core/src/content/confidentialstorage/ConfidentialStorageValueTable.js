import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Table with confidential storage values
 */
export class ConfidentialStorageValueTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }

  getManager() {
    return this.props.confidentialStorageValueManager;
  }

  getContentKey() {
    return 'content.confidentialStorage';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    // when create new script is generate non existing id
    // and set parameter new to 1 (true)
    // this is necessary for ScriptDetail
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/confidential-storage/${uuidId}?new=1`);
    } else {
      this.context.router.push('/confidential-storage/' + entity.id);
    }
  }

  render() {
    const { uiKey, confidentialStorageValueManager } = this.props;
    const { filterOpened } = this.state;
    //
    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={confidentialStorageValueManager}
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
                <Basic.Row>
                <div className="col-lg-6">
                  <Advanced.Filter.TextField
                    ref="ownerId"
                    placeholder={this.i18n('filter.ownerId')}/>
                </div>
                <div className="col-lg-6">
                  <Advanced.Filter.TextField
                    ref="ownerType"
                    placeholder={this.i18n('filter.ownerType')}/>
                </div>
                </Basic.Row>
                <Basic.Row className="last">
                <div className="col-lg-12">
                  <Advanced.Filter.TextField
                    ref="key"
                    placeholder={this.i18n('filter.key')}/>
                </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={!filterOpened}
          _searchParameters={ this.getSearchParameters() }
          >
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="key" sort />
            <Advanced.Column
              property="ownerId"
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data, property }) => {
                  return (
                    <Advanced.EntityInfo
                      entityType={ this._getType(data[rowIndex].ownerType ) }
                      entityIdentifier={ data[rowIndex][property] }
                      face="popover"
                      showEntityType={ false }/>
                  );
                }
              }/>
          <Advanced.Column property="ownerType" sort />
        </Advanced.Table>
      </div>
    );
  }
}

ConfidentialStorageValueTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  ConfidentialStorageValueManager: PropTypes.object.isRequired
};

ConfidentialStorageValueTable.defaultProps = {
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(ConfidentialStorageValueTable);
