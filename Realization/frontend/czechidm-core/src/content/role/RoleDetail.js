import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import RolePriorityEnum from '../../enums/RolePriorityEnum';
import authorityHelp from './AuthoritiesPanel_cs.md';
import AuthoritiesPanel from './AuthoritiesPanel';
import * as Basic from '../../components/basic';
import { RoleManager, IdentityManager, RoleCatalogueManager, SecurityManager } from '../../redux';

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
      entity.priorityEnum = RolePriorityEnum.NONE;
      entity.priority = RolePriorityEnum.getPriority(RolePriorityEnum.NONE) + '';
      this._setSelectedEntity(entity);
    } else {
      this._setSelectedEntity(this._prepareEntity(entity));
    }
  }

  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (nextProps.entity && nextProps.entity !== entity && nextProps.entity.subRoles) {
      this._setSelectedEntity(this._prepareEntity(nextProps.entity));
    }
  }

  _prepareEntity(entity) {
    const copyOfEntity = _.merge({}, entity); // we can not modify given entity
    // we dont need to load entities again - we have them in embedded objects
    copyOfEntity.subRoles = !entity.subRoles ? [] : entity.subRoles.map(subRole => { return subRole._embedded.sub; });
    copyOfEntity.superiorRoles = !entity.superiorRoles ? [] : entity.superiorRoles.map(superiorRole => { return superiorRole._embedded.superior; });
    copyOfEntity.guarantees = !entity.guarantees ? [] : entity.guarantees.map(guarantee => { return guarantee._embedded.guarantee; });
    copyOfEntity.roleCatalogues = !entity.roleCatalogues ? [] : entity.roleCatalogues.map(roleCatalogue => { return roleCatalogue._embedded.roleCatalogue; } );
    copyOfEntity.priorityEnum = RolePriorityEnum.getKeyByPriority(copyOfEntity.priority);
    copyOfEntity.priority = copyOfEntity.priority + ''; // We have to do convert form int to string (cause TextField and validator)
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
      // transform roleCatalogues to self links
      if (entity.roleCatalogues) {
        entity.roleCatalogues = entity.roleCatalogues.map(roleCatalogueId => {
          return {
            roleCatalogue: roleCatalogueManager.getSelfLink(roleCatalogueId)
          };
        });
      }
      // delete superior roles - we dont want to save them (they are ignored on BE anyway)
      delete entity.superiorRoles;
      //
      this.getLogger().debug('[RoleDetail] save entity', entity);
      if (Utils.Entity.isNew(entity)) {
        this.context.store.dispatch(roleManager.createEntity(entity, null, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(roleManager.patchEntity(entity, null, (patchedEntity, error) => {
          this._afterSave(patchedEntity, error, afterAction);
        }));
      }
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
        this.context.router.replace(`roles`);
      } else {
        this.context.router.replace(`role/${entity.id}/detail`);
      }
    });
  }

  _onChangePriorityEnum(item) {
    if (item) {
      const priority = RolePriorityEnum.getPriority(item.value);
      this.refs.priority.setValue(priority + '');
    } else {
      this.refs.priority.setValue(null);
    }
  }

  render() {
    const { entity, showLoading, _permissions } = this.props;
    const { _showLoading } = this.state;
    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this, 'CONTINUE')}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('tabs.basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading || showLoading }
                readOnly={ !Utils.Entity.isNew(entity) ? !Utils.Permission.hasPermission(_permissions, 'UPDATE') : !SecurityManager.hasAuthority('ROLE_CREATE') }>
                <Basic.Row>
                  <div className="col-lg-8">
                    <div>
                      <Basic.TextField
                        ref="name"
                        label={this.i18n('entity.Role.name')}
                        required
                        min={0}
                        max={255}/>
                      <Basic.EnumSelectBox
                        ref="roleType"
                        label={this.i18n('entity.Role.roleType')}
                        enum={RoleTypeEnum}
                        required
                        readOnly={!Utils.Entity.isNew(entity)}/>
                      <Basic.EnumSelectBox
                        ref="priorityEnum"
                        label={this.i18n('entity.Role.priorityEnum')}
                        enum={RolePriorityEnum}
                        onChange={this._onChangePriorityEnum.bind(this)}/>
                      <Basic.TextField
                        ref="priority"
                        label={this.i18n('entity.Role.priority')}
                        readOnly
                        required/>
                      <Basic.SelectBox
                        multiSelect
                        ref="roleCatalogues"
                        label={this.i18n('entity.Role.roleCatalogue.name')}
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
                      <Basic.Checkbox
                        ref="approveRemove"
                        label={this.i18n('entity.Role.approveRemove')}/>
                      <Basic.TextArea
                        ref="description"
                        label={this.i18n('entity.Role.description')}
                        max={2000}/>
                      <Basic.Checkbox
                        ref="disabled"
                        label={this.i18n('entity.Role.disabled')}/>
                    </div>
                  </div>

                  <div className="col-lg-4">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                      <span dangerouslySetInnerHTML={{ __html: this.i18n('setting.authority.header') }} className="pull-left"/>
                      <Basic.HelpIcon content={authorityHelp} className="pull-right"/>
                      <div className="clearfix"/>
                    </h3>
                    <AuthoritiesPanel
                      ref="authorities"
                      roleManager={ roleManager }
                      authorities={ entity.authorities }
                      disabled={ !Utils.Entity.isNew(entity) ? !Utils.Permission.hasPermission(_permissions, 'UPDATE') : !SecurityManager.hasAuthority('ROLE_CREATE') } />
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={_showLoading}>{this.i18n('button.back')}</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={ this.i18n('button.saveAndContinue') }
                onClick={ this.save.bind(this, 'CONTINUE') }
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ !Utils.Entity.isNew(entity) ? Utils.Permission.hasPermission(_permissions, 'UPDATE') : SecurityManager.hasAuthority('ROLE_CREATE') }
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}

RoleDetail.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
RoleDetail.defaultProps = {
  _permissions: null
};

function select(state, component) {
  if (!component.entity) {
    return {};
  }
  return {
    _permissions: roleManager.getPermissions(state, null, component.entity.id)
  };
}

export default connect(select)(RoleDetail);
