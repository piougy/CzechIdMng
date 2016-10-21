import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import authorityHelp from './AuthoritiesPanel_cs.md';
import AuthoritiesPanel from './AuthoritiesPanel';
import * as Basic from '../../components/basic';
import { RoleManager, WorkflowProcessDefinitionManager, SecurityManager, IdentityManager, RoleCatalogueManager } from '../../redux';

const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
const roleManager = new RoleManager();
const identityManger = new IdentityManager();
const roleCatalogueManager = new RoleCatalogueManager();

class RoleDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      _showLoading: true
    };
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    const { entity } = this.props;

    if (Utils.Entity.isNew(entity)) {
      this._setSelectedEntity(entity);
    } else {
      this._setSelectedEntity(this._prepareEntity(entity));
    }
  }

  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (nextProps.entity && nextProps.entity !== entity) {
      this._setSelectedEntity(this._prepareEntity(nextProps.entity));
    }
  }

  _prepareEntity(entity) {
    entity = this._transformFromEmbedded(entity, 'subRoles', 'sub');
    entity = this._transformFromEmbedded(entity, 'superiorRoles', 'superior');
    entity = this._transformFromEmbedded(entity, 'guarantees', 'guarantee');
    entity = this._transformFromEmbeddedSimple(entity, 'roleCatalogue ');
    return entity;
  }

  /**
   * Method transform object attribute into attribute with id.
   */
  _transformFromEmbeddedSimple(entity, propertyName) {
    if (entity._embedded !== undefined && entity._embedded[propertyName] !== undefined) {
      entity[propertyName] = entity._embedded[propertyName].id;
    }
    return entity;
  }

  /**
   * Method transform list of entity to property.
   * When you need transform simple attribute into propery use @_transformFromEmbeddedSimple
   */
  _transformFromEmbedded(entity, propertyName, variableName) {
    const copyOfEntity = _.merge({}, entity);
    delete copyOfEntity[propertyName];
    copyOfEntity[propertyName] = entity[propertyName].map(function transform(obj) {
      if (obj._embedded !== undefined) {
        if (obj._embedded[variableName] === undefined) {
          return obj[variableName];
        }
        return obj._embedded[variableName].id;
      }
    });
    return copyOfEntity;
  }

  _setSelectedEntity(entity) {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    this.setState({
      _showLoading: true
    });
    const entity = this.refs.form.getData();
    // append selected authorities
    entity.authorities = this.refs.authorities.getWrappedInstance().getSelectedAuthorities();
    // append subroles
    if (entity.subRoles) {
      entity.subRoles = entity.subRoles.map(subRoleId => {
        return {
          sub: roleManager.getSelfLink(subRoleId)
        };
      });
    }
    if (entity.guarantees) {
      entity.guarantees = entity.guarantees.map(guaranteeId => {
        return {
          guarantee: identityManger.getSelfLink(guaranteeId)
        };
      });
    }
    // transform object roleCatalogue to self link
    if (entity.roleCatalogue) {
      entity.roleCatalogue = roleCatalogueManager.getSelfLink(entity.roleCatalogue);
    }
    // delete superior roles - we dont want to save them (they are ignored on BE anyway)
    delete entity.superiorRoles;
    //
    this.getLogger().debug('[RoleDetail] save entity', entity);
    if (Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(roleManager.createEntity(entity, null, (createdEntity, error) => {
        this._afterSave(createdEntity, error, true);
      }));
    } else {
      this.context.store.dispatch(roleManager.patchEntity(entity, null, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error, isNew = false) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (isNew) {
      this.context.router.push('/roles');
    }
    this._setSelectedEntity(this._prepareEntity(entity));
  }

  render() {
    const { entity, showLoading } = this.props;
    const { _showLoading } = this.state;
    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('tabs.basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm ref="form" showLoading={ _showLoading || showLoading } readOnly={!SecurityManager.hasAuthority('ROLE_WRITE')}>
                <Basic.Row>
                  <div className="col-lg-8">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('setting.basic.header')}</h3>
                    <div className="form-horizontal">
                      <Basic.TextField
                        ref="name"
                        label={this.i18n('entity.Role.name')}
                        required/>
                      <Basic.EnumSelectBox
                        ref="roleType"
                        label={this.i18n('entity.Role.roleType')}
                        enum={RoleTypeEnum}
                        required
                        readOnly={!Utils.Entity.isNew(entity)}/>
                      <Basic.SelectBox
                          ref="roleCatalogue"
                          label={this.i18n('entity.Role.roleCatalogue')}
                          manager={roleCatalogueManager}/>
                      <Basic.SelectBox
                        ref="superiorRoles"
                        label={this.i18n('entity.Role.superiorRoles')}
                        manager={roleManager}
                        multiSelect
                        readOnly
                        placeholder=""/>
                      <Basic.SelectBox
                        ref="subRoles"
                        label={this.i18n('entity.Role.subRoles')}
                        manager={roleManager}
                        multiSelect/>
                      <Basic.SelectBox
                          ref="guarantees"
                          label={this.i18n('entity.Role.guarantees')}
                          multiSelect
                          manager={identityManger}/>
                      <Basic.TextArea
                        ref="description"
                        label={this.i18n('entity.Role.description')}/>
                      <Basic.Checkbox
                        ref="disabled"
                        label={this.i18n('entity.Role.disabled')}/>
                    </div>

                    <h3 style={{ margin: '20px 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                      { this.i18n('setting.approval.header') }
                    </h3>
                    <Basic.SelectBox
                      labelSpan=""
                      componentSpan=""
                      ref="approveAddWorkflow"
                      label={this.i18n('entity.Role.approveAddWorkflow')}
                      forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.add') }
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                    <Basic.SelectBox
                      labelSpan=""
                      componentSpan=""
                      ref="approveRemoveWorkflow"
                      label={this.i18n('entity.Role.approveRemoveWorkflow')}
                      forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.remove') }
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                  </div>

                  <div className="col-lg-4">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                      <span dangerouslySetInnerHTML={{ __html: this.i18n('setting.authority.header') }} className="pull-left"/>
                      <Basic.HelpIcon content={authorityHelp} className="pull-right"/>
                      <div className="clearfix"/>
                    </h3>
                    <AuthoritiesPanel
                      ref="authorities"
                      roleManager={roleManager}
                      authorities={entity.authorities}
                      disabled={!SecurityManager.hasAuthority('ROLE_WRITE')}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={_showLoading}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('ROLE_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>


          </Basic.Panel>
        </form>
      </div>
    );
  }
}


RoleDetail.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
RoleDetail.defaultProps = {
};

export default connect()(RoleDetail);
