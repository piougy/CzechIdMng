import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils } from 'czechidm-core';
import { VsRequestManager } from '../../redux';

const manager = new VsRequestManager();

/**
 * Virtual system request detail
 *
 * @author Vít Švanda
 */
class VsRequestDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      showLoading: false
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-request.detail';
  }

  componentDidMount() {
    const { entity } = this.props;
    // set loaded entity to form
    this.refs.form.setData(entity);
    this.refs.uid.focus();
  }

  /**
   * Component will receive new props, try to compare with actual,
   * then init form
   */
  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (entity && entity.id !== nextProps.entity.id) {
      this.refs.form.setData(nextProps.entity);
    }
  }

  /**
   * Relize request
   */
  realize(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    // get entity from form
    const entity = this.refs.form.getData();
    // check form validity
    if (!this.refs.form.isFormValid()) {
      return;
    }
    // ui redux store identifier
    const { uiKey } = this.props;
    //
    this.setState({
      showLoading: true
    }, () => {
      const saveEntity = {
        ...entity,
      };

      this.context.store.dispatch(manager.updateEntity(saveEntity, `${uiKey}-detail`, (patchedEntity, newError) => {
        this._afterSave(patchedEntity, newError, afterAction);
      }));
    });
  }

  /**
  * Cancel request
  */
  cancel(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
  }

  /**
   * `Callback` after save action ends
   */
  _afterSave(entity, error, afterAction = 'CLOSE') {
    this.setState({
      showLoading: false
    }, () => {
      if (error) {
        this.addError(error);
        return;
      }

      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      //
      if (afterAction === 'CLOSE') {
        // reload options with remote connectors
        this.context.router.replace(`example/products`);
      } else {
        this.context.router.replace(`example/product/${entity.id}/detail`);
      }
    });
  }

  render() {
    const { uiKey, entity, _permissions } = this.props;
    const { showLoading } = this.state;
    //
    return (
      <div>
        <Helmet title={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title') } />

          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic') } />

            <Basic.PanelBody
              style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }
              showLoading={ showLoading } >
              <Basic.AbstractForm
                ref="form"
                uiKey={ uiKey }
                readOnly >

                <Basic.Row>
                  <Basic.Col lg={ 2 }>
                    <Basic.TextField
                      ref="uid"
                      label={ this.i18n('vs:entity.VsRequest.uid.label') }
                      required
                      max={ 255 }/>
                  </Basic.Col>
                  <Basic.Col lg={ 10 }>
                    <Basic.TextField
                      ref="state"
                      label={this.i18n('vs:entity.VsRequest.state.label')}
                      required
                      max={ 255 }/>
                  </Basic.Col>
                </Basic.Row>

                <Basic.TextField
                  ref="systemId"
                  label={ this.i18n('vs:entity.VsRequest.system.label') }
                  required/>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={ this.context.router.goBack }>{ this.i18n('button.back') }</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={ this.i18n('button.realize') }
                onClick={ this.realize.bind(this, 'CONTINUE') }
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, _permissions) }
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={ this.cancel.bind(this, 'CONTINUE')}>{this.i18n('button.cancel') }</Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
      </div>
    );
  }
}

VsRequestDetail.propTypes = {
  /**
   * Loaded entity
   */
  entity: PropTypes.object,
  /**
   * Entity, permissions etc. fro this content are stored in redux under given key
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Logged identity permissions - what can do with currently loaded entity
   */
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
VsRequestDetail.defaultProps = {
  _permissions: null
};

function select(state, component) {
  if (!component.entity) {
    return {};
  }
console.log("dddddddsss", component.entity.id);
  console.log("ddddddd",  manager.getPermissions(state, null, component.entity.id));
  return {
    _permissions: manager.getPermissions(state, null, component.entity.id)
  };
}

export default connect(select)(VsRequestDetail);
