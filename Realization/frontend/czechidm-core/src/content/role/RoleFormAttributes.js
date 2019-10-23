import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleFormAttributeTable from './RoleFormAttributeTable';
import { RoleManager, FormDefinitionManager} from '../../redux';
let roleManager = null;
const formDefinitionManager = new FormDefinitionManager();

/**
 * Role form attributes (sub-definition)
 *
 * @author Vít Švanda
 */
class RoleFormAttributes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.roles';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-form-attributes', this.props.match.params);
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;

    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    roleManager = this.getRequestManager(this.props.match.params, new RoleManager());

    this.context.store.dispatch(roleManager.fetchEntity(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if ((nextProps.match.params && this.props.match.params && nextProps.match.params.requestId !== this.props.match.params.requestId)
      || (nextProps.entity && nextProps.entity !== entity)) {
      // Init manager - evaluates if we want to use standard (original) manager or
      // universal request manager (depends on existing of 'requestId' param)
      roleManager = this.getRequestManager(nextProps.match.params, new RoleManager());
    }
  }

  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    this.setState({
      _showLoading: true
    }, () => {
      const entity = this.refs.form.getData();
      this.refs.form.processStarted();
      //
      this.getLogger().debug('[RoleFormAttributeDetail] save entity', entity);
      this.context.store.dispatch(roleManager.updateEntity(entity, null, (patchedEntity, error) => {
        this._afterSave(patchedEntity, error, afterAction);
      }));
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.processEnded();
      if (error) {
        this.addError(error);
        return;
      }
      //
      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      if (afterAction === 'CLOSE') {
        this.context.history.replace(this.addRequestPrefix('roles', this.props.match.params));
      }
    });
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.match.params.entityId);
    const { entity, showLoading, _permissions} = this.props;
    const _showLoading = showLoading;
    if (!roleManager || !entity) {
      return null;
    }
    const identityRoleAttributeForceSearch = new SearchParameters()
        .setFilter('type', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole');

    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <Basic.ContentHeader text={ this.i18n('entity.Role.identityRoleAttributeDefinition.label') } style={{ marginBottom: 0 }}/>
        <Basic.AbstractForm
          ref="form"
          data={entity}
          showLoading={ _showLoading }
          readOnly={ !roleManager.canSave(entity, _permissions) }>
          <div style={{ display: 'flex' }}>
            <div style={{ flex: 1 }}>
              <Basic.SelectBox
                ref="identityRoleAttributeDefinition"
                manager={formDefinitionManager}
                forceSearchParameters={identityRoleAttributeForceSearch}
                label={ null }
                helpBlock={this.i18n('entity.Role.identityRoleAttributeDefinition.help')}/>
            </div>
            <Basic.Button
              type="button"
              style={{ marginLeft: 3 }}
              level="success"
              title={ this.i18n('button.saveAndContinue') }
              onClick={ this.save.bind(this, 'CONTINUE') }
              showLoading={ _showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ roleManager.canSave(entity, _permissions) }>
              {this.i18n('button.save')}
            </Basic.Button>
          </div>
        </Basic.AbstractForm>
        <Basic.Div rendered={entity.identityRoleAttributeDefinition}>
          <Basic.ContentHeader icon="fa:th-list" text={ this.i18n('content.role.formAttributes.header') } style={{ marginBottom: 0 }}/>
          <RoleFormAttributeTable
            uiKey="role-form-attributes-table"
            forceSearchParameters={ forceSearchParameters }
            className="no-margin"
            formDefinition={entity.identityRoleAttributeDefinition}
            match={ this.props.match }/>
        </Basic.Div>
      </div>
    );
  }
}

function select(state, component) {
  if (!roleManager) {
    return {};
  }
  const { entityId } = component.match.params;
  return {
    entity: roleManager.getEntity(state, entityId),
    showLoading: roleManager.isShowLoading(state, null, entityId),
    _permissions: roleManager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(RoleFormAttributes);
