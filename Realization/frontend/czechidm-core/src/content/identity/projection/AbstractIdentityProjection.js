import React from 'react';
import PropTypes from 'prop-types';
import Joi from 'joi';
import moment from 'moment';
import Immutable from 'immutable';
import _ from 'lodash';
import Helmet from 'react-helmet';
import uuid from 'uuid';
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
  SecurityManager,
  CodeListManager,
  CodeListItemManager
} from '../../../redux';
import IdentityStateEnum from '../../../enums/IdentityStateEnum';
import OrganizationPosition from '../OrganizationPosition';
import IdentityRoles from '../IdentityRoles';
import DisableIdentityDashboardButton from '../../dashboards/button/DisableIdentityDashboardButton';
import EnableIdentityDashboardButton from '../../dashboards/button/EnableIdentityDashboardButton';

const PASSWORD_PREVALIDATION = 'PASSWORD_PREVALIDATION';

const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();
const contractPositionManager = new ContractPositionManager();
const identityProjectionManager = new IdentityProjectionManager();
const formProjectionManager = new FormProjectionManager();
const codeListManager = new CodeListManager();
const codeListItemManager = new CodeListItemManager();

/**
 * Univarzal form for identity projection - generalizable super class.
 * Add redux layer in your projection.
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
export default class AbstractIdentityProjection extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      generatePassword: false,
      generatePasswordShowLoading: false,
      activeKey: null,
      editContracts: new Immutable.OrderedSet()
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
          } else if (this._isTrue(entity, 'all-contracts')) {
            // we need to read codelist items
            const searchParameters = new Domain.SearchParameters()
              .setFilter('codeListId', 'contract-position')
              .setSort('name')
              .setSize(10000);
            this.context.store.dispatch(
              codeListItemManager.fetchEntities(searchParameters, `identity-projection-codelist-contract-position`, (items, itemError) => {
                if (itemError && (itemError.statusCode === 400 || itemError.statusCode === 403)) {
                  this._initProjection(entityId, identityProjection, entity);
                } else if (itemError) {
                  this.addError(itemError);
                } else {
                  this._initProjection(entityId, identityProjection, entity, null, items._embedded[codeListItemManager.getCollectionType()] || []);
                }
              })
            );
          } else {
            this._initProjection(entityId, identityProjection, entity);
          }
        }));
      }
    } else {
      this._fetchIdentityProjection(entityId);
    }
    // autocomplete codelist items
    this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded('contract-position'));
  }

  _fetchIdentityProjection(entityId, focusUsername = true) {
    this.getLogger().debug(`[FormDetail] loading entity detail [id:${ entityId }]`);
    this.context.store.dispatch(identityProjectionManager.fetchProjection(entityId, null, (entity, error) => {
      if (error) {
        this.addError(error);
      } else {
        this._initProjection(entityId, entity, null, focusUsername);
      }
    }));
  }

  _initProjection(entityId, identityProjection = null, formProjection = null, focusUsername = true, contractPositions = null) {
    const { location } = this.props;
    const { generatePassword } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    const _contractPositions = contractPositions || this.props.contractPositions;
    let activeKey = null;
    //
    // prepare form friendly projection
    let _identityProjection = null;
    if (identityProjection) {
      if (!formProjection && identityProjection.identity && identityProjection.identity._embedded) {
        formProjection = identityProjection.identity._embedded.formProjection;
      }
      // basic attributes
      _identityProjection = {
        ...identityProjection.identity,
        identity: identityProjection.identity,
        email: identityProjection.identity.email || ''
      };
      // transform all contract into array
      _identityProjection.allContracts = [];
      if (identityProjection.contract) {
        _identityProjection.allContracts[0] = identityProjection.contract;
        if (identityContractManager.canSave(_identityProjection.allContracts[0])) {
          activeKey = 0;
        }
      }
      if (identityProjection.otherContracts) {
        identityProjection.otherContracts.forEach((contract, index) => {
          _identityProjection.allContracts.push(contract); // index >= 1
          if (activeKey === null && identityContractManager.canSave(contract)) {
            activeKey = index + 1;
          }
        });
      }
      // transform other position to map by contract index (index - support new contract too - prevent to use uuid).
      _identityProjection.otherPositions = new Immutable.Map({});
      if (identityProjection.otherPositions) {
        identityProjection.otherPositions.forEach(position => {
          // find contract index in array by uuid
          const index = _identityProjection.allContracts.findIndex(contract => contract.id === position.identityContract);
          if (!_identityProjection.otherPositions.has(index)) {
            _identityProjection.otherPositions = _identityProjection.otherPositions.set(index, []);
          }
          const positions = _identityProjection.otherPositions.get(index);
          positions.push(position);
          _identityProjection.otherPositions = _identityProjection.otherPositions.set(index, positions);
        });
      }
    } else {
      // new projection
      _identityProjection = {
        id: entityId,
        email: '', // TODO: fix email validator ... doesn't work with null
        identity: {
          id: entityId,
          formProjection: formProjection ? formProjection.id : Utils.Ui.getUrlParameter(this.props.location, 'projection')
        },
        allContracts: []
      };
    }
    //
    if (isNew) {
      // prepare new prime contract
      if (!_identityProjection['validFrom-0']) {
        _identityProjection['validFrom-0'] = moment();
      }
      // prepare contracts
      if (this._isTrue(formProjection, 'all-contracts')) {
        for (let i = 0; i < _contractPositions.length; i++) {
          _identityProjection.allContracts[i] = {
            position: _contractPositions[i].code,
            validFrom: moment()
          };
        }
      } else {
        _identityProjection.allContracts[0] = {
          position: 'Default',
          validFrom: moment()
        };
      }
    }
    // transform contract properties to projection root
    for (let i = 0; i < _identityProjection.allContracts.length; i++) {
      const contract = _identityProjection.allContracts[i];
      _identityProjection[`validFrom-${ i }`] = contract.validFrom;
      _identityProjection[`validTill-${ i }`] = contract.validTill;
      _identityProjection[`workPosition-${ i }`] = contract.workPosition;
      // transform contract other position to projection root
      if (_identityProjection.otherPositions && _identityProjection.otherPositions.has(i)) {
        // single other position is supported now only
        _identityProjection[`otherWorkPosition-${ i }`] = _identityProjection.otherPositions.get(i)[0].workPosition;
      }
    }

    //
    this.setState({
      identityProjection: _identityProjection,
      formProjection,
      attributes: this.getAttributes(formProjection),
      isNew,
      activeKey: activeKey || 0
    }, () => {
      if (this.refs.username && focusUsername) {
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

  _hasPermission(identityProjection, permission) {
    if (!identityProjection) {
      return false;
    }
    return Utils.Permission.hasPermission(identityProjection._permissions, permission);
  }

  _isTrue(formProjection, propertyName) {
    if (!formProjection) {
      return null;
    }
    const formInstance = new Domain.FormInstance({}).setProperties(formProjection.properties);
    const formValue = formInstance.getSingleValue(propertyName);
    //
    return formValue ? !!formValue.value : false;
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()
        || (this.refs.eav && !this.refs.eav.isValid())
        || (this.refs.password && !this.refs.password.validate())) {
      return;
    }
    const { identityProjection, isNew, formProjection, editContracts } = this.state;
    // set contracts data
    for (let i = 0; i < identityProjection.allContracts.length; i++) {
      if (this.refs[`contractEav-${ i }`] && !this.refs[`contractEav-${ i }`].isValid()) {
        return;
      }
    }
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
    // contracts
    _identityProjection.contract = null;
    _identityProjection.otherContracts = [];
    _identityProjection.otherPositions = [];
    for (let i = 0; i < identityProjection.allContracts.length; i++) {
      if (isNew && this._isTrue(formProjection, 'all-contracts') && !editContracts.has(i)) {
        // Contract is not edited => not saved.
        continue;
      }
      const contract = _.clone(identityProjection.allContracts[i]); // prevent to fill id into form => permissions will be evaluated as UPDATE
      if (identityContractManager.canSave(contract)) {
        // generate contract id on FE => roles and other positions can be added to concrete contract
        if (!contract.id) {
          contract.id = uuid.v1();
        }
        contract.validFrom = data[`validFrom-${ i }`];
        contract.validTill = data[`validTill-${ i }`];
        contract.workPosition = data[`workPosition-${ i }`];
        if (this.refs[`contractEav-${ i }`]) {
          contract._eav = this.refs[`contractEav-${ i }`].getValues();
        }
        if (_identityProjection.contract === null) {
          // prime contract
          contract.main = true;
          _identityProjection.contract = contract;
        } else {
          // other contract
          _identityProjection.otherContracts.push(contract);
        }
      }
      //
      // other contract positions
      let otherPositions = [];
      if (identityProjection.otherPositions && identityProjection.otherPositions.has(i)) {
        otherPositions = identityProjection.otherPositions.get(i);
      } else {
        otherPositions.push({}); // new other position
      }
      let preserveOtherPositionOnIndex = 0;
      //
      if (this.refs[`otherWorkPosition-${ i }`]) {
        preserveOtherPositionOnIndex = 1;
        const otherPosition = otherPositions[0];
        if (contractPositionManager.canSave(otherPosition) && identityContractManager.canSave(contract)) {
          otherPosition.workPosition = data[`otherWorkPosition-${ i }`];
          if (otherPosition.workPosition) {
            if (!otherPosition.identityContract) {
              otherPosition.identityContract = contract.id;
            }
            _identityProjection.otherPositions.push(otherPosition);
          } else {
            // not added into seved projection => empty value will be deleted.
          }
        }
      }
      // prevent to delete other position, not eddited by this projection currently
      otherPositions.forEach((additionalOtherPosition, index) => {
        if (index >= preserveOtherPositionOnIndex && contractPositionManager.canSave(additionalOtherPosition)) {
          _identityProjection.otherPositions.push(additionalOtherPosition);
        }
      });
      //
      // assigned roles - directly assigned roles are used now => other roles are not sent to BE => remain untached
      if (isNew) {
        if (data[`role-${ i }`]) {
          data[`role-${ i }`].forEach(role => {
            if (!_identityProjection.identityRoles) {
              _identityProjection.identityRoles = [];
            }
            _identityProjection.identityRoles.push({
              role,
              identityContract: contract.id,
              validFrom: contract.validFrom
            });
          });
        }
      }
    }
    //
    if (!isNew) {
      // update is possible by rolerequest only
      _identityProjection.identityRoles = identityProjection.identityRoles;
    } else if (!_identityProjection.identity.formProjection) {
      _identityProjection.identity.formProjection = formProjection ? formProjection.id : Utils.Ui.getUrlParameter(this.props.location, 'projection');
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

  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  _onEdit(index, event) {
    if (event) {
      event.preventDefault();
    }
    const { editContracts } = this.state;
    this.setState({
      editContracts: editContracts.add(index)
    });
  }

  getAttributes(formProjection) {
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
    //
    return attributes;
  }

  /**
   * Render top header.
   */
  renderHeader() {
    const { match, _imageUrl, location } = this.props;
    const { entityId } = match.params;
    const { identityProjection, formProjection } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    return (
      <Basic.Div>
        <Helmet title={ isNew ? this.i18n('create.title') : this.i18n('edit.title') } />
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

      </Basic.Div>
    );
  }

  /**
   * Render identity basic and eav attributes.
   */
  renderIdentity() {
    const { formProjection, identityProjection, isNew } = this.state;
    const readOnly = !identityProjectionManager.canSave(isNew ? null : identityProjection);
    //
    return (
      <Basic.Div>
        <Basic.TextField
          ref="username"
          label={ this.i18n('identity.username.label') }
          max={ 255 }
          rendered={ this._isRendered(formProjection, 'username') }
          readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGEUSERNAME')) }
          required={ !isNew && this._isRendered(formProjection, 'username') }/>

        <Basic.Row>
          <Basic.Col
            lg={ this._isRendered(formProjection, 'lastName') ? 6 : 12 }
            rendered={ this._isRendered(formProjection, 'firstName') }>
            <Basic.TextField
              ref="firstName"
              label={ this.i18n('content.identity.profile.firstName') }
              max={ 255 }
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGENAME')) }/>
          </Basic.Col>
          <Basic.Col
            lg={ this._isRendered(formProjection, 'firstName') ? 6 : 12 }
            rendered={ this._isRendered(formProjection, 'lastName') }>
            <Basic.TextField
              ref="lastName"
              label={ this.i18n('content.identity.profile.lastName') }
              max={ 255 }
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGENAME')) }/>
          </Basic.Col>
        </Basic.Row>

        <Basic.TextField
          ref="externalCode"
          label={ this.i18n('content.identity.profile.externalCode') }
          rendered={ this._isRendered(formProjection, 'externalCode') }
          max={ 255 }
          readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGEEXTERNALCODE')) }/>

        <Basic.Row>
          <Basic.Col
            lg={ this._isRendered(formProjection, 'titleAfter') ? 6 : 12 }
            rendered={ this._isRendered(formProjection, 'titleBefore') }>
            <Basic.TextField
              ref="titleBefore"
              label={ this.i18n('entity.Identity.titleBefore') }
              max={ 100 }
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGENAME')) }/>
          </Basic.Col>
          <Basic.Col
            lg={ this._isRendered(formProjection, 'titleBefore') ? 6 : 12 }
            rendered={ this._isRendered(formProjection, 'titleAfter') }>
            <Basic.TextField
              ref="titleAfter"
              label={ this.i18n('entity.Identity.titleAfter') }
              max={ 100 }
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGENAME')) }/>
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
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGEEMAIL')) }/>
          </Basic.Col>
          <Basic.Col
            lg={ this._isRendered(formProjection, 'email') ? 6 : 12 }
            rendered={ this._isRendered(formProjection, 'phone') }>
            <Basic.TextField
              ref="phone"
              label={ this.i18n('content.identity.profile.phone.label') }
              placeholder={ this.i18n('content.identity.profile.phone.placeholder') }
              max={ 30 }
              readOnly={ readOnly || (!isNew && !this._hasPermission(identityProjection, 'CHANGEPHONE')) }/>
          </Basic.Col>
        </Basic.Row>

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

        { this.renderIdentityAttributes(identityProjection, isNew, readOnly) }

      </Basic.Div>
    );
  }

  /**
   * Render identity eav attributes.
   */
  renderIdentityAttributes() {
    const { attributes, identityProjection, isNew } = this.state;
    const { entityId } = this.props.match.params;
    //
    return (
      <Advanced.EavContent
        ref="eav"
        formableManager={ identityManager }
        contentKey="content.identity.eav"
        showSaveButton
        showAttributesOnly
        showDefinitions={ attributes }
        entityId={ isNew ? null : entityId }
        formInstances={ isNew ? null : identityProjection._eav } />
    );
  }

  /**
   * Render prime (~first) contract.
   */
  renderPrimeContract() {
    const { formProjection, identityProjection, isNew } = this.state;
    const primeContract = identityProjection.allContracts[0];
    if (!primeContract) {
      // prime contract is not present (e.g. identity without contract)
      return null;
    }
    //
    let showContract = false;
    if (isNew) {
      showContract = identityContractManager.canSave(primeContract);
    } else {
      showContract = identityContractManager.canRead(primeContract);
    }
    //
    if (!showContract || !this._isTrue(formProjection, 'prime-contract') || this._isTrue(formProjection, 'all-contracts')) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader icon="component:prime-contract" text={ this.i18n('primeContract.label') }/>

        { this.renderContract(0) }
      </Basic.Div>
    );
  }

  /**
   * Render contract by index.
   */
  renderContract(index) {
    const { location } = this.props;
    const { identityProjection } = this.state;
    const contract = identityProjection.allContracts.length > index ? identityProjection.allContracts[index] : {};
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    const readOnly = !identityContractManager.canSave(contract);
    //
    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Col lg={ 6 }>
            <Basic.DateTimePicker
              mode="date"
              ref={ `validFrom-${ index }` }
              label={ this.i18n('contract.validFrom.label') }
              readOnly={ readOnly }/>
          </Basic.Col>
          <Basic.Col lg={ 6 }>
            <Basic.DateTimePicker
              mode="date"
              ref={ `validTill-${ index }` }
              label={ this.i18n('contract.validTill.label') }
              readOnly={ readOnly }/>
          </Basic.Col>
        </Basic.Row>

        <Advanced.TreeNodeSelect
          ref={ `workPosition-${ index }` }
          label={ this.i18n('contract.workPosition.label') }
          header={ this.i18n('contract.workPosition.label') }
          treeNodeLabel={ this.i18n('contract.workPosition.label') }
          useFirstType
          readOnly={ readOnly }/>

        { this.renderOtherPosition(index) }

        { this.renderContractAttributes(index) }

        {
          !isNew
          ||
          this.renderAssignedRoles(index)
        }
      </Basic.Div>
    );
  }

  /**
   * Render contract eav attributes by index.
   */
  renderContractAttributes(index) {
    const { attributes, identityProjection, isNew } = this.state;
    const contract = identityProjection.allContracts[index];
    if (!contract) {
      return null;
    }
    //
    return (
      <Advanced.EavContent
        ref={ `contractEav-${ index }` }
        formableManager={ identityContractManager }
        contentKey="content.identity-contract.eav"
        showSaveButton
        showAttributesOnly
        showDefinitions={ attributes }
        entityId={ isNew ? null : contract.id }
        formInstances={ isNew ? null : contract._eav } />
    );
  }

  /**
   * Render one other contrantact position for contract by index..
   */
  renderOtherPosition(index) {
    const { userContext } = this.props;
    const { identityProjection, formProjection, isNew } = this.state;
    const contract = identityProjection.allContracts.length > index ? identityProjection.allContracts[index] : {};
    const otherPosition = identityProjection.otherPositions && identityProjection.otherPositions.has(index)
    ? identityProjection.otherPositions.get(index)[0]
    : {};
    //
    if (!this._isTrue(formProjection, 'other-position')) {
      return null;
    }
    //
    return (
      <Basic.Div rendered={
        isNew
        ?
        SecurityManager.hasAuthority('CONTRACTPOSITION_CREATE', userContext)
        :
        SecurityManager.hasAuthority('CONTRACTPOSITION_READ', userContext)
      }>
        <Advanced.TreeNodeSelect
          ref={ `otherWorkPosition-${ index }` }
          label={ this.i18n('otherPosition.workPosition.label') }
          header={ this.i18n('otherPosition.workPosition.label') }
          treeNodeLabel={ this.i18n('otherPosition.workPosition.label') }
          useFirstType
          readOnly={ !contractPositionManager.canSave(otherPosition) || !identityContractManager.canSave(contract) }/>
      </Basic.Div>
    );
  }

  /**
   * Render all contracts.
   */
  renderAllContracts() {
    const { formProjection, activeKey, identityProjection, isNew, editContracts } = this.state;
    //
    // not enabled
    if (!this._isTrue(formProjection, 'all-contracts') || identityProjection.allContracts.length === 0) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader
          icon="component:contracts"
          text={ this.i18n('allContracts.label') }/>
        <Basic.Tabs activeKey={ activeKey } onSelect={ this._onChangeSelectTabs.bind(this) }>
          {
            identityProjection
              .allContracts
              .map((contract, index) => {
                return (
                  <Basic.Tab
                    eventKey={ index }
                    title={ <Advanced.CodeListValue value={ contract.position || 'Default' } code="contract-position"/> }
                    className="bordered">
                    <Basic.Div style={{ padding: 15 }}>
                      <Basic.Div
                        rendered={ isNew && !editContracts.has(index) }
                        style={{ display: 'flex', height: 200, justifyContent: 'center', alignItems: 'center' }}>
                        <Basic.Button
                          buttonSize="lg"
                          level="primary"
                          icon="fa:plus"
                          onClick={ this._onEdit.bind(this, index) }>
                          { this.i18n('button.editContract.label') }
                        </Basic.Button>
                      </Basic.Div>
                      <Basic.Div style={ isNew && !editContracts.has(index) ? { display: 'none' } : null }>
                        { this.renderContract(index) }
                      </Basic.Div>
                    </Basic.Div>
                  </Basic.Tab>
                );
              })
          }
        </Basic.Tabs>
      </Basic.Div>
    );
  }

  /**
   * Render password.
   */
  renderPassword() {
    const {
      identityProjection,
      isNew,
      formProjection,
      generatePassword,
      generatePasswordShowLoading,
      passwordAgain,
      password,
      validationError,
      validationDefinition
    } = this.state;
    //
    if (!this._isRendered(formProjection, 'password')) {
      return null;
    }
    //
    return (
      <Basic.Div>
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
      </Basic.Div>
    );
  }

  /**
   * Render assigned roles for contract by index (if is new) or show assigned roles + requests.
   */
  renderAssignedRoles(index) {
    const { userContext } = this.props;
    const { identityProjection, formProjection, isNew } = this.state;
    //
    if (!SecurityManager.hasAllAuthorities(['IDENTITYROLE_READ', 'IDENTITYCONTRACT_CHANGEPERMISSION'], userContext)) {
      return null;
    }
    if (!this._isTrue(formProjection, 'assigned-roles')) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader
          icon="component:roles"
          text={ this.i18n('roles.header.edit') }
          rendered={ !isNew }
          style={{ marginBottom: 0 }}/>
        {
          isNew
          ?
          <Advanced.RoleSelect
            ref={ `role-${ index }` }
            forceSearchParameters={ new Domain.SearchParameters().setName('can-be-requested') }
            label={ this.i18n('roles.header.new') }
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
      </Basic.Div>
    );
  }

  render() {
    const { match, showLoading, userContext } = this.props;
    const { entityId } = match.params;
    const { identityProjection, formProjection, isNew } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Div className="col-lg-offset-2 col-lg-8">

            { this.renderHeader() }

            <form onSubmit={ this.save.bind(this) }>
              {
                !identityProjection
                ?
                <Basic.Panel rendered={ identityProjection === null || identityProjection === undefined }>
                  <Basic.Loading isStatic show/>
                </Basic.Panel>
                :
                <Basic.Panel className="last">
                  <Basic.PanelBody>
                    <Basic.AbstractForm
                      ref="form"
                      data={ identityProjection }
                      style={{ padding: 0 }}>

                      { this.renderIdentity() }

                      { this.renderPassword() }

                      { this.renderPrimeContract() }

                      { this.renderAllContracts() }

                      {
                        isNew
                        ||
                        this.renderAssignedRoles()
                      }

                    </Basic.AbstractForm>
                  </Basic.PanelBody>
                  <Basic.PanelFooter>
                    <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                      { this.i18n('button.back') }
                    </Basic.Button>
                    {
                      !formProjection
                      ||
                      <Basic.Button
                        type="button"
                        level="link"
                        rendered={ false && SecurityManager.hasAllAuthorities(['FORMPROJECTION_UPDATE'], userContext) && this.isDevelopment() }
                        onClick={ () => this.context.history.push(`/form-projections/${ formProjection.id }/detail`) }>
                        { this.i18n('button.formProjection.label') }
                      </Basic.Button>
                    }
                    <Basic.Div style={{ display: 'inline' }} rendered={ !isNew }>
                      <DisableIdentityDashboardButton
                        entityId={ identityProjection.username }
                        identity={ identityProjection }
                        permissions={ identityProjection._permissions }
                        buttonSize="default"
                        onComplete={ () => this._fetchIdentityProjection(entityId, false) }
                        showLoading={ showLoading }/>
                      <EnableIdentityDashboardButton
                        entityId={ identityProjection.username }
                        identity={ identityProjection }
                        permissions={ identityProjection._permissions }
                        buttonSize="default"
                        onComplete={ () => this._fetchIdentityProjection(entityId, false) }
                        showLoading={ showLoading }/>
                    </Basic.Div>
                    <Basic.Button
                      type="submit"
                      level="success"
                      showLoading={ showLoading }
                      showLoadingIcon
                      showLoadingText={ this.i18n('button.saving') }
                      rendered={ identityProjectionManager.canSave(isNew ? null : identityProjection) }>
                      { this.i18n('button.save') }
                    </Basic.Button>
                  </Basic.PanelFooter>
                </Basic.Panel>
              }
            </form>
          </Basic.Div>
        </Basic.Row>
      </Basic.Div>
    );
  }
}

AbstractIdentityProjection.propTypes = {
  identityProjection: PropTypes.object,
  userContext: PropTypes.object
};
AbstractIdentityProjection.defaultProps = {
  identityProjection: null,
  userContext: null,
  _imageUrl: null
};
