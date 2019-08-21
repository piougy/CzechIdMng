import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import ConfigLoader from '../../utils/ConfigLoader';
//
import {
  RoleManager,
  RequestManager,
  SecurityManager,
  RoleCatalogueManager,
  ConfigurationManager,
  CodeListManager
} from '../../redux';

// Table uiKey
const requestManager = new RequestManager();
const codeListManager = new CodeListManager();

/**
* Table of roles.
*
* FIXME: use default RoleManager and rename prop to 'manager'.
*
* @author Radek Tomiška
*/
class RoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened
    };
    this.roleCatalogueManager = new RoleCatalogueManager();
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.props.showEnvironment) {
      this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded('environment'));
    }
    this.refs.text.focus();
  }

  getContentKey() {
    return 'content.roles';
  }

  getManager() {
    return this.props.roleManager;
  }

  getDefaultSearchParameters() {
    let searchParameters = this.getManager().getDefaultSearchParameters();
    //
    if (this.props.showEnvironment) {
      searchParameters = searchParameters.setFilter('environment', ConfigLoader.getConfig('role.table.filter.environment', []));
    }
    //
    return searchParameters;
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

  _validateCreateRequestDialog(result) {
    if (result === 'reject') {
      return true;
    }
    if (result === 'confirm' && this.refs['new-request-form'].isFormValid()) {
      return true;
    }
    return false;
  }

  _focusOnRequestDialog() {
    this.refs['role-name'].focus();
  }

  createRequest(event) {
    if (event && event.preventDefault) {
      event.preventDefault();
    }

    this.refs[`confirm-new-request`].show(
      null,
      this.i18n(`content.roles.action.createRequest.header`),
      this._validateCreateRequestDialog.bind(this),
      this._focusOnRequestDialog.bind(this)
    ).then(() => {
      const roleName = this.refs[`role-name`].getValue();
      const promise = requestManager.getService().createRequest('roles', {name: roleName, code: roleName});
      promise.then((json) => {
        // Init universal request manager (manually)
        const manager = this.getRequestManager({requestId: json.id}, new RoleManager());
        // Fetch entity - we need init permissions for new manager
        this.context.store.dispatch(manager.fetchEntityIfNeeded(json.ownerId, null, (e, error) => {
          this.handleError(error);
        }));
        // Redirect to new request
        this.context.router.push(`${this.addRequestPrefix('role', {requestId: json.id})}/${json.ownerId}/detail`);
      }).catch(ex => {
        this.setState({
          showLoading: false
        });
        this.handleError(ex);
      });
    });
  }

  showDetail(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/role/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/role/${entity.id}/detail`);
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
      this.context.store.dispatch(roleManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          // redirect to role detail with identities table
          if (error.statusEnum === 'ROLE_DELETE_FAILED_IDENTITY_ASSIGNED') {
            this.context.router.push(`/role/${entity.id}/identities`);
            this.addMessage({
              position: 'tc',
              level: 'info',
              title: this.i18n('delete.identityAssigned.title'),
              message: this.i18n('delete.identityAssigned.message', { role: roleManager.getNiceLabel(entity) })
            });
          } else {
            this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: roleManager.getNiceLabel(entity) }) }, error);
          }
        }
        if (!error && successEntities) {
          this.refs.table.getWrappedInstance().reload();
        }
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
    const data = {
      ... this.refs.filterForm.getData(),
      roleCatalogue: nodeId
    };
    this.refs.roleCatalogue.setValue(nodeId);
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  render() {
    const {
      uiKey,
      roleManager,
      columns,
      showCatalogue,
      forceSearchParameters,
      _requestsEnabled,
      className,
      showAddButton,
      showEnvironment
    } = this.props;
    const { filterOpened, showLoading } = this.state;
    const _showTree = showCatalogue && SecurityManager.hasAuthority('ROLECATALOGUE_AUTOCOMPLETE');
    //
    return (
      <Basic.Row>
        <Basic.Confirm ref="confirm-new-request" level="success">
          <Basic.AbstractForm ref="new-request-form" uiKey="confirm-new-request" >
            <Basic.TextField
              label={ this.i18n('content.roles.action.createRequest.name') }
              ref="role-name"
              placeholder={ this.i18n('content.roles.action.createRequest.message') }
              required/>
          </Basic.AbstractForm>
        </Basic.Confirm>

        {/* FIXME: resposive design - wrong wrapping on mobile */}
        <Basic.Col
          lg={ 3 }
          style={ _showTree ? { paddingRight: 0 } : {} }
          rendered={ _showTree }>
          <Advanced.Tree
            ref="roleCatalogueTree"
            uiKey="role-catalogue-tree"
            manager={ this.roleCatalogueManager }
            onSelect={ this._useFilterByTree.bind(this) }
            header={ this.i18n('content.roles.roleCataloguePick') }
            rendered={ _showTree }/>
        </Basic.Col>

        <Basic.Col lg={ !_showTree ? 12 : 9 } style={ _showTree ? { paddingLeft: 0 } : {} }>
          <Basic.Confirm ref="confirm-delete" level="danger"/>

          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ roleManager }
            rowClass={ ({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }
            filterOpened={ filterOpened }
            forceSearchParameters={ forceSearchParameters }
            showRowSelection
            style={ !_showTree ? {} : { borderLeft: '1px solid #ddd' } }
            showLoading={ showLoading }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className={ _showTree ? '' : 'last'}>
                    <Basic.Col lg={ showEnvironment ? 4 : 8 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('content.roles.filter.text.placeholder')}
                        help={ Advanced.Filter.getTextHelp() }/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className={ showEnvironment ? '' : 'hidden'}>
                      <Advanced.CodeListSelect
                        ref="environment"
                        code="environment"
                        label={ null }
                        placeholder={ this.i18n('entity.Role.environment.label') }
                        multiSelect/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className="last" rendered={ _showTree }>
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.RoleCatalogueSelect
                        ref="roleCatalogue"
                        label={ null }
                        placeholder={ this.i18n('entity.Role.roleCatalogue.name') }
                        header={ this.i18n('entity.Role.roleCatalogue.name') }/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            buttons={[
              <span>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={ this.showDetail.bind(this, { }) }
                  rendered={ showAddButton && !_requestsEnabled && SecurityManager.hasAuthority('ROLE_CREATE') }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
                <Basic.Button
                  level="success"
                  key="add_request"
                  className="btn-xs"
                  onClick={ this.createRequest.bind(this) }
                  rendered={ showAddButton && _requestsEnabled && SecurityManager.hasAuthority('ROLE_CREATE') }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
              </span>
            ]}
            _searchParameters={ this.getSearchParameters() }
            className={ className }>

            <Advanced.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={ this.i18n('button.detail') }
                      onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                  );
                }
              }
              sort={false}/>
            <Advanced.ColumnLink to="role/:id/detail" property="name" width="15%" sort face="text" rendered={_.includes(columns, 'name')}/>
            <Advanced.Column property="baseCode" width={ 125 } face="text" sort rendered={_.includes(columns, 'baseCode')}/>
            <Advanced.Column
              property="environment"
              width={ 100 }
              face="text"
              sort
              rendered={ _.includes(columns, 'environment') && showEnvironment }
              cell={
                ({ rowIndex, data, property }) => {
                  return (
                    <Advanced.CodeListValue code="environment" value={ data[rowIndex][property] }/>
                  );
                }
              }
              />
            <Advanced.Column property="roleType" width={ 75 } sort face="enum" enumClass={ RoleTypeEnum } rendered={ false && _.includes(columns, 'roleType') }/>
            <Advanced.Column property="roleCatalogue.name" width={ 75 } face="text" rendered={ _.includes(columns, 'roleCatalogue') }/>
            <Advanced.Column
              property="description"
              sort
              face="text"
              rendered={ _.includes(columns, 'description') }
              maxLength={ 100 }/>
            <Advanced.Column property="disabled" sort face="bool" width={ 75 } rendered={ _.includes(columns, 'disabled') }/>
          </Advanced.Table>
        </Basic.Col>
      </Basic.Row>
    );
  }
}

RoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * If role catalogue is shown
   */
  showCatalogue: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Css
   */
  className: PropTypes.string,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool
};

RoleTable.defaultProps = {
  columns: ['name', 'baseCode', 'environment', 'roleType', 'disabled', 'approvable', 'description'],
  filterOpened: true,
  showCatalogue: true,
  forceSearchParameters: null,
  showAddButton: true
};

function select(state, component) {
  return {
    showEnvironment: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.environment', true),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _requestsEnabled: ConfigurationManager.getPublicValueAsBoolean(state, component.roleManager.getEnabledPropertyKey())
  };
}

export default connect(select, null, null, { withRef: true })(RoleTable);
