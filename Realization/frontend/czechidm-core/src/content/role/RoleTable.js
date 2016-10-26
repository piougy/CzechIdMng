import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
//
import { SecurityManager, RoleCatalogueManager } from '../../redux';
import uuid from 'uuid';

// Table uiKey
const rootsKey = 'role-catalogue-tree-roots';

/**
* Table of roles
*/
export class RoleTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      showLoading: true,
      rootNodes: null
    };
    this.roleCatalogueManager = new RoleCatalogueManager();
  }

  componentDidMount() {
    const searchParametersRoots = this.roleCatalogueManager.getService().getRootSearchParameters();
    this.context.store.dispatch(this.roleCatalogueManager.fetchEntities(searchParametersRoots, rootsKey, (loadedRoots) => {
      const rootNodes = loadedRoots._embedded[this.roleCatalogueManager.getCollectionType()];
      this.setState({
        rootNodes,
        showLoading: false
      });
    }));
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

  _useFilterByTree(nodeId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    if (!nodeId) {
      return;
    }
    const data = {
      ... this.refs.filterForm.getData(),
      roleCatalogue: nodeId
    };
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

/**
 * Decorator for Role cataloue tree. Custom icons
 */
  _roleTreeHeaderDecorator(props) {
    const style = props.style;
    const icon = props.node.isLeaf ? 'group' : 'building';
    return (
      <div style={style.base}>
        <div style={style.title}>
          <Basic.Icon type="fa" icon={icon} style={{ marginRight: '5px' }}/>
          <Basic.Button level="link" onClick={this._useFilterByTree.bind(this, props.node.id)} style={{padding: '0px 0px 0px 0px'}}>
            { props.node.name }
            {
              !props.node.childrenCount
              ||
              <small style={{ color: '#aaa' }}>{' '}({props.node.childrenCount})</small>
            }
          </Basic.Button>
        </div>
      </div>
    );
  }

  render() {
    const { uiKey, roleManager, columns } = this.props;
    const { filterOpened, showLoading, rootNodes } = this.state;
    const showTree = !showLoading && rootNodes && rootNodes.length !== 0;
    return (
      <Basic.Row>
        <div className="col-lg-3" style={{ paddingRight: 0, paddingLeft: 0, marginLeft: 15, marginRight: -15 }}>
          <div className="basic-toolbar">
            <div className="pull-left">
              <h3 style={{ margin: 0 }}>{this.i18n('content.roles.roleCataloguePick')}</h3>
            </div>
            <div className="clearfix"></div>
          </div>
          <div style={{ paddingLeft: 15, paddingRight: 15 }}>
            {
              !showTree
              ||
              <Basic.Panel style={{ marginTop: 15 }}>
                <Advanced.Tree
                  ref="roleCatalogueTree"
                  rootNodes={ rootNodes }
                  headerDecorator={this._roleTreeHeaderDecorator.bind(this)}
                  uiKey="roleCatalogueTree"
                  manager={this.roleCatalogueManager}
                  />
              </Basic.Panel>
            }
          </div>
        </div>

        <div className="col-lg-9">
          <Basic.Confirm ref="confirm-delete" level="danger"/>

          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={roleManager}
            rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
            filterOpened={filterOpened}
            showRowSelection={SecurityManager.hasAuthority('ROLE_DELETE')}
            style={{ borderLeft: '1px solid #ddd' }}
            showLoading={showLoading}
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
                  onClick={this.showDetail.bind(this, { })}
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
            <Advanced.ColumnLink to="role/:id/detail" property="name" width="15%" sort face="text" rendered={_.includes(columns, 'name')}/>
            <Advanced.Column property="roleType" width="75px" sort face="enum" enumClass={RoleTypeEnum} rendered={_.includes(columns, 'roleType')}/>
            <Advanced.Column property="roleCatalogue.name" width="75px" sort face="text" rendered={_.includes(columns, 'roleCatalogue')}/>
            <Advanced.Column property="description" sort face="text" rendered={_.includes(columns, 'description')}/>
            <Advanced.Column
              header={this.i18n('entity.Role.approvable')}
              width="75px"
              className="column-face-bool"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <input type="checkbox" disabled checked={data[rowIndex].approveAddWorkflow || data[rowIndex].approveRemoveWorkflow} />
                  );
                }
              }
              sort={false}/>
            <Advanced.Column property="disabled" sort face="bool" width="75px" rendered={_.includes(columns, 'disabled')}/>
          </Advanced.Table>
        </div>
    </Basic.Row>
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
  columns: ['name', 'roleType', 'disabled', 'approvable', 'description', 'roleCatalogue'],
  filterOpened: false
};

function select() {
  return {
  };
}

export default connect(select)(RoleTable);
