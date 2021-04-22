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
 * Univerzal form for identity projection - generalizable super class.
 * Add redux layer in your projection.
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
export default class AbstractIdentityProjection extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      identityProjection: null,
      generatePassword: false,
      generatePasswordShowLoading: false,
      activeKey: null,
      editContracts: new Immutable.OrderedSet(),
      validationErrors: null
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
          } else if (this.isTrue(entity, 'all-contracts')) {
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
    const _contractPositions = contractPositions || [];
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
      // reset prefilled username, if username is not rendered => will be generated on backend
      if (!this.isRendered(formProjection, 'username')) {
        _identityProjection.username = null;
      }
      // prepare new prime contract
      if (!_identityProjection['validFrom-0']) {
        _identityProjection['validFrom-0'] = moment();
      }
      //
      let validTill = null;
      if (this.isRequired(formProjection, 'IdmIdentityContract.validTill')) {
        const maxDate = this.getMaxDate(formProjection, 'IdmIdentityContract.validTill');
        if (maxDate) {
          validTill = maxDate;
        }
      }
      if (!_identityProjection['validTill-0']) {
        _identityProjection['validTill-0'] = validTill;
      }
      // prepare contracts
      if (this.isTrue(formProjection, 'all-contracts')) {
        for (let i = 0; i < _contractPositions.length; i++) {
          _identityProjection.allContracts[i] = {
            position: _contractPositions[i].code,
            validFrom: moment(),
            validTill
          };
        }
      } else {
        _identityProjection.allContracts[0] = {
          position: 'Default',
          validFrom: moment(),
          validTill
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
    let editContracts = new Immutable.OrderedSet();
    if (_identityProjection.allContracts.length > 0) {
      editContracts = editContracts.add(0);
    }
    //
    this.setState({
      identityProjection: _identityProjection,
      formProjection,
      attributes: this.getAttributes(formProjection),
      isNew,
      activeKey: activeKey || 0,
      editContracts
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

  /**
   * Configured Basic field form vaidation validation.
   *
   * @since 11.0.0
   */
  getBasicFieldValidation(formProjection, basicField) {
    if (!formProjection || !formProjection.formValidations) {
      return true;
    }
    //
    try {
      return JSON
        .parse(formProjection.formValidations)
        .find(formValidation => {
          return formValidation.basicField === basicField;
        });
    } catch (syntaxError) {
      return null;
    }
  }

  /**
   * Basic field is rendered.
   * Render identity or contract basic attributes.
   *
   * @since 10.3.0
   */
  isRendered(formProjection, basicField) {
    if (!formProjection || !formProjection.basicFields) {
      return true;
    }
    //
    try {
      const basicFields = JSON
        .parse(formProjection.basicFields)
        .filter(f => {
          if (basicField.indexOf('IdmIdentityContract.') > -1) {
            return f.indexOf('IdmIdentityContract.') > -1;
          }
          return f.indexOf('IdmIdentityContract.') < 0;
        })
        .map(f => {
          if (_.isObject(f)) {
            return !f.code || f.code.toLowerCase();
          }
          return !f || f.toLowerCase();
        });
      if (!basicFields || basicFields.length === 0) {
        return true;
      }
      return _.includes(basicFields, basicField.toLowerCase());
    } catch (syntaxError) {
      // nothing
    }
    //
    return true; // rendered by default
  }

  /**
   * Basic field is readOnly.
   *
   * @since 11.0.0
   */
  isReadOnly(formProjection, basicField, readOnly = false) {
    const formValidation = this.getBasicFieldValidation(formProjection, basicField);
    if (!formValidation) {
      return readOnly;
    }
    return readOnly || formValidation.readonly;
  }

  /**
   * Basic field is required.
   *
   * @since 10.3.0
   */
  isRequired(formProjection, basicField, readOnly = false) {
    if (!this.isRendered(formProjection, basicField)) {
      return false;
    }
    if (this.isReadOnly(formProjection, basicField, readOnly)) {
      return false;
    }
    //
    const { isNew } = this.state;
    const formValidation = this.getBasicFieldValidation(formProjection, basicField);
    //
    if (basicField === 'username') {
      return !isNew || (formValidation && formValidation.required);
    }
    if (!formValidation) { // not required by default
      return false;
    }
    //
    return formValidation.required;
  }

  /**
   * Basic field validation message.
   *
   * @since 11.0.0
   */
  getValidationMessage(formProjection, basicField) {
    const formValidation = this.getBasicFieldValidation(formProjection, basicField);
    if (!formValidation) {
      return null;
    }
    return formValidation.validationMessage;
  }

  hasPermission(identityProjection, permission) {
    if (!identityProjection) {
      return false;
    }
    return Utils.Permission.hasPermission(identityProjection._permissions, permission);
  }

  /**
   * Get configured form projection boolean property value.
   *
   * @param  {IdmFormProjection} formProjection
   * @param  {string} propertyName
   * @return {bool}
   */
  isTrue(formProjection, propertyName) {
    const value = this.getPropertyValue(formProjection, propertyName);
    //
    return value ? !!value : false;
  }

  /**
   * Get configured form projection property value.
   *
   * @param  {IdmFormProjection} formProjection
   * @param  {string} propertyName
   * @return {string}
   * @since 11.0.0
   */
  getPropertyValue(formProjection, propertyName) {
    if (!formProjection) {
      return null;
    }
    const formInstance = new Domain.FormInstance({}).setProperties(formProjection.properties);
    const formValue = formInstance.getSingleValue(propertyName);
    //
    return formValue ? formValue.value : null;
  }

  /**
   * Get min validation.
   *
   * @param  {IdmFormProjection} formProjection
   * @param {string} basicField
   * @param {int} min value (range)
   * @return {int} or null if not configured
   * @since 11.0.0
   */
  getMin(formProjection, basicField, readOnly = false, minValueRange = null) {
    const formValidation = this.getBasicFieldValidation(formProjection, basicField);
    if (!formValidation) {
      return minValueRange;
    }
    if (this.isReadOnly(formProjection, basicField, readOnly)) {
      return minValueRange;
    }
    //
    let min = formValidation.min;
    // not configured
    if (Utils.Ui.isEmpty(min)) {
      return minValueRange;
    }
    min = parseInt(min, 10);
    if (Utils.Ui.isEmpty(min)) {
      return minValueRange;
    }
    if (Utils.Ui.isEmpty(minValueRange)) {
      return min;
    }
    //
    return min < minValueRange ? minValueRange : min;
  }

  /**
   * Get max validation.
   *
   * @param  {IdmFormProjection} formProjection
   * @param {string} basicField
   * @param {int} max value (range)
   * @return {int} or null if not configured
   * @since 11.0.0
   */
  getMax(formProjection, basicField, readOnly = false, maxValueRange = null) {
    const formValidation = this.getBasicFieldValidation(formProjection, basicField);
    if (!formValidation) {
      return maxValueRange;
    }
    if (this.isReadOnly(formProjection, basicField, readOnly)) {
      return maxValueRange;
    }
    //
    let max = formValidation.max;
    // not configured
    if (Utils.Ui.isEmpty(max)) {
      return maxValueRange;
    }
    max = parseInt(max, 10);
    if (Utils.Ui.isEmpty(max)) {
      return maxValueRange;
    }
    if (Utils.Ui.isEmpty(maxValueRange)) {
      return max;
    }
    //
    return max > maxValueRange ? maxValueRange : max;
  }

  /**
   * Get min date validation.
   *
   * @param  {IdmFormProjection} formProjection
   * @param {string} basicField
   * @return {moment} or null if not configured
   * @since 11.0.0
   */
  getMinDate(formProjection, basicField, readOnly = false) {
    const minDays = this.getMin(formProjection, basicField, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(minDays) || readOnly) {
      return null;
    }
    //
    return moment().add(minDays, 'days');
  }

  /**
   * Get max date validation.
   *
   * @param  {IdmFormProjection} formProjection
   * @param {string} basicField
   * @return {moment} or null if not configured
   * @since 11.0.0
   */
  getMaxDate(formProjection, basicField, readOnly = false) {
    const maxDays = this.getMax(formProjection, basicField, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(maxDays) || readOnly) {
      return null;
    }
    //
    return moment().add(maxDays, 'days');
  }

  getInvalidBasicField(validationErrors, attributeCode) {
    if (!validationErrors) {
      return [];
    }
    //
    return validationErrors
      .filter(attribute => { // by attribute code
        return attribute.attributeCode === attributeCode && attribute.definitionCode === 'idm:basic-fields';
      });
  }

  /**
   * Returns true, when filled form is valid
   *
   * @return {Boolean} valid = true
   */
  isValid() {
    const { identityProjection } = this.state;
    //
    // form validation
    let isValid = true;
    if (!this.refs.form.isFormValid()
        || (this.refs.eav && !this.refs.eav.isValid())
        || (this.refs.password && !this.refs.password.validate())) {
      isValid = false;
    }
    for (let i = 0; i < identityProjection.allContracts.length; i++) {
      if (this.refs[`contractEav-${ i }`] && !this.refs[`contractEav-${ i }`].isValid()) {
        isValid = false;
      }
    }
    //
    return isValid;
  }

  /**
   * Get identity projection (data) from form before send projection to BE (see #save) and after validation (see #isValid).
   */
  getIdentityProjection() {
    const { identityProjection, isNew, formProjection, editContracts } = this.state;
    const data = this.refs.form.getData();
    //
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
      if (isNew && this.isTrue(formProjection, 'all-contracts') && !editContracts.has(i)) {
        // Contract is not edited => not saved.
        continue;
      }
      const contract = _.clone(identityProjection.allContracts[i]); // prevent to fill id into form => permissions will be evaluated as UPDATE
      if (identityContractManager.canSave(contract)) {
        // generate contract id on FE => roles and other positions can be added to concrete contract
        if (!contract.id) {
          contract.id = uuid.v1();
        }
        if (this.refs[`validFrom-${ i }`]) {
          contract.validFrom = data[`validFrom-${ i }`];
        } else if (isNew) {
          // not shown -but preset in init => reset is needed
          contract.validFrom = null;
        }
        if (this.refs[`validTill-${ i }`]) {
          contract.validTill = data[`validTill-${ i }`];
        }
        if (this.refs[`workPosition-${ i }`]) {
          contract.workPosition = data[`workPosition-${ i }`];
        }
        if (this.refs[`contractEav-${ i }`]) {
          contract._eav = this.refs[`contractEav-${ i }`].getValues();
        }
        if (_identityProjection.contract === null) {
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
        if (contractPositionManager.canSave(otherPosition) && identityContractManager.canSave(isNew ? {} : contract)) {
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
    return _identityProjection;
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    //
    // form validation
    if (!this.isValid()) {
      return;
    }
    // form show loading
    this.refs.form.processStarted();
    // get data from form
    const identityProjection = this.getIdentityProjection();
    // post => save
    this.context.store.dispatch(identityProjectionManager.saveProjection(identityProjection, null, this.afterSave.bind(this)));
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  afterSave(identityProjection, error) {
    if (error) {
      let validationErrors = null;
      if (error.statusEnum === 'FORM_INVALID' && error.parameters) {
        validationErrors = error.parameters.attributes;
        // focus the first invalid component
        if (validationErrors && validationErrors.length > 0) {
          // identity owner
          const firstValidationError = validationErrors[0];
          if (firstValidationError.ownerType === 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity') {
            const found = this.refs.eav && this.refs.eav.focus(firstValidationError.attributeCode);
            if (!found && this.refs[firstValidationError.attributeCode] && firstValidationError.definitionCode === 'idm:basic-fields') {
              this.refs[firstValidationError.attributeCode].focus();
            }
          } else {
            // get contract index by uuid
            const _identityProjection = this.state.identityProjection;
            const allContracts = _identityProjection.allContracts;
            let contractIndex = null;
            if (allContracts.length > 0) {
              contractIndex = allContracts.findIndex((contract) => contract.id === firstValidationError.ownerId);
            }
            if (contractIndex !== null && this.refs[`contractEav-${ contractIndex }`]) {
              this.setState({
                activeKey: contractIndex
              }, () => {
                this.refs[`contractEav-${ contractIndex }`].focus(firstValidationError.attributeCode, firstValidationError.ownerId);
              });
            }
          }
        }
      }
      //
      this.setState({
        validationError: error, // from password
        validationErrors, // from eav forms
        validationDefinition: false
      }, () => {
        this.addError(error);
        if (this.refs.form) {
          this.refs.form.processEnded();
        }
      });
    } else {
      this.setState({
        validationErrors: null
      }, () => {
        const { isNew } = this.state;
        //
        this._initProjection(identityProjection.id, identityProjection, this.state.formProjection, false);
        this.addMessage({
          message: this.i18n('action.save.success', { record: identityProjection.identity.username, count: 1 })
        });
        this.context.history.replace(identityManager.getDetailLink(identityProjection.identity));
        if (this.refs.form) {
          // form show loading
          this.refs.form.processEnded();
        }
        // reload role requests, if new
        if (isNew && this.refs.identityRolesTable) {
          this.refs.identityRolesTable._refreshAll();
        }
      });
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
      editContracts: !editContracts.has(index) ? editContracts.add(index) : editContracts.remove(index)
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
    const { formProjection, identityProjection, isNew, validationErrors } = this.state;
    const readOnly = !identityProjectionManager.canSave(isNew ? null : identityProjection);
    const _readOnlyUsername = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGEUSERNAME'));
    const _readOnlyName = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGENAME'));
    const _readOnlyExternalCode = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGEEXTERNALCODE'));
    const _readOnlyEmail = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGEEMAIL'));
    const _readOnlyPhone = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGEPHONE'));
    const _readOnlyDescription = readOnly || (!isNew && !this.hasPermission(identityProjection, 'CHANGEDESCRIPTION'));
    //
    return (
      <Basic.Div>
        <Basic.TextField
          ref="username"
          label={ this.i18n('identity.username.label') }
          rendered={ this.isRendered(formProjection, 'username') }
          readOnly={ this.isReadOnly(formProjection, 'username', _readOnlyUsername) }
          required={ this.isRequired(formProjection, 'username', _readOnlyUsername) }
          min={ this.getMin(formProjection, 'username', _readOnlyUsername) }
          max={ this.getMax(formProjection, 'username', _readOnlyUsername, 255) }
          validationMessage={ this.getValidationMessage(formProjection, 'username') }
          validationErrors={
            this.getInvalidBasicField(validationErrors, 'username')
          }/>

        <Basic.Row>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'lastName') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'firstName') }>
            <Basic.TextField
              ref="firstName"
              label={ this.i18n('content.identity.profile.firstName') }
              readOnly={ this.isReadOnly(formProjection, 'firstName', _readOnlyName) }
              required={ this.isRequired(formProjection, 'firstName', _readOnlyName) }
              min={ this.getMin(formProjection, 'firstName', _readOnlyName) }
              max={ this.getMax(formProjection, 'firstName', _readOnlyName, 255) }
              validationMessage={ this.getValidationMessage(formProjection, 'firstName') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'firstName')
              }/>
          </Basic.Col>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'firstName') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'lastName') }>
            <Basic.TextField
              ref="lastName"
              label={ this.i18n('content.identity.profile.lastName') }
              readOnly={ this.isReadOnly(formProjection, 'lastName', _readOnlyName) }
              required={ this.isRequired(formProjection, 'lastName', _readOnlyName) }
              min={ this.getMin(formProjection, 'lastName', _readOnlyName) }
              max={ this.getMax(formProjection, 'lastName', _readOnlyName, 255) }
              validationMessage={ this.getValidationMessage(formProjection, 'lastName') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'lastName')
              }/>
          </Basic.Col>
        </Basic.Row>

        <Basic.TextField
          ref="externalCode"
          label={ this.i18n('content.identity.profile.externalCode') }
          rendered={ this.isRendered(formProjection, 'externalCode') }
          readOnly={ this.isReadOnly(formProjection, 'externalCode', _readOnlyExternalCode) }
          required={ this.isRequired(formProjection, 'externalCode', _readOnlyExternalCode) }
          min={ this.getMin(formProjection, 'externalCode', _readOnlyExternalCode) }
          max={ this.getMax(formProjection, 'externalCode', _readOnlyExternalCode, 255) }
          validationMessage={ this.getValidationMessage(formProjection, 'externalCode') }
          validationErrors={
            this.getInvalidBasicField(validationErrors, 'externalCode')
          }/>

        <Basic.Row>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'titleAfter') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'titleBefore') }>
            <Basic.TextField
              ref="titleBefore"
              label={ this.i18n('entity.Identity.titleBefore') }
              readOnly={ this.isReadOnly(formProjection, 'titleBefore', _readOnlyName) }
              required={ this.isRequired(formProjection, 'titleBefore', _readOnlyName) }
              min={ this.getMin(formProjection, 'titleBefore', _readOnlyName) }
              max={ this.getMax(formProjection, 'titleBefore', _readOnlyName, 100) }
              validationMessage={ this.getValidationMessage(formProjection, 'titleBefore') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'titleBefore')
              }/>
          </Basic.Col>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'titleBefore') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'titleAfter') }>
            <Basic.TextField
              ref="titleAfter"
              label={ this.i18n('entity.Identity.titleAfter') }
              readOnly={ this.isReadOnly(formProjection, 'titleAfter', _readOnlyName) }
              required={ this.isRequired(formProjection, 'titleAfter', _readOnlyName) }
              min={ this.getMin(formProjection, 'titleAfter', _readOnlyName) }
              max={ this.getMax(formProjection, 'titleAfter', _readOnlyName, 100) }
              validationMessage={ this.getValidationMessage(formProjection, 'titleAfter') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'titleAfter')
              }/>
          </Basic.Col>
        </Basic.Row>

        <Basic.Row>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'phone') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'email') }>
            <Basic.TextField
              ref="email"
              label={ this.i18n('content.identity.profile.email.label') }
              placeholder={ this.i18n('content.identity.profile.email.placeholder') }
              validation={ Joi.string().email() }
              readOnly={ this.isReadOnly(formProjection, 'email', _readOnlyEmail) }
              required={ this.isRequired(formProjection, 'email', _readOnlyEmail) }
              min={ this.getMin(formProjection, 'email', _readOnlyEmail) }
              max={ this.getMax(formProjection, 'email', _readOnlyEmail, 255) }
              validationMessage={ this.getValidationMessage(formProjection, 'email') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'email')
              }/>
          </Basic.Col>
          <Basic.Col
            lg={ this.isRendered(formProjection, 'email') ? 6 : 12 }
            rendered={ this.isRendered(formProjection, 'phone') }>
            <Basic.TextField
              ref="phone"
              label={ this.i18n('content.identity.profile.phone.label') }
              placeholder={ this.i18n('content.identity.profile.phone.placeholder') }
              readOnly={ this.isReadOnly(formProjection, 'phone', _readOnlyPhone) }
              required={ this.isRequired(formProjection, 'phone', _readOnlyPhone) }
              min={ this.getMin(formProjection, 'phone', _readOnlyPhone) }
              max={ this.getMax(formProjection, 'phone', _readOnlyPhone, 30) }
              validationMessage={ this.getValidationMessage(formProjection, 'phone') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'phone')
              }/>
          </Basic.Col>
        </Basic.Row>

        <Basic.TextArea
          ref="description"
          label={ this.i18n('content.identity.profile.description.label') }
          placeholder={ this.i18n('content.identity.profile.description.placeholder') }
          rows={ 4 }
          rendered={ this.isRendered(formProjection, 'description') }
          readOnly={ this.isReadOnly(formProjection, 'description', _readOnlyDescription) }
          required={ this.isRequired(formProjection, 'description', _readOnlyDescription) }
          min={ this.getMin(formProjection, 'description', _readOnlyDescription) }
          max={ this.getMax(formProjection, 'description', _readOnlyDescription, 1000) }
          validationMessage={ this.getValidationMessage(formProjection, 'description') }
          validationErrors={
            this.getInvalidBasicField(validationErrors, 'description')
          }/>

        <Basic.EnumSelectBox
          ref="state"
          enum={ IdentityStateEnum }
          useSymbol={ false }
          label={ this.i18n('entity.Identity.state.label') }
          helpBlock={ <span>{ this.i18n('entity.Identity.state.help') }</span> }
          readOnly
          rendered={ !isNew && this.isRendered(formProjection, 'state') }/>

        <Basic.Checkbox
          ref="disabled"
          label={ this.i18n('entity.Identity.disabledReadonly.label') }
          helpBlock={ this.i18n('entity.Identity.disabledReadonly.help') }
          readOnly
          rendered={ !isNew && this.isRendered(formProjection, 'disabled') } />

        { this.renderIdentityAttributes(identityProjection, isNew, readOnly) }

      </Basic.Div>
    );
  }

  /**
   * Render identity eav attributes.
   */
  renderIdentityAttributes() {
    const { attributes, identityProjection, isNew, validationErrors } = this.state;
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
        formInstances={ isNew ? null : identityProjection._eav }
        validationErrors={ validationErrors }
        useDefaultValue={ isNew }/>
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
    if (!showContract || !this.isTrue(formProjection, 'prime-contract') || this.isTrue(formProjection, 'all-contracts')) {
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
  renderContract(index, readOnly = false) {
    const { location } = this.props;
    const { formProjection, identityProjection, validationErrors } = this.state;
    const contract = identityProjection.allContracts.length > index ? identityProjection.allContracts[index] : {};
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    const _readOnly = readOnly || !identityContractManager.canSave(contract) || contract.controlledBySlices;
    //
    return (
      <Basic.Div>
        <Basic.Row
          rendered={
            this.isRendered(formProjection, 'IdmIdentityContract.validFrom')
            ||
            this.isRendered(formProjection, 'IdmIdentityContract.validTill')
          }>
          <Basic.Col lg={ this.isRendered(formProjection, 'IdmIdentityContract.validTill') ? 6 : 12 }>
            <Basic.DateTimePicker
              mode="date"
              ref={ `validFrom-${ index }` }
              label={ this.i18n('contract.validFrom.label') }
              rendered={ this.isRendered(formProjection, 'IdmIdentityContract.validFrom') }
              readOnly={ this.isReadOnly(formProjection, 'IdmIdentityContract.validFrom', _readOnly) }
              required={ this.isRequired(formProjection, 'IdmIdentityContract.validFrom', _readOnly) }
              minDate={ this.getMinDate(formProjection, 'IdmIdentityContract.validFrom', _readOnly) }
              maxDate={ this.getMaxDate(formProjection, 'IdmIdentityContract.validFrom', _readOnly) }
              validationMessage={ this.getValidationMessage(formProjection, 'IdmIdentityContract.validFrom') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'IdmIdentityContract.validFrom')
              }/>
          </Basic.Col>
          <Basic.Col lg={ this.isRendered(formProjection, 'IdmIdentityContract.validFrom') ? 6 : 12 }>
            <Basic.DateTimePicker
              mode="date"
              ref={ `validTill-${ index }` }
              label={ this.i18n('contract.validTill.label') }
              rendered={ this.isRendered(formProjection, 'IdmIdentityContract.validTill') }
              readOnly={ this.isReadOnly(formProjection, 'IdmIdentityContract.validTill', _readOnly) }
              required={ this.isRequired(formProjection, 'IdmIdentityContract.validTill', _readOnly) }
              minDate={ this.getMinDate(formProjection, 'IdmIdentityContract.validTill', _readOnly) }
              maxDate={ this.getMaxDate(formProjection, 'IdmIdentityContract.validTill', _readOnly) }
              validationMessage={ this.getValidationMessage(formProjection, 'IdmIdentityContract.validTill') }
              validationErrors={
                this.getInvalidBasicField(validationErrors, 'IdmIdentityContract.validTill')
              }/>
          </Basic.Col>
        </Basic.Row>

        <Advanced.TreeNodeSelect
          ref={ `workPosition-${ index }` }
          label={ this.i18n('contract.workPosition.label') }
          header={ this.i18n('contract.workPosition.label') }
          treeNodeLabel={ this.i18n('contract.workPosition.label') }
          useFirstType
          rendered={ this.isRendered(formProjection, 'IdmIdentityContract.workPosition') }
          readOnly={ this.isReadOnly(formProjection, 'IdmIdentityContract.workPosition', _readOnly) }
          required={ this.isRequired(formProjection, 'IdmIdentityContract.workPosition', _readOnly) }
          validationMessage={ this.getValidationMessage(formProjection, 'IdmIdentityContract.workPosition') }
          validationErrors={
            this.getInvalidBasicField(validationErrors, 'IdmIdentityContract.workPosition')
          }/>

        { this.renderOtherPosition(index, _readOnly) }

        { this.renderContractAttributes(index, readOnly || contract.controlledBySlices) }

        {
          !isNew
          ||
          this.renderAssignedRoles(index, readOnly)
        }
      </Basic.Div>
    );
  }

  /**
   * Render contract eav attributes by index.
   */
  renderContractAttributes(index, readOnly = false) {
    const { attributes, identityProjection, isNew, validationErrors } = this.state;
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
        showSaveButton={ !readOnly }
        showAttributesOnly
        showDefinitions={ attributes }
        entityId={ isNew ? null : contract.id }
        formInstances={ isNew ? null : contract._eav }
        validationErrors={ validationErrors }
        useDefaultValue={ isNew }/>
    );
  }

  /**
   * Render one other contrantact position for contract by index..
   */
  renderOtherPosition(index, readOnly = false) {
    const { userContext } = this.props;
    const { identityProjection, formProjection, isNew } = this.state;
    const contract = identityProjection.allContracts.length > index ? identityProjection.allContracts[index] : {};
    const otherPosition = identityProjection.otherPositions && identityProjection.otherPositions.has(index)
    ? identityProjection.otherPositions.get(index)[0]
    : {};
    //
    if (!this.isTrue(formProjection, 'other-position')) {
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
          readOnly={ readOnly || !contractPositionManager.canSave(otherPosition) || !identityContractManager.canSave(contract) }/>
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
    if (!this.isTrue(formProjection, 'all-contracts') || identityProjection.allContracts.length === 0) {
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
                      <Basic.Div rendered={ isNew && identityProjection.allContracts.length > 1 }>
                        <Basic.ToggleSwitch
                          label={ this.i18n('button.editContract.label') }
                          onChange={ this._onEdit.bind(this, index) }
                          value={ editContracts.has(index) }/>
                      </Basic.Div>
                      { this.renderContract(index, isNew && !editContracts.has(index)) }
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
    if (!this.isRendered(formProjection, 'password')) {
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
  renderAssignedRoles(index, readOnly = false) {
    const { userContext } = this.props;
    const { identityProjection, formProjection, isNew } = this.state;
    //
    if (!SecurityManager.hasAllAuthorities(['IDENTITYROLE_READ', 'IDENTITYCONTRACT_CHANGEPERMISSION'], userContext)) {
      return null;
    }
    if (!this.isTrue(formProjection, 'assigned-roles')) {
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
            multiSelect
            readOnly={ readOnly }/>
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

  /**
   * Render back button (browser history is used).
   */
  renderBackButton() {
    return (
      <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
        { this.i18n('button.back') }
      </Basic.Button>
    );
  }

  /**
   * Render main save button.
   */
  renderSaveButton() {
    const { showLoading } = this.props;
    const { identityProjection, isNew } = this.state;
    //
    return (
      <Basic.Button
        type="submit"
        level="success"
        showLoading={ showLoading }
        showLoadingIcon
        showLoadingText={ this.i18n('button.saving') }
        rendered={ identityProjectionManager.canSave(isNew ? null : identityProjection) }
        onClick={ this.save.bind(this) }>
        { this.i18n('button.save') }
      </Basic.Button>
    );
  }

  /**
   * Render additional action buttons.
   * Buttons Are rendered between back and save button.
   */
  renderAdditionalButtons() {
    const { match, showLoading, userContext } = this.props;
    const { entityId } = match.params;
    const { identityProjection, formProjection, isNew } = this.state;
    //
    const buttons = [];
    if (formProjection) {
      buttons.push(
        <Basic.Button
          type="button"
          level="link"
          rendered={ SecurityManager.hasAllAuthorities(['FORMPROJECTION_UPDATE'], userContext) && this.isDevelopment() }
          onClick={ () => this.context.history.push(`/form-projections/${ formProjection.id }/detail`) }>
          { this.i18n('button.formProjection.label') }
        </Basic.Button>
      );
    }
    if (!isNew) {
      buttons.push(
        <DisableIdentityDashboardButton
          entityId={ identityProjection.username }
          identity={ identityProjection }
          permissions={ identityProjection._permissions }
          buttonSize="default"
          onComplete={ () => this._fetchIdentityProjection(entityId, false) }
          showLoading={ showLoading }/>
      );
      buttons.push(
        <EnableIdentityDashboardButton
          entityId={ identityProjection.username }
          identity={ identityProjection }
          permissions={ identityProjection._permissions }
          buttonSize="default"
          onComplete={ () => this._fetchIdentityProjection(entityId, false) }
          showLoading={ showLoading }/>
      );
    }
    //
    return buttons;
  }

  render() {
    const { identityProjection, isNew } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Div className="col-lg-offset-2 col-lg-8">

            { this.renderHeader() }

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
                    <form onSubmit={ this.save.bind(this) }>
                      { this.renderIdentity() }

                      { this.renderPassword() }

                      { this.renderPrimeContract() }

                      { this.renderAllContracts() }

                      {
                        /* onEnter action - is needed because buttons are outside html form
                          (complex component with tables and filters bellow need to be outside too to prevent form submit) */
                      }
                      <input type="submit" className="hidden"/>
                    </form>
                    {
                      isNew
                      ||
                      this.renderAssignedRoles()
                    }
                  </Basic.AbstractForm>
                </Basic.PanelBody>
                <Basic.PanelFooter>
                  { this.renderBackButton() }
                  { this.renderAdditionalButtons() }
                  { this.renderSaveButton() }
                </Basic.PanelFooter>
              </Basic.Panel>
            }
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
