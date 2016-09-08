import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import * as Utils from 'core/utils';
import RoleTypeEnum from 'core/enums/RoleTypeEnum';
import { SecurityManager} from 'core/redux';
import uuid from 'uuid';

/**
* Table of roles
*/
export class RoleTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'content.roles';
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
      this.context.router.push(`/role/${uuidId}/new?new=1`);
    } else {
      this.context.router.push('/role/' + entity.id + '/detail');
    }
  }

  onDelete(bulkActionValue, selectedRows) {
    const { roleManager, uiKey } = this.props;
    const selectedEntities = roleManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: roleManager.getNiceLabel(selectedEntities[0]), records: roleManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: roleManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(roleManager.deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { uiKey, roleManager, columns } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={roleManager}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filterOpened={filterOpened}
          showRowSelection={SecurityManager.hasAuthority('ROLE_DELETE')}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('entity.Role.name')}
                      label={this.i18n('entity.Role.name')}/>
                  </div>
                  <div className="col-lg-4">
                    <Basic.EnumSelectBox
                      ref="roleType"
                      label={this.i18n('entity.Role.roleType')}
                      enum={RoleTypeEnum}/>
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
                onClick={this.showDetail.bind(this, { roleType: RoleTypeEnum.findKeyBySymbol(RoleTypeEnum.TECHNICAL) })}
                rendered={SecurityManager.hasAuthority('ROLE_WRITE')}>
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
          <Advanced.Column property="name" sort face="text" rendered={_.includes(columns, 'name')}/>
          <Advanced.Column property="roleType" sort face="enum" enumClass={RoleTypeEnum} rendered={_.includes(columns, 'roleType')}/>
          <Advanced.Column
            header={this.i18n('entity.Role.approvable')}
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <input type="checkbox" disabled checked={data[rowIndex].approveAddWorkflow || data[rowIndex].approveRemoveWorkflow} />
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="disabled" sort face="bool" rendered={_.includes(columns, 'disabled')}/>
        </Advanced.Table>
      </div>
    );
  }
}

RoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

RoleTable.defaultProps = {
  columns: ['name', 'roleType', 'disabled', 'approvable'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.roleManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(RoleTable);
