import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemMappingManager } from '../../redux';
import uuid from 'uuid';
import RoleTable from 'czechidm-core/src/content/role/RoleTable';

const uiKey = 'system-roles-table';
const manager = new RoleSystemManager();
const roleManager = new Managers.RoleManager();
const systemMappingManager = new SystemMappingManager();

/**
 * See roles, assigned to system on "Systems" tab
 *
 * @author Petr Hanák
 */
class SystemRoles extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.roles';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-roles']);
  }

  showDetail(entity, add) {
    const systemId = entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.params.entityId;
    if (add) {
      // When we add new object class, then we need id of role as parametr and use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`/system/${systemId}/roles/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`system/${systemId}/roles/${entity.id}/detail`);
    }
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleTable
            uiKey="system-role-table"
            roleManager={this.getManager()}
            filterOpened={false}
            showCatalogue={false}
            forceSearchParameters={forceSearchParameters} />
        </Basic.Panel>
      </div>
    );
  }
}
//
//   render() {
//     const { entityId } = this.props.params;
//     const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
//     return (
//       <div>
//         <Helmet title={this.i18n('title')} />
//         <Basic.Confirm ref="confirm-delete" level="danger"/>
//
//         <Basic.ContentHeader style={{ marginBottom: 0 }}>
//           <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
//         </Basic.ContentHeader>
//
//         <Basic.Panel className="no-border last">
//           <Advanced.Table
//             ref="table"
//             uiKey={uiKey}
//             manager={this.getManager()}
//             forceSearchParameters={forceSearchParameters}
//             showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
//             actions={
//               Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
//               ?
//               [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
//               :
//               null
//             }
//             buttons={
//               [
//                 <Basic.Button
//                   level="success"
//                   key="add_button"
//                   className="btn-xs"
//                   onClick={this.showDetail.bind(this, null, true)}
//                   rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
//                   <Basic.Icon type="fa" icon="plus"/>
//                   {' '}
//                   {this.i18n('button.add')}
//                 </Basic.Button>
//               ]
//             }>
//             <Advanced.Column
//               property=""
//               header=""
//               className="detail-button"
//               cell={
//                 ({ rowIndex, data }) => {
//                   return (
//                     <Advanced.DetailButton
//                       title={this.i18n('button.detail')}
//                       onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
//                   );
//                 }
//               }/>
//             <Advanced.ColumnLink
//               to="/role/:_target/detail"
//               target="_embedded.role.id"
//               access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
//               property="_embedded.role.name"
//               header={this.i18n('core:entity.IdentityRole.role')}
//               sort/>
//             <Advanced.ColumnLink
//               to="/system/:_target/detail"
//               target="_embedded.identityContract.id"
//               access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
//               property="_embedded.identityContract.id"
//               header={this.i18n('core:entity.IdentityRole.identityContract.title')}
//               />
//             <Advanced.Column
//               property="systemMapping"
//               header={this.i18n('acc:entity.RoleSystem.systemMapping')}
//               cell={
//                 ({ rowIndex, data }) => {
//                   const roleSystem = data[rowIndex];
//                   return (
//                     <Link to={`/system/${roleSystem._embedded.system.id}/mappings/${roleSystem._embedded.systemMapping.id}/detail`} >{systemMappingManager.getNiceLabel(roleSystem._embedded.systemMapping)}</Link>
//                   );
//                 }
//               }
//               />
//           </Advanced.Table>
//         </Basic.Panel>
//       </div>
//     );
//   }

SystemRoles.propTypes = {
  role: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SystemRoles.defaultProps = {
  role: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    role: Utils.Entity.getEntity(state, roleManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemRoles);
