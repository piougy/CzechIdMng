import React, { PropTypes } from 'react';
import Joi from 'joi';
import { connect } from 'react-redux';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import ApiOperationTypeEnum from '../../enums/ApiOperationTypeEnum';
import IdentityStateEnum from '../../enums/IdentityStateEnum';

const identityManager = new IdentityManager();

/**
 * Identity's detail form
 *
 * @author Radek Tomiška
 */
class IdentityDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      showLoadingIdentityTrimmed: false,
      setDataToForm: false
    };
  }

  getContentKey() {
    return 'content.identity.profile';
  }

  componentDidMount() {
    const { identity } = this.props;
    this.refs.form.setData(identity);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.identity) {
      if (nextProps.identity._trimmed) {
        this.setState({showLoadingIdentityTrimmed: true});
      } else {
        this.setState({showLoadingIdentityTrimmed: false});
      }
      if (nextProps.identity !== this.props.identity) {
        // after receive new Identity we will hide showLoading on form
        this.setState({showLoading: false, setDataToForm: true});
      }
    }
  }

  componentDidUpdate() {
    if (this.props.identity && !this.props.identity._trimmed && this.state.setDataToForm) {
      // We have to set data to form after is rendered
      this.transformData(this.props.identity, null, ApiOperationTypeEnum.GET);
    }
  }

  onSave(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const json = this.refs.form.getData();
    this.saveIdentity(json);
  }

  saveIdentity(json) {
    this.setState({
      showLoading: true,
      setDataToForm: false // Form will not be set new data (we are waiting to saved data)
    }, () => {
      this.context.store.dispatch(identityManager.updateEntity(json, null, (updatedEntity, error) => {
        this._afterSave(updatedEntity, error);
      }));
    });
  }

  _afterSave(entity, error) {
    this.setState({
      showLoading: false
    }, () => {
      if (error) {
        this.addError(error);
        return;
      }
      this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.saved', { username: entity.username }) });
      //
      // when username was changed, then new url is replaced
      const { identity } = this.props;
      if (identity.username !== entity.username) {
        this.context.router.replace(`/identity/${encodeURIComponent(entity.username)}/profile`);
      }
    });
  }

  transformData(json, error, operationType) {
    this.refs.form.setData(json, error, operationType);
  }

  render() {
    const { identity, readOnly, _permissions } = this.props;
    const { showLoading, showLoadingIdentityTrimmed } = this.state;
    //
    const blockLoginDate = identity && identity.blockLoginDate ? moment(identity.blockLoginDate).format(this.i18n('format.datetime')) : null;
    //
    return (
      <div>
        <form onSubmit={this.onSave.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.PanelHeader text={this.i18n('header')}/>
            <Basic.Alert
              ref="blockLoginDate"
              level="warning"
              rendered={blockLoginDate !== null}
              text={this.i18n('blockLoginDate', {date: blockLoginDate})} />
            <Basic.AbstractForm ref="form" readOnly={ !identityManager.canSave(identity, _permissions) || readOnly } showLoading={showLoadingIdentityTrimmed || showLoading}>
              <Basic.TextField ref="username" label={this.i18n('content.identity.profile.username')} required max={255}/>
              <Basic.TextField ref="firstName" label={this.i18n('content.identity.profile.firstName')} max={255} />
              <Basic.TextField ref="lastName" label={this.i18n('content.identity.profile.lastName')} max={255} />
              <Basic.TextField ref="externalCode" label={this.i18n('content.identity.profile.externalCode')} max={255}/>

              <Basic.Row>
                <div className="col-lg-6">
                  <Basic.TextField ref="titleBefore" label={this.i18n('entity.Identity.titleBefore')} max={100} />
                </div>
                <div className="col-lg-6">
                  <Basic.TextField ref="titleAfter" label={this.i18n('entity.Identity.titleAfter')} max={100}/>
                </div>
              </Basic.Row>

              <Basic.Row>
                <div className="col-lg-6">
                  <Basic.TextField
                    ref="email"
                    label={this.i18n('content.identity.profile.email.label')}
                    placeholder={this.i18n('email.placeholder')}
                    validation={Joi.string().allow(null).email()}/>
                </div>
                <div className="col-lg-6">
                  <Basic.TextField
                    ref="phone"
                    label={this.i18n('content.identity.profile.phone.label')}
                    placeholder={this.i18n('phone.placeholder')}
                    max={30} />
                </div>
              </Basic.Row>

              <Basic.TextArea
                ref="description"
                label={this.i18n('content.identity.profile.description.label')}
                placeholder={this.i18n('description.placeholder')}
                rows={4}
                max={1000}/>

              <Basic.EnumSelectBox
                ref="state"
                enum={ IdentityStateEnum }
                useSymbol={ false }
                label={ this.i18n('entity.Identity.state.label') }
                helpBlock={ <span>{ this.i18n('entity.Identity.state.help') }</span> }
                readOnly/>

              <Basic.Checkbox
                ref="disabled"
                label={this.i18n('entity.Identity.disabledReadonly.label')}
                helpBlock={this.i18n('entity.Identity.disabledReadonly.help')}
                readOnly />

            </Basic.AbstractForm>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ identityManager.canSave(identity, _permissions) }
                hidden={ readOnly }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

IdentityDetail.propTypes = {
  identity: PropTypes.object,
  entityId: PropTypes.string.isRequired,
  readOnly: PropTypes.bool,
  userContext: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
IdentityDetail.defaultProps = {
  userContext: null,
  _permissions: null,
  readOnly: false
};

function select(state, component) {
  return {
    userContext: state.security.userContext,
    _permissions: identityManager.getPermissions(state, null, component.entityId)
  };
}
export default connect(select)(IdentityDetail);
