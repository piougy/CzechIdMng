import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Utils, Managers } from 'czechidm-core';
//
import uuid from 'uuid';

/**
* Table of target sysstems
*/
export class SystemTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'acc:content.systems';
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

  showDetail(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/system/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/system/${entity.id}/detail`);
    }
  }

  onDelete(bulkActionValue, selectedRows) {
    const { manager, uiKey } = this.props;
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(manager.deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { uiKey, manager, columns } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filterOpened={filterOpened}
          showRowSelection={Managers.SecurityManager.hasAuthority('SYSTEM_DELETE')}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('acc:entity.System.name')}
                      label={this.i18n('acc:entity.System.name')}/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { })}
                rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
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
          <Advanced.ColumnLink to="system/:id/detail" property="name" width="15%" sort face="text" rendered={_.includes(columns, 'name')}/>
          <Advanced.Column property="description" sort face="text" rendered={_.includes(columns, 'description')}/>
          <Advanced.Column property="virtual" sort face="bool" width="75px" rendered={_.includes(columns, 'virtual')}/>
          <Advanced.Column property="disabled" sort face="bool" width="75px" rendered={_.includes(columns, 'disabled')}/>
        </Advanced.Table>
      </div>
    );
  }
}

SystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

SystemTable.defaultProps = {
  columns: ['name', 'description', 'disabled', 'virtual'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.manager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(SystemTable);
