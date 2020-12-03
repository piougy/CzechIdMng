import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Joi from 'joi';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import {
  ProfileManager,
  IdentityManager,
  DataManager,
  SecurityManager,
  ConfigurationManager,
  NotificationConfigurationManager
} from '../../redux';
import { LocalizationService } from '../../services';
import TwoFactorAuthenticationTypeEnum from '../../enums/TwoFactorAuthenticationTypeEnum';

const identityManager = new IdentityManager();
const profileManager = new ProfileManager();
const securityManager = new SecurityManager();
const notificationConfigurationManager = new NotificationConfigurationManager();

/**
 * Flag icon in enum select box.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class FlagOptionDecorator extends Basic.SelectBox.OptionDecorator {

  renderIcon(entity) {
    const lgnClassName = classnames(
      'flag',
      entity.value
    );
    //
    return (
      <span className={ lgnClassName } style={{ marginRight: 7 }}/>
    );
  }
}

/**
 * Flag icon in enum select box.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class FlagValueDecorator extends Basic.SelectBox.ValueDecorator {

  renderIcon(entity) {
    const lgnClassName = classnames(
      'flag',
      entity.value
    );
    //
    return (
      <span className={ lgnClassName } style={{ marginRight: 7 }}/>
    );
  }
}

/**
 * Identity profile - modal dialog.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class Profile extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showTwoFactorConfirm: null,
      twoFactorRegistration: null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(notificationConfigurationManager.fetchSupportedNotificationTypes());
  }

  _getIdentityIdentifier() {
    const { profile, userContext } = this.props;
    //
    if (profile._embedded && profile._embedded.identity) {
      return profile._embedded.identity.username;
    }
    if (!profile.identity) {
      // profile is not created yet => new will be created by logged identity
      return userContext.username;
    }
    return profile.identity;
  }

  onSave(entity = {}, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const json = { ...this.refs.form.getData(), ...entity };
    //
    if (Utils.Entity.isNew(json)) {
      json.identity = this.props.userContext.id;
      this.context.store.dispatch(profileManager.createEntity(json, null, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(profileManager.patchEntity(json, null, (patchedEntity, error) => {
        this._afterSave(patchedEntity, error);
      }));
    }
  }

  _afterSave(profile, error) {
    if (error) {
      this.addError(error);
      return;
    }
    //
    // if updated profile is logged identity profile => dispach changes
    this.context.store.dispatch(securityManager.setCurrentProfile(profile));
    this.addMessage({ level: 'success', key: 'core-profile-save-success', message: this.i18n('message.success.update') });
    // new profile can be created => nee need to set id into form
    this.refs.form.setData(profile);
  }

  onChangeTwoFactorAuthenticationType(option) {
    const json = this.refs.form.getData();
    //
    if (!option) {
      if (json.twoFactorAuthenticationType) {
        this.refs['confirm-delete-two-factor'].show(
          this.i18n(`content.identity.profile.twoFactor.confirmDelete.message`),
          this.i18n(`content.identity.profile.twoFactor.confirmDelete.title`)
        ).then(() => {
          this.onSave({ twoFactorAuthenticationType: null });
        }, () => {
          // Rejected
        });
      }
    } else if (option.value === json.twoFactorAuthenticationType) {
      return false;
    } else if (Utils.Entity.isNew(json)) {
      json.identity = this.props.userContext.id;
      this.context.store.dispatch(profileManager.createEntity(json, null, (createdEntity, error) => {
        if (error) {
          this.addError(error);
          return;
        }
        this.context.store.dispatch(securityManager.setCurrentProfile(createdEntity));
        this.refs.form.setData(createdEntity);
        this._showTwoFactorAuthenticationConfirm(createdEntity, option.value);
      }));
    } else {
      this._showTwoFactorAuthenticationConfirm(json, option.value);
    }
    //
    return false;
  }

  _showTwoFactorAuthenticationConfirm(profile, twoFactorAuthenticationType) {
    switch (twoFactorAuthenticationType) {
      case 'APPLICATION':
      case 'NOTIFICATION': {
        this.context.store.dispatch(profileManager.twoFactorAuthenticationInit(
          profile.id,
          twoFactorAuthenticationType,
          (twoFactorRegistration, error) => {
            if (error) {
              this.addError(error);
              return;
            }
            this.setState({
              showTwoFactorConfirm: twoFactorAuthenticationType,
              twoFactorRegistration,
              profile
            });
          }
        ));
        break;
      }
      default: {
        this.onSave({ twoFactorAuthenticationType });
      }
    }
  }

  onTwoFactorConfirm(twoFactorAuthenticationType, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const json = this.refs.form.getData();
    this.context.store.dispatch(profileManager.twoFactorAuthenticationConfirm(
      json.id,
      {
        verificationSecret: this.state.twoFactorRegistration.verificationSecret,
        verificationCode: json.verificationCode,
        twoFactorAuthenticationType
      },
      (updatedProfile, error) => {
        if (error) {
          this.addError(error);
          return;
        }
        this.setState({
          showTwoFactorConfirm: null,
          twoFactorRegistration: null,
          profile: null
        }, () => {
          this.context.store.dispatch(securityManager.setCurrentProfile(updatedProfile));
          this.addMessage({ level: 'success', key: 'core-profile-save-success', message: this.i18n('message.success.update') });
          this.refs.form.setData(updatedProfile);
        });
      }
    ));
  }

  _supportedLanguageOptions() {
    const supportedLanguages = LocalizationService.getSupportedLanguages();
    //
    if (!supportedLanguages || supportedLanguages.length === 0) {
      return [];
    }
    //
    return supportedLanguages.map(lng => {
      return {
        value: lng,
        niceLabel: lng
      };
    });
  }

  /**
   * Dropzone component function called after select file
   * @param file selected file (multiple is not allowed)
   */
  _onDrop(files) {
    if (this.refs.dropzone.state.isDragReject) {
      this.addMessage({
        message: this.i18n('content.identity.profile.fileRejected'),
        level: 'warning'
      });
      return;
    }
    files.forEach((file) => {
      const fileName = file.name.toLowerCase();
      if (!fileName.endsWith('.jpg') && !fileName.endsWith('.jpeg') && !fileName.endsWith('.png') && !fileName.endsWith('.gif')) {
        this.addMessage({
          message: this.i18n('content.identity.profile.fileRejected', {name: file.name}),
          level: 'warning'
        });
        return;
      }
      const objectURL = URL.createObjectURL(file);
      this.setState({
        cropperSrc: objectURL,
        showCropper: true,
        fileName: file.name
      });
    });
  }

  deleteImage() {
    this.refs['confirm-delete'].show(
      this.i18n(`content.identity.profile.deleteImage.message`),
      this.i18n(`content.identity.profile.deleteImage.title`)
    ).then(() => {
      this.context.store.dispatch(identityManager.deleteProfileImage(this._getIdentityIdentifier()));
    }, () => {
      // Rejected
    });
  }

  _showCropper() {
    this.setState({
      showCropper: true
    });
  }

  _closeCropper() {
    this.setState({
      showCropper: false
    });
  }

  _crop() {
    this.refs.cropper.crop((formData) => {
      // append selected fileName
      formData.fileName = this.state.fileName;
      formData.name = this.state.fileName;
      formData.append('fileName', this.state.fileName);
      //
      this.context.store.dispatch(identityManager.uploadProfileImage(this._getIdentityIdentifier(), formData, (profile, error) => {
        if (error) {
          this.addError(error);
          return;
        }
        // new profile can be created => wee need to set id into form
        this.refs.form.setData(profile);
      }));
    });
    this._closeCropper();
  }

  render() {
    const {
      rendered,
      show,
      onHide,
      showLoading,
      profile,
      _permissions,
      userContext,
      sizeOptions,
      _imageUrl,
      _imageLoading,
      _supportedNotificationTypes
    } = this.props;
    const {
      showCropper,
      cropperSrc,
      showTwoFactorConfirm,
      twoFactorRegistration
    } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    const supportedLanguageOptions = this._supportedLanguageOptions();
    // disable notification method, if sms gateway is not configured
    const supportTwoFactorNotificationAuthenticationType = _.includes(_supportedNotificationTypes, 'sms') || this.isDevelopment();
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-delete-two-factor" level="warning"/>

        <Basic.Modal
          show={ show }
          onHide={ onHide }
          keyboard
          backdrop="static"
          onEnter={ () => this.context.store.dispatch(identityManager.fetchProfile(userContext.username)) }>
          <form onSubmit={ showTwoFactorConfirm ? this.onTwoFactorConfirm.bind(this, showTwoFactorConfirm) : this.onSave.bind(this) }>
            <Basic.Modal.Header
              closeButton
              icon="user"
              text={
                showTwoFactorConfirm
                ?
                this.i18n('entity.Profile.twoFactorAuthenticationType.label')
                :
                this.i18n('content.identity.profile-setting.header')
              }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                data={ profile }
                readOnly={ !profileManager.canSave(profile, _permissions) }
                showLoading={ showLoading }>

                <Basic.Div className={ showTwoFactorConfirm ? 'hidden' : '' }>
                  <Basic.Div style={{ marginTop: 5, marginBottom: 20 }}>
                    <Basic.Div className="profile-image-wrapper">
                      <Basic.Div style={{ marginBottom: 15, fontWeight: 700 }}>
                        { this.i18n('entity.Profile.image.label') }
                      </Basic.Div>
                      <Advanced.ImageDropzone
                        ref="dropzone"
                        accept="image/*"
                        multiple={ false }
                        onDrop={ this._onDrop.bind(this) }
                        showLoading={ _imageLoading }
                        readOnly={ !profileManager.canSave(profile, _permissions) }>
                        <img className="img-thumbnail" alt="profile" src={ _imageUrl } />
                      </Advanced.ImageDropzone>
                      <Basic.Button
                        type="button"
                        rendered={ !!(cropperSrc && _imageUrl) }
                        titlePlacement="right"
                        onClick={ this._showCropper.bind(this) }
                        className="btn-xs btn-edit">
                        <Basic.Icon icon="edit"/>
                      </Basic.Button>
                      <Basic.Button
                        type="button"
                        level="danger"
                        rendered={ !!(_imageUrl && profileManager.canSave(profile, _permissions)) }
                        titlePlacement="left"
                        onClick={ this.deleteImage.bind(this) }
                        className="btn-xs btn-remove">
                        <Basic.Icon type="fa" icon="trash"/>
                      </Basic.Button>
                    </Basic.Div>
                  </Basic.Div>

                  <Basic.EnumSelectBox
                    ref="preferredLanguage"
                    label={ this.i18n('entity.Profile.preferredLanguage.label') }
                    helpBlock={ this.i18n('entity.Profile.preferredLanguage.help') }
                    hidden={ supportedLanguageOptions.length === 0 }
                    options={ supportedLanguageOptions }
                    clearable={ false }
                    emptyOptionLabel={ false }
                    optionComponent={ FlagOptionDecorator }
                    valueComponent={ FlagValueDecorator }
                    onChange={ (option) => this.onSave({ preferredLanguage: option.value }) }/>

                  <Basic.EnumSelectBox
                    ref="twoFactorAuthenticationType"
                    label={ this.i18n('entity.Profile.twoFactorAuthenticationType.label') }
                    helpBlock={ this.i18n('entity.Profile.twoFactorAuthenticationType.help') }
                    options={[
                      {
                        niceLabel: TwoFactorAuthenticationTypeEnum.getNiceLabel(
                          TwoFactorAuthenticationTypeEnum.findKeyBySymbol(TwoFactorAuthenticationTypeEnum.APPLICATION)
                        ),
                        value: TwoFactorAuthenticationTypeEnum.findKeyBySymbol(TwoFactorAuthenticationTypeEnum.APPLICATION),
                        description: this.i18n('core:enums.TwoFactorAuthenticationTypeEnum.helpBlock.APPLICATION')
                      },
                      {
                        niceLabel: TwoFactorAuthenticationTypeEnum.getNiceLabel(
                          TwoFactorAuthenticationTypeEnum.findKeyBySymbol(TwoFactorAuthenticationTypeEnum.NOTIFICATION)
                        ),
                        value: TwoFactorAuthenticationTypeEnum.findKeyBySymbol(TwoFactorAuthenticationTypeEnum.NOTIFICATION),
                        disabled: !supportTwoFactorNotificationAuthenticationType,
                        description:
                          supportTwoFactorNotificationAuthenticationType
                          ?
                          this.i18n('core:enums.TwoFactorAuthenticationTypeEnum.helpBlock.NOTIFICATION')
                          :
                          this.i18n('content.identity.profile.twoFactor.notification.disabled')
                      },
                    ]}
                    useSymbol={ false }
                    onChange={ this.onChangeTwoFactorAuthenticationType.bind(this) }/>

                  <Basic.EnumSelectBox
                    ref="defaultPageSize"
                    label={ this.i18n('entity.Profile.defaultPageSize.label') }
                    helpBlock={ this.i18n('entity.Profile.defaultPageSize.help') }
                    options={
                      sizeOptions.map(option => {
                        return {
                          value: option,
                          niceLabel: option
                        };
                      })
                    }
                    onChange={ (option) => this.onSave({ defaultPageSize: option ? option.value : null }) }/>

                  <Basic.Checkbox
                    ref="navigationCollapsed"
                    label={ this.i18n('entity.Profile.navigationCollapsed.label') }
                    helpBlock={ this.i18n('entity.Profile.navigationCollapsed.help') }
                    onChange={ (event) => this.onSave({ navigationCollapsed: event.currentTarget.checked }) }/>

                  <Basic.Checkbox
                    ref="systemInformation"
                    label={ this.i18n('entity.Profile.systemInformation.label') }
                    helpBlock={ this.i18n('entity.Profile.systemInformation.help') }
                    onChange={ (event) => this.onSave({ systemInformation: event.currentTarget.checked }) }/>
                </Basic.Div>

                <Basic.Div className={ showTwoFactorConfirm ? '' : 'hidden' }>
                  <Basic.Div className="text-center" rendered={ showTwoFactorConfirm === 'APPLICATION' }>
                    <Basic.Alert level="info">
                      { this.i18n('content.identity.profile.twoFactor.application.help', { escape: false }) }
                    </Basic.Alert>
                    {
                      !twoFactorRegistration
                      ||
                      <img src={ twoFactorRegistration.qrcode } alt="qr-code"/>
                    }
                    <br />
                    <p>
                      { this.i18n('content.identity.profile.twoFactor.application.code.enter') }
                      <Basic.Popover
                        trigger={ ['click'] }
                        className="abstract-entity-info-popover"
                        value={ (<strong>{ twoFactorRegistration ? twoFactorRegistration.verificationSecret : '' }</strong>) }>
                        <span style={{ color: '#31708f', margin: '0px 5px', cursor: 'pointer' }}>
                          { this.i18n('content.identity.profile.twoFactor.application.code.code') }
                        </span>
                      </Basic.Popover>
                      { this.i18n('content.identity.profile.twoFactor.application.code.instead') }
                    </p>
                    <br />
                    <br />
                  </Basic.Div>

                  <Basic.Alert level="info" rendered={ showTwoFactorConfirm === 'NOTIFICATION' }>
                    { this.i18n('content.identity.profile.twoFactor.notification.help', { escape: false }) }
                  </Basic.Alert>
                  <Basic.TextField
                    ref="verificationCode"
                    label={
                      showTwoFactorConfirm === 'APPLICATION'
                      ?
                      this.i18n('content.identity.profile.twoFactor.application.verificationCode.label')
                      :
                      this.i18n('content.identity.profile.twoFactor.notification.verificationCode.label')
                    }
                    placeholder={
                      showTwoFactorConfirm === 'APPLICATION'
                      ?
                      this.i18n('content.identity.profile.twoFactor.application.verificationCode.placeholder')
                      :
                      this.i18n('content.identity.profile.twoFactor.notification.verificationCode.placeholder')
                    }
                    helpBlock={
                      showTwoFactorConfirm === 'APPLICATION'
                      ?
                      this.i18n('content.identity.profile.twoFactor.application.verificationCode.help')
                      :
                      this.i18n('content.identity.profile.twoFactor.notification.verificationCode.help')
                    }
                    required={ showTwoFactorConfirm !== null }
                    validation={
                      showTwoFactorConfirm !== null
                      ?
                      Joi
                        .number()
                        .integer()
                        .required()
                        .min(0)
                        .max(999999)
                      :
                      Joi.any().allow(null)
                    }/>
                </Basic.Div>
              </Basic.AbstractForm>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                showLoading={ showLoading }
                onClick={ onHide }
                rendered={ !showTwoFactorConfirm }>
                { this.i18n('button.close') }
              </Basic.Button>

              <Basic.Button
                level="link"
                showLoading={ showLoading }
                onClick={ () => this.setState({ showTwoFactorConfirm: null }, () => this.refs.verificationCode.setValue('')) }
                rendered={ showTwoFactorConfirm }>
                { this.i18n('button.cancel') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                rendered={ showTwoFactorConfirm }>
                { this.i18n('button.enable.label') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

        <Basic.Modal
          bsSize="default"
          show={ showCropper }
          onHide={ this._closeCropper.bind(this) }
          backdrop="static" >
          <Basic.Modal.Body>
            <Advanced.ImageCropper
              ref="cropper"
              src={ cropperSrc }/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this._closeCropper.bind(this) }
              showLoading={ showLoading }>
              { this.i18n('button.close') }
            </Basic.Button>
            <Basic.Button
              level="info"
              onClick={ this._crop.bind(this) }
              showLoading={ showLoading }>
              { this.i18n('button.crop') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }

}

Profile.propTypes = {
  ...Basic.AbstractContent.propTypes,
  /**
   * Modal is shown.
   */
  show: PropTypes.bool,
  /**
   * onHide callback
   */
  onHide: PropTypes.func
};

Profile.defaultProps = {
  ...Basic.AbstractContent.defaultProps
};

function select(state) {
  const identifier = state.security.userContext.username;
  const profileUiKey = identityManager.resolveProfileUiKey(identifier);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    userContext: state.security.userContext,
    profile,
    showLoading: DataManager.isShowLoading(state, profileUiKey)
      || (profile ? profileManager.isShowLoading(state, null, profile.id) : false),
    _permissions: profile ? profileManager.getPermissions(state, null, profile.id) : [],
    sizeOptions: ConfigurationManager.getSizeOptions(state),
    _imageLoading: DataManager.isShowLoading(state, profileUiKey),
    _imageUrl: profile ? profile.imageUrl : null,
    _supportedNotificationTypes: DataManager.getData(state, NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES)
  };
}

export default connect(select)(Profile);
