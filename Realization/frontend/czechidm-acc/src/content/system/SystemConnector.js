import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Utils, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';

const uiKey = 'eav-connector-';
const manager = new SystemManager();

class SystemConnectorContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      error: null
    };
  }

  getContentKey() {
    return 'acc:content.system.connector';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-connector']);
    // load definition and values
    const { entityId } = this.props.params;
    this.context.store.dispatch(manager.fetchConnectorConfiguration(entityId, `${uiKey}-${entityId}`, (formInstance, error) => {
      if (error) {
        if (error.statusEnum === 'CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND') {
          this.addErrorMessage({ hidden: true, level: 'info' }, error);
          this.setState({ error });
        } else {
          this.addError(error);
          this.setState({ error: null });
        }
      } else {
        this.getLogger().debug(`[EavForm]: Loaded form definition [${formInstance.getDefinition().type}|${formInstance.getDefinition().name}]`);
      }
    }));
  }

  showDetail() {
    const { entityId } = this.props.params;
    this.context.router.push(`/system/${entityId}/detail`);
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.eav.isValid()) {
      return;
    }
    //
    const { entityId } = this.props.params;
    const filledFormValues = this.refs.eav.getValues();
    this.getLogger().debug(`[EavForm]: Saving form [${this.refs.eav.getFormDefinition().type}|${this.refs.eav.getFormDefinition().name}]`);
    // save values
    this.context.store.dispatch(manager.saveConnectorConfiguration(entityId, filledFormValues, `${uiKey}-${entityId}`, (savedFormInstance, error) => {
      if (error) {
        this.addError(error);
      } else {
        const system = manager.getEntity(this.context.store.getState(), entityId);
        this.addMessage({ message: this.i18n('save.success', { name: system.name }) });
        this.getLogger().debug(`[EavForm]: Form [${this.refs.eav.getFormDefinition().type}|${this.refs.eav.getFormDefinition().name}] saved`);
      }
    }));
  }

  render() {
    const { formInstance, _showLoading } = this.props;
    const { error } = this.state;

    let content;
    if (error) {
      // connector is wrong configured
      content = (
        <Basic.Alert level="info">
          {this.i18n(`${error.module}:error.${error.statusEnum}.message`, error.parameters)}
          <div style={{ marginTop: 15 }}>
            <Basic.Button level="info" onClick={this.showDetail.bind(this)}>
              {this.i18n('button.showBasicInfo')}
            </Basic.Button>
          </div>
        </Basic.Alert>
      );
    } else if (!formInstance || _showLoading) {
      // connector eav form is loaded from BE
      content = (
        <Basic.Loading isStatic showLoading/>
      );
    } else {
      // connector setting is ready
      content = (
        <form className="form-horizontal" style={{ marginTop: 15 }} onSubmit={this.save.bind(this)}>
          <Basic.PanelBody>
            <Advanced.EavForm ref="eav" formInstance={formInstance}/>
          </Basic.PanelBody>
          <Basic.PanelFooter>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      );
    }

    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          { content }
        </Basic.Panel>
      </div>
    );
  }
}

SystemConnectorContent.propTypes = {
  formInstance: PropTypes.oject,
  _showLoading: PropTypes.bool
};
SystemConnectorContent.defaultProps = {
  formInstance: null,
  _showLoading: false
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${entityId}`),
    formInstance: Managers.DataManager.getData(state, `${uiKey}-${entityId}`)
  };
}

export default connect(select)(SystemConnectorContent);
