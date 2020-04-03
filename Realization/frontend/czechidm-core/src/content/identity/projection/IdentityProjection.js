import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Joi from 'joi';
import moment from 'moment';
import Immutable from 'immutable';
import _ from 'lodash';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import * as Domain from '../../../domain';
import {
  IdentityManager,
  IdentityContractManager,
  ContractPositionManager,
  IdentityProjectionManager,
  FormProjectionManager,
  DataManager,
  ConfigurationManager,
  SecurityManager
} from '../../../redux';
import IdentityStateEnum from '../../../enums/IdentityStateEnum';
import OrganizationPosition from '../OrganizationPosition';
import IdentityRoles from '../IdentityRoles';

const PASSWORD_PREVALIDATION = 'PASSWORD_PREVALIDATION';

const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();
const contractPositionManager = new ContractPositionManager();
const identityProjectionManager = new IdentityProjectionManager();
const formProjectionManager = new FormProjectionManager();


/**
 * Univarzal form for identity projection..
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class IdentityProjection extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      generatePassword: false,
      generatePasswordShowLoading: false
    };
  }

  getContentKey() {
    return 'content.identity.projection';
  }

  getNavigationKey() {
    return 'identities';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { identityProjection, location } = this.props;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    if (isNew) {
      let formProjectionId;
      if (identityProjection && identityProjection.identity && identityProjection.identity.formProjection) {
        formProjectionId = identityProjection.identity.formProjection;
      } else {
        formProjectionId = Utils.Ui.getUrlParameter(this.props.location, 'projection');
      }
      if (!formProjectionId) {
        // form projection not found - default will be shown
        this._initProjection(entityId, identityProjection, {});
      } else {
        // fetch projection definition
        this.context.store.dispatch(formProjectionManager.autocompleteEntityIfNeeded(formProjectionId, null, (entity, error) => {
          if (error) {
            this.addError(error);
          } else {
            this._initProjection(entityId, identityProjection, entity);
          }
        }));
      }
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${ entityId }]`);
      this.context.store.dispatch(identityProjectionManager.fetchProjection(entityId, null, (entity, error) => {
        if (error) {
          this.addError(error);
        } else {
          this._initProjection(entityId, entity);
        }
      }));
    }
  }

  _initProjection(entityId, identityProjection = null, formProjection = null) {
    const { location } = this.props;
    const { generatePassword } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    // prepare form friendly projection
    let _identityProjection = null;
    if (identityProjection) {
      if (!formProjection && identityProjection.identity && identityProjection.identity._embedded) {
        formProjection = identityProjection.identity._embedded.formProjection;
      }
      let validFrom = null;
      let validTill = null;
      let workPosition = null;
      let contractEav = null;
      if (identityProjection.contract) {
        validFrom = identityProjection.contract.validFrom;
        validTill = identityProjection.contract.validTill;
        workPosition = identityProjection.contract.workPosition;
        contractEav = identityProjection.contract._eav;
      } else {
        validFrom = isNew ? moment() : null;
      }
      let otherPosition = null;
      if (identityProjection.otherPositions && identityProjection.otherPositions.length > 0) {
        otherPosition = identityProjection.otherPositions[0];
      }
      //
      _identityProjection = {
        ...identityProjection.identity,
        identity: identityProjection.identity,
        email: identityProjection.identity.email || '',
        contract: identityProjection.contract,
        validFrom,
        validTill,
        workPosition,
        contractEav,
        otherContracts: identityProjection.otherContracts,
        otherPosition,
        otherWorkPosition: otherPosition ? otherPosition.workPosition : null,
        otherPositions: identityProjection.otherPositions,
        role:
          identityProjection.identityRoles
          ?
          identityProjection.identityRoles
            .filter(identityRole => Utils.Entity.isValid(identityRole))
            .filter(identityRole => !identityRole.directRole)
            .filter(identityRole => !identityRole.automaticRole)
            .map(identityRole => identityRole.role)
          :
          null,
        identityRoles: identityProjection.identityRoles
      };
    } else {
      // new projection
      _identityProjection = {
        id: entityId,
        email: '', // TODO: fix email validator ... doesn't work with null
        identity: {
          id: entityId,
          formProjection: formProjection ? formProjection.id : Utils.Ui.getUrlParameter(this.props.location, 'projection')
        },
        validFrom: moment()
      };
    }
    // prepare new contract
    if (!_identityProjection.contract
        && isNew
        && identityContractManager.canSave()) {
      _identityProjection.contract = {};
    }
    // prepare new contract
    if (!_identityProjection.contract
        && isNew
        && identityContractManager.canSave()) {
      _identityProjection.contract = {};
    }
    //
    this.setState({
      identityProjection: _identityProjection,
      formProjection
    }, () => {
      if (this.refs.username) {
        this.refs.username.focus();
      }
      if (isNew && this.refs.password) {
        if (generatePassword) {
          this.generatePassword();
        }
        this._preValidate();
      }
    });
  }

  /*
   * Method shows password rules before applying change of password
   */
  _preValidate() {
    const requestData = {
      accounts: []
    };
    requestData.idm = true;

    identityManager.preValidate(requestData)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        let error;
        if (Utils.Response.getFirstError(json)) {
          error = Utils.Response.getFirstError(json);
        } else if (json._errors) {
          error = json._errors.pop();
        }

        if (error) {
          this.setState({
            validationError: error,
            validationDefinition: true
          });

          throw error;
        }
        return json;
      })
      .catch(error => {
        if (!error) {
          return;
        }
        if (error.statusEnum === PASSWORD_PREVALIDATION) {
          this.addErrorMessage({ hidden: true }, error);
        } else {
          this.addError(error);
        }
      });
  }

  /**
   * Return true when currently logged user can change password.
   *
   */
  _canPasswordChange(identityProjection) {
    if (!identityProjection || !identityProjection._permissions) {
      return false;
    }
    const { passwordChangeType } = this.props;
    //
    return identityManager.canChangePassword(passwordChangeType, identityProjection._permissions);
  }

  setNewPassword(password) {
    this.setState({
      password,
      passwordAgain: password
    });
  }

  generatePassword(event = null) {
    const generate = event ? event.currentTarget.checked : true;
    this.setState({
      generatePassword: generate
    }, () => {
      if (!generate) {
        this.setNewPassword(null);
        this.refs.password.focus();
        return;
      }
      // generate
      this.setState({
        generatePasswordShowLoading: true
      }, () => {
        identityManager
          .getService()
          .generatePassword()
          .then(response => response.json())
          .then(json => {
            if (!json.error) {
              this.setState({
                generatePasswordShowLoading: false,
                generatePassword: true
              }, () => {
                this.setNewPassword(json.content);
              });
            } else {
              this.setState({
                generatePasswordShowLoading: false,
                generatePassword: false
              }, () => {
                this.addError(json.error);
              });
            }
          })
          .catch(error => {
            this.setState({
              generatePasswordShowLoading: false,
              generatePassword: false
            }, () => {
              this.addError(error);
            });
          });
      });
    });
  }

  _isRendered(formProjection, field) {
    if (!formProjection || !formProjection.basicFields) {
      return true;
    }
    //
    try {
      const basicFields = JSON
        .parse(formProjection.basicFields)
        .map(f => {
          if (_.isObject(f)) {
            return !f.code || f.code.toLowerCase();
          }
          return !f || f.toLowerCase();
        });
      return _.includes(basicFields, field.toLowerCase());
    } catch (syntaxError) {
      // nothing
    }
    //
    return true; // rendered by default
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()
        || (this.refs.eav && !this.refs.eav.isValid())
        || (this.refs.contractEav && !this.refs.contractEav.isValid())
        || (this.refs.password && !this.refs.password.validate())) {
      return;
    }
    const { location } = this.props;
    const { identityProjection } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    this.refs.form.processStarted();
    const data = this.refs.form.getData();
    if (this.refs.eav) {
      data._eav = this.refs.eav.getValues();
    }
    // construct projection
    const _identityProjection = {
      id: data.id,
      identity: data
    };
    //
    // prime contract
    if (identityProjection.contract && identityContractManager.canSave(identityProjection.contract)) {
      _identityProjection.contract = identityProjection.contract;
      _identityProjection.contract.validFrom = data.validFrom;
      _identityProjection.contract.validTill = data.validTill;
      _identityProjection.contract.workPosition = data.workPosition;
      //
      if (this.refs.contractEav) {
        _identityProjection.contract._eav = this.refs.contractEav.getValues();
      }
    }
    //
    // other contracts - leave them alone for now
    _identityProjection.otherContracts = identityProjection.otherContracts;
    //
    // other contract position
    if (contractPositionManager.canSave(identityProjection.otherPosition)) {
      _identityProjection.otherPositions = identityProjection.otherPositions;
      if (data.otherWorkPosition) {
        if (!_identityProjection.otherPositions) {
          _identityProjection.otherPositions = [];
        }
        if (_identityProjection.otherPositions.length === 0) {
          _identityProjection.otherPositions.push({});
        }
        _identityProjection.otherPositions[0].workPosition = data.otherWorkPosition;
      } else if (_identityProjection.otherPositions && _identityProjection.otherPositions.length > 0) {
        _identityProjection.otherPositions.splice(0, 1);
      }
    }
    //
    // assigned roles - directly assigned roles are used now => other roles are not sent to BE => remain untached
    if (isNew) {
      _identityProjection.identityRoles = [];
      if (data.role) {
        data.role.forEach(role => {
          _identityProjection.identityRoles.push({
            role,
            validFrom: data.validFrom
          });
        });
      }
    } else {
      // update is possible by rolerequest only
      _identityProjection.identityRoles = identityProjection.identityRoles;
    }
    //
    // post => save
    this.context.store.dispatch(identityProjectionManager.saveProjection(_identityProjection, null, this._afterSave.bind(this)));
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(identityProjection, error) {
    if (error) {
      this.setState({
        validationError: error,
        validationDefinition: false
      }, () => {
        this.addError(error);
        if (this.refs.form) {
          this.refs.form.processEnded();
        }
      });
    } else {
      this.addMessage({
        message: this.i18n('action.save.success', { record: identityProjection.identity.username, count: 1 })
      });
      this.context.history.replace(identityManager.getDetailLink(identityProjection.identity));
      if (this.refs.form) {
        this.refs.form.processEnded();
      }
      // reload role requests, if new
      if (this.refs.identityRolesTable) {
        this.refs.identityRolesTable._refreshAll();
      }
    }
  }

  render() {
    const {
      match,
      location,
      showLoading,
      _imageUrl,
      userContext
    } = this.props;
    const { entityId } = match.params;
    const {
      identityProjection,
      formProjection,
      generatePassword,
      generatePasswordShowLoading,
      passwordAgain,
      password,
      validationError,
      validationDefinition
    } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');

    let attributes = new Immutable.OrderedMap();
    if (formProjection && formProjection.formDefinitions) {
      try {
        const formDefinitions = JSON.parse(formProjection.formDefinitions);
        if (formDefinitions && formDefinitions.length > 0) {
          formDefinitions.forEach(formDefinition => {
            if (formDefinition.definition) {
              attributes = attributes.set(formDefinition.definition, new Immutable.OrderedSet());
              if (formDefinition.attributes) {
                formDefinition.attributes.forEach(attribute => {
                  attributes = attributes.set(formDefinition.definition, attributes.get(formDefinition.definition).add(attribute));
                });
              }
            }
          });
        }
      } catch (syntaxError) {
        // nothing
      }
    }
    let showContract = false;
    if (isNew) {
      showContract = identityContractManager.canSave(identityProjection ? identityProjection.contract : {});
    } else {
      showContract = identityContractManager.canRead(identityProjection ? identityProjection.contract : {});
    }
    //
    // TODO: form can be saved, if logged user can update identity, can be improved in future
    const readOnly = !identityProjectionManager.canSave(isNew ? null : identityProjection);
    //
    return (
      <Basic.Div>
        <Helmet title={ isNew ? this.i18n('create.title') : this.i18n('edit.title') } />
        <Basic.Row>
          <Basic.Div className="col-lg-offset-2 col-lg-8">
            <Advanced.DetailHeader
              entity={ isNew ? null : identityProjection }
              back="/identities"
              buttons={[
                <Basic.Icon
                  value="fa:angle-double-right"
                  style={{ marginRight: 5, cursor: 'pointer' }}
                  title={ this.i18n('component.advanced.IdentityInfo.link.detail.default.label') }
                  onClick={ () => this.context.history.push(`/identity/${ encodeURIComponent(identityProjection.username) }/profile`) }
                  rendered={ !isNew }/>
              ]}>
              {
                _imageUrl
                ?
                <img src={ _imageUrl } alt="profile" className="img-circle img-thumbnail" style={{ height: 40, padding: 0 }} />
                :
                <Basic.Icon
                  icon={ formProjection ? formProjectionManager.getLocalization(formProjection, 'icon', 'component:identity') : 'component:identity' }
                  identity={ identityProjection }/>
              }
              { ' ' }
              { identityProjectionManager.getNiceLabel(identityProjection) }
              <small>
                { ' ' }
                { isNew ? this.i18n('create.title') : this.i18n('edit.title') }
              </small>
            </Advanced.DetailHeader>

            <OrganizationPosition identity={ entityId } rendered={ !isNew }/>

            <form onSubmit={ this.save.bind(this) }>

              <Basic.Panel rendered={ identityProjection === null || identityProjection === undefined }>
                <Basic.Loading isStatic show/>
              </Basic.Panel>

              <Basic.Panel className={ identityProjection === null || identityProjection === undefined ? 'hidden' : 'last' }>

                <Basic.PanelBody>
                  <Basic.AbstractForm
                    ref="form"
                    data={ identityProjection }
                    readOnly={ readOnly }
                    style={{ padding: 0 }}>

                    <Basic.TextField
                      ref="username"
                      label={ this.i18n('identity.username.label') }
                      max={ 255 }
                      rendered={ this._isRendered(formProjection, 'username') }
                      readOnly={ readOnly }
                      required={ !isNew && this._isRendered(formProjection, 'username') }/>

                    <Basic.Row>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'lastName') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'firstName') }>
                        <Basic.TextField
                          ref="firstName"
                          label={ this.i18n('content.identity.profile.firstName') }
                          max={ 255 }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'firstName') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'lastName') }>
                        <Basic.TextField
                          ref="lastName"
                          label={ this.i18n('content.identity.profile.lastName') }
                          max={ 255 }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                    </Basic.Row>

                    <Basic.TextField
                      ref="externalCode"
                      label={ this.i18n('content.identity.profile.externalCode') }
                      rendered={ this._isRendered(formProjection, 'externalCode') }
                      max={ 255 }
                      readOnly={ readOnly }/>

                    <Basic.Row>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'titleAfter') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'titleBefore') }>
                        <Basic.TextField
                          ref="titleBefore"
                          label={ this.i18n('entity.Identity.titleBefore') }
                          max={ 100 }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'titleBefore') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'titleAfter') }>
                        <Basic.TextField
                          ref="titleAfter"
                          label={ this.i18n('entity.Identity.titleAfter') }
                          max={ 100 }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                    </Basic.Row>

                    <Basic.Row>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'phone') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'email') }>
                        <Basic.TextField
                          ref="email"
                          label={ this.i18n('content.identity.profile.email.label') }
                          placeholder={ this.i18n('content.identity.profile.email.placeholder') }
                          validation={ Joi.string().email() }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                      <Basic.Col
                        lg={ this._isRendered(formProjection, 'email') ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'phone') }>
                        <Basic.TextField
                          ref="phone"
                          label={ this.i18n('content.identity.profile.phone.label') }
                          placeholder={ this.i18n('content.identity.profile.phone.placeholder') }
                          max={ 30 }
                          readOnly={ readOnly }/>
                      </Basic.Col>
                    </Basic.Row>

                    <Basic.Div rendered={ showContract }>
                      <Basic.Row>
                        <Basic.Col lg={ 6 }>
                          <Basic.DateTimePicker
                            mode="date"
                            ref="validFrom"
                            label={ this.i18n('contract.validFrom.label') }
                            readOnly={ readOnly || !identityContractManager.canSave(identityProjection ? identityProjection.contract : {}) }/>
                        </Basic.Col>
                        <Basic.Col lg={ 6 }>
                          <Basic.DateTimePicker
                            mode="date"
                            ref="validTill"
                            label={ this.i18n('contract.validTill.label') }
                            readOnly={ readOnly || !identityContractManager.canSave(identityProjection ? identityProjection.contract : {}) }/>
                        </Basic.Col>
                      </Basic.Row>

                      <Advanced.TreeNodeSelect
                        ref="workPosition"
                        label={ this.i18n('contract.workPosition.label') }
                        header={ this.i18n('contract.workPosition.label') }
                        treeNodeLabel={ this.i18n('contract.workPosition.label') }
                        useFirstType
                        readOnly={ readOnly || !identityContractManager.canSave(identityProjection ? identityProjection.contract : {}) }/>
                    </Basic.Div>

                    <Basic.Div rendered={
                      isNew
                      ?
                      (
                        SecurityManager.hasAuthority('CONTRACTPOSITION_CREATE', userContext)
                        &&
                        showContract
                      )
                      :
                      SecurityManager.hasAuthority('CONTRACTPOSITION_READ', userContext)
                    }>
                      <Advanced.TreeNodeSelect
                        ref="otherWorkPosition"
                        label={ this.i18n('otherPosition.workPosition.label') }
                        header={ this.i18n('otherPosition.workPosition.label') }
                        treeNodeLabel={ this.i18n('otherPosition.workPosition.label') }
                        useFirstType
                        readOnly={
                          readOnly
                          ||
                          !contractPositionManager.canSave(identityProjection ? identityProjection.otherPosition : {})
                          ||
                          (identityProjection && !identityProjection.contract && !identityContractManager.canSave({}))
                        }/>
                    </Basic.Div>
                    {
                      !identityProjection
                      ||
                      <Advanced.EavContent
                        ref="eav"
                        formableManager={ identityManager }
                        contentKey="content.identity.eav"
                        showSaveButton
                        showAttributesOnly
                        readOnly={ readOnly }
                        showDefinitions={ attributes }
                        entityId={ isNew ? null : entityId }
                        formInstances={ isNew ? null : identityProjection._eav } />
                    }

                    {
                      !identityProjection || !identityProjection.contract || !showContract
                      ||
                      <Advanced.EavContent
                        ref="contractEav"
                        formableManager={ identityContractManager }
                        contentKey="content.identity-contract.eav"
                        showSaveButton
                        showAttributesOnly
                        readOnly={ readOnly }
                        showDefinitions={ attributes }
                        entityId={ isNew ? null : identityProjection.contract.id }
                        formInstances={ isNew ? null : identityProjection.contractEav } />
                    }

                    <Basic.TextArea
                      ref="description"
                      label={ this.i18n('content.identity.profile.description.label') }
                      placeholder={ this.i18n('content.identity.profile.description.placeholder') }
                      rows={ 4 }
                      max={ 1000 }
                      rendered={ this._isRendered(formProjection, 'description') }
                      readOnly={ readOnly }/>

                    <Basic.EnumSelectBox
                      ref="state"
                      enum={ IdentityStateEnum }
                      useSymbol={ false }
                      label={ this.i18n('entity.Identity.state.label') }
                      helpBlock={ <span>{ this.i18n('entity.Identity.state.help') }</span> }
                      readOnly
                      rendered={ !isNew && this._isRendered(formProjection, 'state') }/>

                    <Basic.Checkbox
                      ref="disabled"
                      label={ this.i18n('entity.Identity.disabledReadonly.label') }
                      helpBlock={ this.i18n('entity.Identity.disabledReadonly.help') }
                      readOnly
                      rendered={ !isNew && this._isRendered(formProjection, 'disabled') } />

                    <Basic.Row>
                      <Basic.Col
                        lg={ isNew && SecurityManager.hasAllAuthorities(['IDENTITYROLE_READ', 'IDENTITY_CHANGEPERMISSION'], userContext) ? 6 : 12 }
                        rendered={ this._isRendered(formProjection, 'password') }>
                        <Basic.ContentHeader
                          icon="component:password"
                          text={ this.i18n('component.advanced.PasswordField.password.label') }
                          rendered={ isNew }/>

                        <Basic.Div rendered={ isNew }>
                          <Basic.Div
                            className="abstract-form"
                            style={{ paddingTop: 0, paddingBottom: 0 }}>
                            <Basic.Checkbox
                              ref="generatePassword"
                              value={ generatePassword }
                              label={ this.i18n('content.identity.create.button.generate') }
                              onChange={ this.generatePassword.bind(this) }/>

                            <Advanced.PasswordField
                              className="form-control"
                              ref="password"
                              type={ generatePassword || generatePasswordShowLoading ? 'text' : 'password' }
                              required={ !generatePassword }
                              readOnly={ generatePassword }
                              newPassword={ password }
                              newPasswordAgain={ passwordAgain }/>
                          </Basic.Div>
                          <Advanced.ValidationMessage error={ validationError } validationDefinition={ validationDefinition }/>
                        </Basic.Div>

                        <Basic.Div rendered={ !isNew && this._canPasswordChange(identityProjection) }>
                          <Basic.LabelWrapper label={ isNew || this.i18n('component.advanced.PasswordField.password.label') }>

                            <Basic.Button
                              level="link"
                              icon="component:password"
                              onClick={ (e) => {
                                if (e) {
                                  e.preventDefault();
                                }
                                this.context.history.push(`/identity/${ encodeURIComponent(identityProjection.username) }/password/change`);
                              }}
                              className="embedded"
                              title={ this.i18n('content.password.change.message.passwordChange.link') }>
                              { this.i18n('content.password.change.button.passwordChange') }
                            </Basic.Button>
                          </Basic.LabelWrapper>
                        </Basic.Div>
                      </Basic.Col>

                      <Basic.Col
                        lg={ isNew && this._isRendered(formProjection, 'password') ? 6 : 12 }
                        rendered={ SecurityManager.hasAllAuthorities(['IDENTITYROLE_READ', 'IDENTITY_CHANGEPERMISSION'], userContext) }>
                        <Basic.ContentHeader icon="component:identity-roles" text={ this.i18n('roles.header.new') } rendered={ isNew }/>
                        <Basic.ContentHeader
                          icon="component:roles"
                          text={ this.i18n('roles.header.edit') }
                          rendered={ !isNew }
                          style={{ marginBottom: 0 }}/>
                        {
                          isNew
                          ?
                          <Advanced.RoleSelect
                            ref="role"
                            forceSearchParameters={ new Domain.SearchParameters().setName('can-be-requested') }
                            label={ null }
                            placeholder={ this.i18n('entity.Role._type') }
                            header={ this.i18n('roles.header') }
                            multiSelect/>
                          :
                          <Basic.Div>
                            <IdentityRoles
                              ref="identityRolesTable"
                              identity={ identityProjection }
                              match={ this.props.match }
                              embedded
                              columns={[
                                'role',
                                'identityContract',
                                'validFrom',
                                'validTill',
                                'automaticRole'
                              ]}/>
                          </Basic.Div>
                        }
                      </Basic.Col>
                    </Basic.Row>
                  </Basic.AbstractForm>
                </Basic.PanelBody>
                <Basic.PanelFooter>
                  <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                    { this.i18n('button.back') }
                  </Basic.Button>
                  <Basic.Button
                    type="submit"
                    level="success"
                    showLoading={ showLoading }
                    showLoadingIcon
                    showLoadingText={ this.i18n('button.saving') }
                    rendered={ !readOnly }>
                    { this.i18n('button.save') }
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </Basic.Div>
        </Basic.Row>
      </Basic.Div>
    );
  }
}

IdentityProjection.propTypes = {
  identityProjection: PropTypes.object,
  userContext: PropTypes.object
};
IdentityProjection.defaultProps = {
  identityProjection: null,
  userContext: null,
  _imageUrl: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = DataManager.getData(state, profileUiKey);
  const identityProjection = identityProjectionManager.getEntity(state, entityId);
  //
  return {
    identityProjection,
    userContext: state.security.userContext,
    showLoading: identityProjectionManager.isShowLoading(state, null, !identityProjection ? entityId : identityProjection.id),
    _imageUrl: profile ? profile.imageUrl : null,
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange')
  };
}

export default connect(select)(IdentityProjection);
