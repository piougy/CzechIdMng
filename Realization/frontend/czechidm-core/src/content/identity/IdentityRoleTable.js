import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import { IdentityRoleManager, IdentityManager, RoleTreeNodeManager, RoleManager, IdentityContractManager, CodeListManager, DataManager } from '../../redux';
import IdentityRoleEav from './IdentityRoleEav';
import IncompatibleRoleWarning from '../role/IncompatibleRoleWarning';
import FormInstance from '../../domain/FormInstance';

const manager = new IdentityRoleManager();
const identityManager = new IdentityManager();
const roleManager = new RoleManager();
const roleTreeNodeManager = new RoleTreeNodeManager();
const identityContractManager = new IdentityContractManager();
const codeListManager = new CodeListManager();
const uiKeyIncompatibleRoles = 'identity-incompatible-roles-';

const TEST_ADD_ROLE_DIRECTLY = false;

/**
 * Table of assigned roles ~ identity roles
 * - table suppors add permission
 *
 * @author Radek Tomiška
 */
export class IdentityRoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.params;
    this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded('environment'));
    this.context.store.dispatch(identityManager.fetchIncompatibleRoles(entityId, `${ uiKeyIncompatibleRoles }${ entityId }`));
  }

  getContentKey() {
    return 'content.identity.roles';
  }

  getManager() {
    return manager;
  }

  /**
   * Can change identity permission
   *
   * @return {[type]} [description]
   */
  _canChangePermissions() {
    const { _permissions } = this.props;
    //
    return Utils.Permission.hasPermission(_permissions, 'CHANGEPERMISSION');
  }

  _changePermissions() {
    const { entityId } = this.props.params;
    const identity = identityManager.getEntity(this.context.store.getState(), entityId);
    //
    const uuidId = uuid.v1();
    this.context.router.push(`/role-requests/${uuidId}/new?new=1&applicantId=${identity.id}`);
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  showDetail(entity) {
    super.showDetail(entity, () => {
      this.refs.role.focus();
    });
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  _getIncompatibleRoles(entity) {
    const { _incompatibleRoles } = this.props;
    //
    if (!_incompatibleRoles) {
      return [];
    }
    //
    return _incompatibleRoles.filter(ir => ir.directRole.id === entity.role);
  }

  _attributesCell({rowIndex, data}) {
    const value = data[rowIndex];
    const result = [];
    if ( value
      && value._eav
      && value._eav.length === 1
      && value._eav[0].formDefinition) {
      const formInstance = value._eav[0];
      const _formInstance = new FormInstance(formInstance.formDefinition, formInstance.values, formInstance.validationErrors);
      result.push(
          <Advanced.EavForm
            key={ _.uniqueId(`${rowIndex}-${value.id}`) }
            ref="eavForm"
            formInstance={ _formInstance }
            readOnly
            useDefaultValue={false}/>
        );
    }
    return (
      <Basic.Div className="abstract-form condensed" style={{minWidth: 150, padding: 0}}>
        {result}
      </Basic.Div>
    );
  }

  render() {
    const {
      forceSearchParameters,
      showAddButton,
      showDetailButton,
      _showLoading,
      columns,
      className,
      rendered
    } = this.props;
    const { detail, activeKey } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <div>
        <Advanced.Table
          uiKey={ this.getUiKey() }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRefreshButton
          className={ className }
          showToolbar
          buttons={[
            <Basic.Button
              level="success"
              className="btn-xs"
              onClick={ this.showDetail.bind(this, {}) }
              rendered={ showAddButton && TEST_ADD_ROLE_DIRECTLY }>
              <Basic.Icon value="fa:plus"/>
              {' '}
              {this.i18n('button.add')}
            </Basic.Button>,
            <Basic.Button
              level="warning"
              onClick={ this._changePermissions.bind(this) }
              rendered={ showAddButton }
              disabled={ !this._canChangePermissions() }
              title={ this._canChangePermissions() ? null : this.i18n('security.access.denied') }
              titlePlacement="bottom">
              <Basic.Icon type="fa" icon="key"/>
              {' '}
              { this.i18n('changePermissions') }
            </Basic.Button>
          ]}
          _searchParameters={ this.getSearchParameters() }
          rowClass={
            ({rowIndex, data}) => {
              const entity = data[rowIndex];
              if (this._getIncompatibleRoles(entity).length > 0) {
                // RT: is looks to agressive? Or combine disabled + incompatible
                // return 'warning';
              }
              return Utils.Ui.getRowClass(entity);
            }}
            >
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
            rendered={ showDetailButton }/>
          <Advanced.Column
            header={this.i18n('entity.IdentityRole.role')}
            sort
            sortProperty="role.name"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                const content = [];
                //
                content.push(
                  <IncompatibleRoleWarning incompatibleRoles={ this._getIncompatibleRoles(entity) }/>
                );
                content.push(
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.role }
                    entity={ entity._embedded.role }
                    face="popover"
                    showIcon/>
                );
                //
                return content;
              }
            }
            rendered={ _.includes(columns, 'role') }/>
          <Advanced.Column
            header={this.i18n('entity.Role.environment.label')}
            title={this.i18n('entity.Role.environment.help')}
            width={ 125 }
            face="text"
            sort
            sortProperty="role.environment"
            rendered={_.includes(columns, 'environment')}
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.CodeListValue code="environment" value={ data[rowIndex]._embedded.role.environment }/>
                );
              }
            }
            />
          <Advanced.Column
            header={this.i18n('content.task.IdentityRoleConceptTable.identityRoleAttributes.header')}
            cell={
              ({rowIndex, data}) => {
                return this._attributesCell({ rowIndex, data });
              }
            }/>
          <Advanced.Column
            header={this.i18n('entity.IdentityRole.identityContract.title')}
            property="identityContract"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                return (
                  <Advanced.EntityInfo
                    entityType="identityContract"
                    entityIdentifier={ data[rowIndex][property] }
                    entity={ data[rowIndex]._embedded[property] }
                    showIdentity={ false }
                    face="popover"
                    showIcon />
                );
              }
            }
            rendered={ _.includes(columns, 'identityContract') }/>
          <Advanced.Column
            header={this.i18n('entity.IdentityRole.contractPosition.label')}
            property="contractPosition"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                if (!data[rowIndex][property]) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="contractPosition"
                    entityIdentifier={ data[rowIndex][property] }
                    entity={ data[rowIndex]._embedded[property] }
                    showIdentity={ false }
                    face="popover" />
                );
              }
            }
            rendered={ _.includes(columns, 'contractPosition') }/>
          <Advanced.Column
            property="validFrom"
            header={this.i18n('label.validFrom')}
            face="date"
            sort
            rendered={ _.includes(columns, 'validFrom') }/>
          <Advanced.Column
            property="validTill"
            header={this.i18n('label.validTill')}
            face="date"
            sort
            rendered={ _.includes(columns, 'validTill') }/>
          <Advanced.Column
            property="directRole"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                if (!data[rowIndex][property]) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="identityRole"
                    entityIdentifier={ data[rowIndex][property] }
                    entity={ data[rowIndex]._embedded[property] }
                    showIdentity={ false }
                    face="popover" />
                );
              }
            }
            width={ 150 }
            rendered={ _.includes(columns, 'directRole') }/>

          <Advanced.Column
            property="automaticRole"
            header={ <Basic.Icon value="component:automatic-role"/> }
            title={ this.i18n('entity.IdentityRole.automaticRole.help') }
            face="bool"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data }) => {
                return (
                  <Basic.BooleanCell propertyValue={ data[rowIndex].automaticRole !== null } className="column-face-bool"/>
                );
              }
            }
            width={ 15 }
            rendered={ _.includes(columns, 'automaticRole') }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard={ !_showLoading }
          rendered={ showDetailButton }>

          <form onSubmit={ this.save.bind(this) }>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('create.header') } rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={this.i18n('edit.header', { role: detail.entity._embedded ? roleManager.getNiceLabel(detail.entity._embedded.role) : null })}
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.Tabs
                activeKey={ activeKey }
                onSelect={ this._onChangeSelectTabs.bind(this)}>
                <Basic.Tab
                  eventKey={ 1 }
                  title={ this.i18n('detail.tabs.basic') }
                  style={{ padding: 15}}
                  >
                  <Basic.AbstractForm ref="form" showLoading={ _showLoading } readOnly={ !TEST_ADD_ROLE_DIRECTLY }>
                    <Basic.SelectBox
                      ref="role"
                      manager={ roleManager }
                      label={ this.i18n('entity.IdentityRole.role') }
                      required/>
                    <Basic.SelectBox
                      ref="identityContract"
                      manager={ identityContractManager }
                      label={ this.i18n('entity.IdentityRole.identityContract.label') }
                      helpBlock={ this.i18n('entity.IdentityRole.identityContract.help') }
                      readOnly={ !TEST_ADD_ROLE_DIRECTLY }
                      required/>
                    <Basic.LabelWrapper
                      label={ this.i18n('entity.IdentityRole.automaticRole.label') }
                      helpBlock={ this.i18n('entity.IdentityRole.automaticRole.help') }
                      rendered={ detail.entity.automaticRole }>
                      { detail.entity.automaticRole ? roleTreeNodeManager.getNiceLabel(detail.entity._embedded.automaticRole) : null }
                    </Basic.LabelWrapper>
                    <Basic.Row>
                      <Basic.Col md={ 6 }>
                        <Basic.DateTimePicker
                          mode="date"
                          ref="validFrom"
                          label={this.i18n('label.validFrom')}/>
                      </Basic.Col>
                      <Basic.Col md={ 6 }>
                        <Basic.DateTimePicker
                          mode="date"
                          ref="validTill"
                          label={this.i18n('label.validTill')}/>
                      </Basic.Col>
                    </Basic.Row>
                  </Basic.AbstractForm>
                  {
                    detail.entity.directRole !== null
                    ||
                    <div>
                      <Basic.ContentHeader style={{ marginBottom: 0 }} className="marginable">
                        <Basic.Icon value="component:sub-roles"/>
                        {' '}
                        { this.i18n('detail.directRole.subRoles.header') }
                      </Basic.ContentHeader>
                      <IdentityRoleTable
                        uiKey={ `${this.getUiKey()}-all-sub-roles` }
                        showAddButton={ false }
                        forceSearchParameters={ new SearchParameters().setFilter('directRoleId', detail.entity.id) }
                        showDetailButton={ false }
                        params={ this.props.params }
                        columns={ _.difference(IdentityRoleTable.defaultProps.columns, ['identityContract', 'directRole', 'automaticRole']) }
                        className="marginable"/>
                    </div>
                  }
                </Basic.Tab>
                <Basic.Tab
                  eventKey={ 2 }
                  rendered={detail && detail.entity && detail.entity._embedded && detail.entity._embedded.role.identityRoleAttributeDefinition ? true : false}
                  style={{ padding: 15}}
                  title={this.i18n('detail.tabs.attributes')}>
                    <IdentityRoleEav
                      entityId={detail.entity.id}
                      entity={detail.entity}
                    />
                </Basic.Tab>
              </Basic.Tabs>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={ TEST_ADD_ROLE_DIRECTLY }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

IdentityRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Rendered columns - see table columns above
   *
   * TODO: move to advanced table and add column sorting
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  /**
   * Button for show entity detail
   */
  showDetailButton: PropTypes.bool,
  /**
   * Css
   */
  className: PropTypes.string,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};

IdentityRoleTable.defaultProps = {
  rendered: true,
  columns: ['role', 'environment', 'identityContract', 'contractPosition', 'validFrom', 'validTill', 'directRole', 'automaticRole'],
  forceSearchParameters: null,
  showAddButton: true,
  showDetailButton: true,
  _permissions: null
};

function select(state, component) {
  return {
    i18nReady: state.config.get('i18nReady'),
    _showLoading: Utils.Ui.isShowLoading(state, component.uiKey),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _incompatibleRoles: DataManager.getData(state, `${ uiKeyIncompatibleRoles }${ component.params.entityId }`)
  };
}

export default connect(select, null, null, { withRef: true })(IdentityRoleTable);
