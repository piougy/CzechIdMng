import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils, Managers } from 'czechidm-core';
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
    const { formInstance } = this.props;
    const { entityId } = this.props.params;
    const filledFormValues = [];
    let isAllValid = true;
    formInstance.getAttributes().forEach(attribute => {
      const formComponent = this.refs[attribute.name];
      if (!formComponent.validate()) {
        isAllValid = false;
      }
      let formValue = formInstance.getSingleValue(attribute.name);
      if (formValue === null) {
        // construct form value
        formValue = {
          formAttribute: formInstance.getAttributeLink(attribute.name)
        };
      }
      // set value by persistent type
      switch (attribute.persistentType) {
        case 'TEXT': {
          formValue.stringValue = formComponent.getValue();
          break;
        }
        case 'BOOLEAN': {
          formValue.booleanValue = formComponent.getValue();
          break;
        }
        default: {
          this.getLogger().warn(`[EavForm]: Persistent type [${attribute.persistentType}] is not supported and not be filled and send to BE!`);
        }
      }
      filledFormValues.push(formValue);
    });
    if (!isAllValid) {
      return;
    }
    this.getLogger().debug(`[EavForm]: Saving form [${formInstance.getDefinition().type}|${formInstance.getDefinition().name}]`);
    // save values
    this.context.store.dispatch(manager.saveConnectorConfiguration(entityId, filledFormValues, `${uiKey}-${entityId}`, (savedFormInstance, error) => {
      if (error) {
        this.addError(error);
      } else {
        const system = manager.getEntity(this.context.store.getState(), entityId);
        this.addMessage({ message: this.i18n('save.success', { name: system.name }) });
        this.getLogger().debug(`[EavForm]: Form [${formInstance.getDefinition().type}|${formInstance.getDefinition().name}] saved`);
      }
    }));
  }

  render() {
    const { formInstance, _showLoading } = this.props;
    const { error } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          {
            error
            ?
            <Basic.Alert level="info">
              {this.i18n(`${error.module}:error.${error.statusEnum}.message`, error.parameters)}
              <div style={{ marginTop: 15 }}>
                <Basic.Button level="info" onClick={this.showDetail.bind(this)}>
                  {this.i18n('button.showBasicInfo')}
                </Basic.Button>
              </div>
            </Basic.Alert>
            :
            <span>
              {
                !formInstance || _showLoading
                ?
                <Basic.Loading isStatic showLoading/>
                :
                <form className="form-horizontal" style={{ marginTop: 15, marginBottom: 15 }} onSubmit={this.save.bind(this)}>
                  <Basic.PanelBody>
                    {
                      formInstance.getAttributes().map(attribute => {
                        const formValue = formInstance.getSingleValue(attribute.name);
                        //
                        if (attribute.persistentType === 'TEXT') {
                          return (
                            <Basic.TextField
                              ref={attribute.name}
                              required={attribute.required}
                              label={attribute.displayName}
                              value={formValue ? formValue.stringValue : null}
                              helpBlock={attribute.description}/>
                          );
                        }
                        if (attribute.persistentType === 'BOOLEAN') {
                          return (
                            <Basic.Checkbox
                              ref={attribute.name}
                              label={attribute.displayName}
                              value={formValue ? formValue.booleanValue : (attribute.defaultValue === 'true')}
                              helpBlock={attribute.description}/>
                          );
                        }
                        return (
                          <div>Unimplemented persistentType: { attribute.persistentType }</div>
                        );
                      })
                    }
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
              }
            </span>
          }
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
  _showLoading: false,
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${entityId}`),
    formInstance: Managers.DataManager.getData(state, `${uiKey}-${entityId}`)
  };
}

export default connect(select)(SystemConnectorContent);
