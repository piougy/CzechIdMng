import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Joi from 'joi';
//
import { Basic, Utils } from 'czechidm-core';
import { RemoteServerManager } from '../../redux';
//
const manager = new RemoteServerManager();

/**
* Remote server with connectors.
*
* @author Radek Tomiška
* @since 10.2.0
*/
class RemoteServerDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  getContentKey() {
    return 'acc:content.remote-servers';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, entity } = this.props;
    //
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, entity || {}, null, () => {
        this.refs.host.focus();
      }));
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, () => {
        this.refs.host.focus();
      }));
    }
  }

  getNavigationKey() {
    return 'sys-remote-server-detail';
  }

  save(event) {
    const { uiKey } = this.props;

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
      //
      if (entity.id === undefined) {
        this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error);
        }));
      } else {
        this.context.store.dispatch(manager.updateEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
      }
    });
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
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
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
      this.refs.password.openConfidential(false);
      if (isNew) {
        this.context.history.replace(`/remote-servers/${ entity.id }/detail`);
      }
    });
  }

  render() {
    const { uiKey, entity, showLoading, _permissions } = this.props;
    //
    return (
      <form onSubmit={ this.save.bind(this) }>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }>
          <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('detail.title') } />
          <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
            <Basic.AbstractForm
              ref="form"
              uiKey={ uiKey }
              data={ entity }
              readOnly={ !manager.canSave(entity, _permissions) }>

              <Basic.Div style={{ display: 'flex' }}>
                <Basic.Div style={{ flex: 3, marginRight: 15 }}>
                  <Basic.TextField
                    ref="host"
                    label={ this.i18n('acc:entity.RemoteServer.host.label') }
                    helpBlock={ this.i18n('acc:entity.RemoteServer.host.help') }
                    required
                    max={ 255 }/>
                </Basic.Div>
                <Basic.Div style={{ flex: 1 }}>
                  <Basic.TextField
                    ref="port"
                    label={ this.i18n('acc:entity.RemoteServer.port.label') }
                    helpBlock={ this.i18n('acc:entity.RemoteServer.port.help') }
                    required
                    validation={
                      Joi
                        .number()
                        .required()
                        .integer()
                        .min(0)
                        .max(65535)
                    }/>
                </Basic.Div>
              </Basic.Div>

              <Basic.Checkbox
                ref="useSsl"
                label={ this.i18n('acc:entity.RemoteServer.useSsl.label') }/>
              <Basic.TextField
                ref="password"
                type="password"
                confidential
                label={ this.i18n('acc:entity.RemoteServer.password.label') }/>
              <Basic.TextField
                ref="timeout"
                label={ this.i18n('acc:entity.RemoteServer.timeout.label') }
                validation={
                  Joi
                    .number()
                    .integer()
                    .min(0)
                    .max(65535)
                    .allow(null)
                }/>
              <Basic.TextArea
                ref="description"
                label={ this.i18n('entity.description.label') }
                rows={ 4 }
                max={ 255 }/>

            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={ showLoading } >
            <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ manager.canSave(entity, _permissions) }>
              { this.i18n('button.save') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

RemoteServerDetail.propTypes = {
  uiKey: PropTypes.string,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
RemoteServerDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  const entity = manager.getEntity(state, entityId);
  //
  return {
    entity,
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(RemoteServerDetail);
