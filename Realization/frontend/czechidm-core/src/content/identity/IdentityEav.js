import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager, DataManager } from '../../redux';
import * as Utils from '../../utils';

const uiKey = 'eav-identity-';
const manager = new IdentityManager();

/**
 * Extended identity attributes
 * TODO: could be imploded to one form - profile?
 */
class IdentityEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      error: null
    };
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.identity.eav';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-eav');
    // load definition and values
    const { entityId } = this.props.params;
    this.context.store.dispatch(manager.fetchFormInstance(entityId, `${uiKey}-${entityId}`, (formInstance, error) => {
      if (error) {
        this.addErrorMessage({ hidden: true, level: 'info' }, error);
        this.setState({ error });
      } else {
        this.getLogger().debug(`[EavForm]: Loaded form definition [${formInstance.getDefinition().type}|${formInstance.getDefinition().name}]`);
      }
    }));
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
    this.context.store.dispatch(manager.saveFormValues(entityId, filledFormValues, `${uiKey}-${entityId}`, (savedFormInstance, error) => {
      if (error) {
        this.addError(error);
      } else {
        const identity = manager.getEntity(this.context.store.getState(), entityId);
        this.addMessage({ message: this.i18n('save.success', { name: manager.getNiceLabel(identity) }) });
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
        <Basic.Alert level="info" text={this.i18n('error.notFound')}/>
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
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          { content }
        </Basic.Panel>
      </div>
    );
  }
}

IdentityEav.propTypes = {
  formInstance: PropTypes.oject,
  _showLoading: PropTypes.bool
};
IdentityEav.defaultProps = {
  formInstance: null,
  _showLoading: false
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${entityId}`),
    formInstance: DataManager.getData(state, `${uiKey}-${entityId}`)
  };
}

export default connect(select)(IdentityEav);
