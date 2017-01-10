import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Managers, Utils } from 'czechidm-core';
import { SystemManager } from '../../redux';

/**
 * Target system detail content
 */
class SystemDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new SystemManager();
    this.state = {
      _showLoading: false,
      selectedFramework: null
    };
  }

  getContentKey() {
    return 'acc:content.system.detail';
  }

  componentDidMount() {
    const { entity } = this.props;
    let data;
    if (Utils.Entity.isNew(entity) || !entity.connectorKey) {
      data = {
        ...entity
      };
    } else {
      data = {
        ...entity,
        connector: entity.connectorKey.fullName
      };
    }
    data.host = 'local';
    this.refs.form.setData(data);
    this.refs.name.focus();
  }

  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }

    const { uiKey, availableFrameworks } = this.props;
    if (!this.refs.form.isFormValid()) {
      return;
    }
    //
    this.setState({
      _showLoading: true
    }, () => {
      const entity = this.refs.form.getData();
      const connector = availableFrameworks.get(entity.connector.split(':')[0]).get(entity.connector);
      const saveEntity = {
        ...entity,
        connectorKey: {
          framework: connector.connectorKey.framework,
          connectorName: connector.connectorKey.connectorName,
          bundleName: connector.connectorKey.bundleName,
          bundleVersion: connector.connectorKey.bundleVersion
        }
      };

      if (Utils.Entity.isNew(saveEntity)) {
        this.context.store.dispatch(this.manager.createEntity(saveEntity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.patchEntity(saveEntity, `${uiKey}-detail`, (patchedEntity, error) => {
          this._afterSave(patchedEntity, error, afterAction);
        }));
      }
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    this.setState({
      _showLoading: false
    }, () => {
      if (error) {
        this.addError(error);
        return;
      }
      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      //
      if (afterAction === 'CLOSE') {
        this.context.router.replace(`systems`);
      } else {
        this.context.router.replace(`system/${entity.id}/detail`);
      }
    });
  }

  _getConnectorOptions(availableFrameworks) {
    if (!availableFrameworks) {
      return [];
    }
    const options = [];
    availableFrameworks.forEach((connectors, framework) => {
      connectors.forEach((connector, fullName) => {
        options.push({
          value: fullName,
          niceLabel: `${connector.connectorDisplayName} (${framework})`
        });
      });
    });
    return options;
  }

  render() {
    const { uiKey, entity, availableFrameworks } = this.props;
    const { _showLoading } = this.state;
    const _availableConnectors = this._getConnectorOptions(availableFrameworks);

    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')} >
                <Basic.TextField
                  ref="name"
                  label={this.i18n('acc:entity.System.name')}
                  required
                  max={255}/>
                <Basic.EnumSelectBox
                  ref="connector"
                  label={this.i18n('acc:entity.System.connectorKey.connectorName')}
                  placeholder={this.i18n('acc:entity.System.connectorKey.connectorName')}
                  options={_availableConnectors}
                  required/>
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('acc:entity.System.description')}
                  max={255}/>
                <Basic.Checkbox
                  ref="virtual"
                  label={this.i18n('acc:entity.System.virtual')}
                  rendered={false}/>
                <Basic.Checkbox
                  ref="readonly"
                  label={this.i18n('acc:entity.System.readonly.label')}
                  helpBlock={this.i18n('acc:entity.System.readonly.help')}/>
                <Basic.Checkbox
                  ref="queue"
                  label={this.i18n('acc:entity.System.queue.label')}
                  helpBlock={this.i18n('acc:entity.System.queue.help')}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('acc:entity.System.disabled')}/>
              </Basic.AbstractForm>
            </Basic.PanelBody>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={this.i18n('button.saveAndContinue')}
                onClick={this.save.bind(this, 'CONTINUE')}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}
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

SystemDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  availableFrameworks: PropTypes.object
};
SystemDetail.defaultProps = {
  availableFrameworks: null
};

function select(state) {
  return {
    availableFrameworks: Managers.DataManager.getData(state, SystemManager.AVAILABLE_CONNECTORS)
  };
}

export default connect(select)(SystemDetail);
