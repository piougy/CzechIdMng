import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import { PasswordManager, DataManager, IdentityManager } from '../../../redux';
import * as Utils from '../../../utils';

const manager = new PasswordManager();
const identityManager = new IdentityManager();

/**
 * Identity password tab with information about password metadata.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
class IdentityPasswordDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      enabled: null,
      _showLoading: false,
      isPasswordNeverExpires: null
    };
  }

  getContentKey() {
    return 'content.password';
  }

  getNavigationKey() {
    return 'profile-password-metadata';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(identityManager.fetchPassword(entityId, `identity-${entityId}-password`));
  }

  componentDidUpdate() {
    const { entity } = this.props;
    const { entityId } = this.props.match.params;
    //
    if (entity) {
      this.context.store.dispatch(manager.queueFetchPermissions(entity.id, `${ entityId }-password-permission`));
    }
  }

  onSave(event) {
    const { entityId } = this.props.match.params;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      _showLoading: true
    }, () => {
      this.refs.form.processStarted();
      const entity = this.refs.form.getData();
      this.context.store.dispatch(manager.patchEntity(entity, `identity-${entityId}-password`, this._afterSave.bind(this)));
    });
  }

  _afterSave(entity, error) {
    const { entityId } = this.props.match.params;
    if (error) {
      this.setState({
        _showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.processEnded();
      this.addMessage({ message: this.i18n('save.success')});
      this.context.store.dispatch(identityManager.fetchPassword(entityId, `identity-${entityId}-password`));
    });
  }

  onChangePasswordNeverExpires(event) {
    this.setState({
      isPasswordNeverExpires: event.currentTarget.checked
    });
  }

  render() {
    const { entity, showLoading, _permissions } = this.props;
    const { isPasswordNeverExpires, _showLoading } = this.state;

    let isPasswordNeverExpiresFinall = (entity && entity.passwordNeverExpires);
    if (isPasswordNeverExpires !== null) {
      isPasswordNeverExpiresFinall = isPasswordNeverExpires;
    }

    let blockLoginDate = null;
    if (entity && entity.blockLoginDate) {
      if (moment(entity.blockLoginDate).isAfter(moment())) {
        blockLoginDate = moment(entity.blockLoginDate).format(this.i18n('format.datetime'));
      }
    }

    const canSave = Utils.Permission.hasPermission(_permissions, 'UPDATE');
    const showLoadingFinal = showLoading || !_permissions || _showLoading;

    if (entity === false) {
      return (
        <Basic.Div style={{ padding: 15 }}>
          <Basic.Alert level="info" text={ this.i18n('noData') } style={{ margin: 0 }}/>
        </Basic.Div>
      );
    }
    //
    return (
      <Basic.Div>
        <Helmet title={this.i18n('title')} />
        <form onSubmit={ this.onSave.bind(this) }>
          <Basic.ContentHeader
            icon="component:password"
            text={ this.i18n('header') }
            style={{ marginBottom: 0, paddingRight: 15, paddingLeft: 15, paddingTop: 15 }} />
          <Basic.Panel className="no-border" style={{ paddingRight: 15, paddingLeft: 15 }}>
            <Basic.Alert
              ref="blockLoginDateInfo"
              level="warning"
              rendered={ blockLoginDate !== null }
              text={ this.i18n('blockLoginDateInfo', { date: blockLoginDate })} />
            <Basic.AbstractForm ref="form" showLoading={ showLoadingFinal } data={ entity } >
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.DateTimePicker
                    readOnly
                    ref="created"
                    helpBlock={ this.i18n('created.help') }
                    label={ this.i18n('created.label') }/>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.TextField
                    readOnly
                    ref="creator"
                    helpBlock={ this.i18n('creator.help') }
                    label={ this.i18n('creator.label') }/>
                </Basic.Col>
              </Basic.Row>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.DateTimePicker
                    ref="validFrom"
                    mode="date"
                    readOnly
                    helpBlock={ this.i18n('validFrom.help') }
                    label={ this.i18n('validFrom.label') }/>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.DateTimePicker
                    ref="validTill"
                    mode="date"
                    rendered={ !isPasswordNeverExpiresFinall }
                    readOnly={ !canSave }
                    helpBlock={ this.i18n('validTill.help') }
                    label={ this.i18n('validTill.label') }/>
                  <Basic.Alert
                    ref="passwordNeverExpiresAndValidTillInfo"
                    rendered={ isPasswordNeverExpiresFinall }
                    level="info"
                    text={ this.i18n('passwordNeverExpiresAndValidTillInfo', { date: blockLoginDate })}
                    style={ { margin: 0 } } />
                </Basic.Col>
              </Basic.Row>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.TextField
                    readOnly
                    ref="unsuccessfulAttempts"
                    helpBlock={ this.i18n('unsuccessfulAttempts.help') }
                    label={ this.i18n('unsuccessfulAttempts.label') }/>
                </Basic.Col>
              </Basic.Row>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.DateTimePicker
                    mode="datetime"
                    readOnly
                    ref="lastSuccessfulLogin"
                    helpBlock={ this.i18n('lastSuccessfulLogin.help') }
                    label={ this.i18n('lastSuccessfulLogin.label') }/>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.DateTimePicker
                    mode="datetime"
                    ref="blockLoginDate"
                    readOnly={ !canSave}
                    helpBlock={ this.i18n('blockLoginDate.help') }
                    label={ this.i18n('blockLoginDate.label') }/>
                </Basic.Col>
              </Basic.Row>

              <Basic.Checkbox
                ref="passwordNeverExpires"
                readOnly={ !canSave }
                onChange={ this.onChangePasswordNeverExpires.bind(this) }
                helpBlock={ this.i18n('passwordNeverExpires.help') }
                label={ this.i18n('passwordNeverExpires.label') }/>

              <Basic.Checkbox
                ref="mustChange"
                label={ this.i18n('entity.Password.mustChange.label') }
                helpBlock={ this.i18n('entity.Password.mustChange.help') }
                readOnly={ !canSave }/>

            </Basic.AbstractForm>

            <Basic.PanelFooter>
              <Basic.Button
                type="button"
                level="link"
                onClick={ this.context.history.goBack }
                showLoading={ showLoadingFinal }>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoadingFinal }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ canSave }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </Basic.Div>
    );
  }
}

IdentityPasswordDetail.propTypes = {
  entity: PropTypes.object,
  userContext: PropTypes.object,
  showLoading: PropTypes.bool
};
IdentityPasswordDetail.defaultProps = {
  entity: null,
  userContext: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  const uiKey = `identity-${entityId}-password`;
  const uiKeyPermission = `${entityId}-password-permission`;
  return {
    _permissions: Utils.Permission.getPermissions(state, `${entityId}-password-permission`),
    entity: DataManager.getData(state, uiKey),
    userContext: state.security.userContext,
    showLoading: Utils.Ui.isShowLoading(state, uiKey) || Utils.Ui.isShowLoading(state, uiKeyPermission)
  };
}

export default connect(select)(IdentityPasswordDetail);
