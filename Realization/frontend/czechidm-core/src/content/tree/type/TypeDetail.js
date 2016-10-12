import React, { PropTypes } from 'react';
import * as Basic from '../../../components/basic';
import { TreeTypeManager, SecurityManager } from '../../../redux';

/**
 * Type detail content
 */
export default class TypeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.treeTypeManager = new TreeTypeManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.tree.types';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('tree-types');

    if (entity !== undefined) {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    }
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
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();

    if (entity.id === undefined) {
      this.context.store.dispatch(this.treeTypeManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.treeTypeManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.goBack();
  }

  closeDetail() {
  }

  render() {
    const { uiKey } = this.props;
    const { showLoading } = this.state;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
            <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('TREETYPE_WRITE')} >
              <Basic.TextField
                ref="code"
                label={this.i18n('entity.TreeType.code')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.TreeType.name')}
                required/>
            </Basic.AbstractForm>

            <Basic.PanelFooter showLoading={showLoading} >
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('TREETYPE_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </form>
      </div>
    );
  }
}

TypeDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
TypeDetail.defaultProps = {
};
