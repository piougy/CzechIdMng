import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { DataManager } from '../../../redux';
import EavForm from './EavForm';

/**
 * Content with eav form
 *
 * @author Radek Tomiška
 */
class EavContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      error: null
    };
  }

  getComponentKey() {
    return this.props.contentKey;
  }

  componentDidMount() {
    super.componentDidMount();
    // load definition and values
    const { entityId, formableManager, uiKey } = this.props;
    //
    this.context.store.dispatch(formableManager.fetchFormInstance(entityId, `${uiKey}-${entityId}`, (formInstance, error) => {
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
    const { entityId, formableManager, uiKey } = this.props;
    //
    const filledFormValues = this.refs.eav.getValues();
    this.getLogger().debug(`[EavForm]: Saving form [${this.refs.eav.getFormDefinition().type}|${this.refs.eav.getFormDefinition().name}]`);
    // save values
    this.context.store.dispatch(formableManager.saveFormValues(entityId, filledFormValues, `${uiKey}-${entityId}`, (savedFormInstance, error) => {
      if (error) {
        this.addError(error);
      } else {
        const entity = formableManager.getEntity(this.context.store.getState(), entityId);
        this.addMessage({ message: this.i18n('save.success', { name: formableManager.getNiceLabel(entity) }) });
        this.getLogger().debug(`[EavForm]: Form [${this.refs.eav.getFormDefinition().type}|${this.refs.eav.getFormDefinition().name}] saved`);
      }
    }));
  }

  render() {
    const { _formInstance, _showLoading, showSaveButton } = this.props;
    const { error } = this.state;

    let content;
    if (error) {
      // connector is wrong configured
      content = (
        <Basic.Alert level="info" text={this.i18n('error.notFound')}/>
      );
    } else if (!_formInstance || _showLoading) {
      // connector eav form is loaded from BE
      content = (
        <Basic.Loading isStatic showLoading/>
      );
    } else {
      // connector setting is ready
      content = (
        <form className="abstract-form" onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">

            <EavForm ref="eav" formInstance={_formInstance} readOnly={!showSaveButton}/>

            <Basic.PanelFooter rendered={showSaveButton}>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ _formInstance.getAttributes().size > 0 }
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
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

EavContent.propTypes = {
  /**
   * UI identifier - it's used as key in store (saving, loading ...)
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Parent entity identifier
   */
  entityId: PropTypes.string.isRequired,
  formableManager: PropTypes.object.isRequired,
  showSaveButton: PropTypes.bool,
  /**
   * Internal properties (loaded by redux)
   */
  _formInstance: PropTypes.object,
  _showLoading: PropTypes.bool
};
EavContent.defaultProps = {
  showSaveButton: false,
  _formInstance: null,
  _showLoading: false
};

function select(state, component) {
  const entityId = component.entityId;
  const uiKey = component.uiKey;
  //
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${entityId}`),
    _formInstance: DataManager.getData(state, `${uiKey}-${entityId}`)
  };
}

export default connect(select)(EavContent);
