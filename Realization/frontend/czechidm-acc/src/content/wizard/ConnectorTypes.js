import { Basic, Managers } from 'czechidm-core';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import IdmContext from 'czechidm-core/src/context/idm-context';
import { SystemManager } from '../../redux';
import SystemWizardDetail from './SystemWizardDetail';


const systemManager = new SystemManager();

/**
 * List of connector types.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
class ConnectorTypes extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.context.store.dispatch(systemManager.fetchSupportedTypes());
  }

  onChooseConnector(connectorType) {
    this.setState({
      showLoading: true
    }, () => {
      systemManager.getService().loadConnectorType({reopened: false, ...connectorType})
        .then((_connectorType) => {
          _connectorType.reopened = false;
          this.setState({showLoading: false, showWizard: true, connectorType: _connectorType});
        })
        .catch(ex => {
          this.setState({
            showLoading: false
          });
          this.addError(ex);
        });
    });
  }

  renderCards(connectorTypeData) {
    const results = [];
    // sort by order
    connectorTypeData.sort((a, b) => {
      return a.order - b.order;
    });
    connectorTypeData.forEach(connectorType => {
      let title = this._getConnectorLabel(connectorType.id, connectorType.module);
      const hasNotTitle = title === connectorType.id;
      if (hasNotTitle) {
        title = connectorType.name;
      }
      let description = this._getConnectorDescription(connectorType.id, connectorType.module);
      description = description || connectorType.version;

      results.push(
        <Basic.Panel
          level="primary"
          className="panel-hover"
          style={{width: 220, maxWidth: 230, marginRight: 15}}
          onClick={this.onChooseConnector.bind(this, connectorType)}>
          <Basic.PanelHeader style={{textAlign: 'center'}}>
            <Basic.ShortText
              value={ title }
              cutChar=""
              maxLength="25"/>
          </Basic.PanelHeader>
          <Basic.PanelBody style={{textAlign: 'center', height: 180}}>
            <Basic.Icon
              type="component"
              icon={connectorType.iconKey}/>
            <small className="help-block">
              <Basic.ShortText
                value={ description }
                cutChar=""
                maxLength="58"/>
            </small>
          </Basic.PanelBody>
          <Basic.PanelFooter
            rendered={false}
            style={{textAlign: 'center'}}>
            <Basic.Button
              level="primary"
              buttonSize="xs"
              onClick={this.onChooseConnector.bind(this, connectorType)}
              title={this.i18n('acc:connector-type.buttonTitle')}>
              <Basic.Icon
                icon="fa:magic"
                style={{marginRight: 5}}/>
              {`${this.i18n('acc:connector-type.wizardStart')}`}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      );
    });

    return (
      <Basic.Div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'flex-end' }}>
        {results}
      </Basic.Div>
    );
  }

  renderConnectorTypes() {
    const {supportedTypes} = this.props;
    const connectorTypeData = [];

    if (supportedTypes) {
      supportedTypes.forEach(type => {
        connectorTypeData.push({...type});
      });
    }

    return (
      <Basic.Div>
        {this.renderCards(connectorTypeData)}
        <Basic.Table
          rendered={false}
          data={connectorTypeData}
          className="table-cell-vertical-middle-align"
        >
          <Basic.Column
            property="button"
            header=" "
            cell={
              ({rowIndex, data}) => {
                const connectorType = data[rowIndex];
                return (
                  <Basic.Button
                    level="default"
                    style={{padding: 5}}
                    onClick={this.onChooseConnector.bind(this, connectorType)}
                    title={this.i18n('acc:connector-type.buttonTitle')}>
                    <Basic.Icon type="component" icon={connectorType.iconKey}/>
                  </Basic.Button>
                );
              }
            }
          />
          <Basic.Column
            property="id"
            header={this.i18n('acc:connector-type.name')}
            cell={
              ({rowIndex, data}) => {
                const connectorType = data[rowIndex];
                const title = this._getConnectorLabel(connectorType.id, connectorType.module);
                const hasNotTitle = title === connectorType.id;
                if (hasNotTitle) {
                  return connectorType.name;
                }

                return (
                  <Basic.Alert
                    level={hasNotTitle ? 'default' : 'info'}
                    style={{margin: 0}}
                    showHtmlText
                    title={title}
                    text={this._getConnectorDescription(connectorType.id, connectorType.module)}
                  />
                );
              }
            }
          />
          <Basic.Column
            property="local"
            face="bool"
            header={this.i18n('acc:connector-type.local')}
            cell={
              ({rowIndex, data}) => {
                const connectorType = data[rowIndex];
                return <Basic.BooleanCell
                  propertyValue={ !!connectorType.local }
                  className="column-face-bool"/>;
              }
            }
          />
          <Basic.Column
            property="version"
            header={this.i18n('acc:connector-type.version')}
          />
        </Basic.Table>
      </Basic.Div>
    );
  }

  _getConnectorLabel(id, module) {
    const locKey = `wizard.${id}.name`;
    let label = this.i18n(`${module}:${locKey}`);
    if (label === locKey) {
      label = id;
    }
    return label;
  }

  _getConnectorDescription(id, module) {
    const locKey = `wizard.${id}.description`;
    let label = this.i18n(`${module}:${locKey}`);
    if (label === locKey) {
      label = '';
    }
    return label;
  }

  closeWizard(finished, wizardContext) {
    this.setState({
      showWizard: false
    }, () => {
      if (finished && wizardContext && wizardContext.entity) {
        this.context.history.push(`/system/${wizardContext.entity.id}/detail`);
      }
    });
  }

  render() {
    const {showWizard, connectorType} = this.state;
    const wizardContext = this.wizardContext;

    return (
      <IdmContext.Provider value={{...this.context, wizardContext}}>
        <Basic.Panel>
          <Helmet title={ this.i18n('acc:connector-type.header') } />
          <Basic.PanelHeader text={this.i18n('acc:connector-type.header')}/>
          <Basic.PanelBody>
            {this.renderConnectorTypes()}
          </Basic.PanelBody>
        </Basic.Panel>
        <Basic.Div rendered={showWizard}>
          <SystemWizardDetail
            match={ this.props.match }
            closeWizard={this.closeWizard.bind(this)}
            connectorType={connectorType}
            show={showWizard}/>
        </Basic.Div>
      </IdmContext.Provider>
    );
  }
}

ConnectorTypes.defaultProps = {
  showConnectors: true
};

function select(state) {
  return {
    supportedTypes: Managers.DataManager.getData(state, SystemManager.UI_KEY_SUPPORTED_TYPES)
  };
}

export default connect(select)(ConnectorTypes);
