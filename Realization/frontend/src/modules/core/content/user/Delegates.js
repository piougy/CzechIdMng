'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { Link }  from 'react-router';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { IdentityDelegateManager } from '../../../../redux';
import { IdentityManager } from '../../../../modules/core/redux';
import DelegateStateEnum from '../../enums/DelegateStateEnum';

const uiKey = 'identity-delegates';

class Delegates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityDelegateManager = new IdentityDelegateManager();
    this.identityManager = new IdentityManager();
    this.state = {
      showDetail: false,
      showLoading: false,
      showLoadingDelete: false,
      isDateUnlimited: true,
      detail: {
        delegate: null,
        from: null,
        till: null,
        _isDateUnlimited: true
      }
    }
  }

  getManager() {
    return this.identityDelegateManager;
  }

  getContentKey() {
    return 'content.user.delegates'
  }

  componentDidMount() {
    this.selectSidebarItem('profile-delegates');
    const { userID } = this.props.params;
    this.getManager().setUsername(userID);
    this.context.store.dispatch(this.getManager().getDelegates(uiKey));
  }

  openDetail() {
    this.setState({
      showDetail: true
    }, () => {
      this.refs.form.setData(this.state.detail);
    });
  }

  closeDetail() {
    this.setState({
      showDetail: false,
      detail: this.refs.form.getData() // persist to state - form will be reopened with previous data
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const { userID } = this.props.params;
    let newDelegate = this.refs.form.getData(); // TODO: multi identities + array + push + bulk
    newDelegate.identity = userID;
    if (newDelegate._isDateUnlimited) {
      newDelegate.from = null;
      newDelegate.till = null;
    }
    delete newDelegate._isDateUnlimited;
    // promisse
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    this.getManager().getService().createDelegate(newDelegate)
    .then(response => {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.addMessage({
          message: this.i18n('message.save.success', { delegate: newDelegate.delegate }),
        });
        // reload delegates
        this.closeDetail();
        this.context.store.dispatch(this.getManager().getDelegates(uiKey));
      } else {
        this.addError(json.error);
      }
    })
    .catch(error => {
      this.addError(error);
    });
  }

  onDeleteDelegate(delegate, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-delete'].show(
      this.i18n('message.delete.confirm.message', { delegate: delegate.delegate }),
      this.i18n('message.delete.confirm.title')
    ).then(result => {
      this.setState({
        showLoadingDelete: true
      }, () => {
        this.getManager().getService().deleteDelegate(delegate)
        .then(response => {
          this.setState({
            showLoadingDelete: false
          });
          if (response.status === 204) { // ok
            return {};
          }
          return response.json();
        })
        .then(json => {
          if (!json.error) {
            this.addMessage({
              message: this.i18n('message.delete.success', { delegate: delegate.delegate }),
            });
            // reload delegates
            this.context.store.dispatch(this.getManager().getDelegates(uiKey));
          } else {
            this.addError(json.error);
          }
        })
        .catch(error => {
          this.addError(error);
        });
      });
    }, (err) => {
      //Rejected
    });
  }

  onDateUnlimitedChange(event) {
    const isDateUnlimited = event ? event.currentTarget.checked : true;
    this.setState({
      isDateUnlimited: isDateUnlimited
    });
  }

  render() {
    const { _entities, _showLoading} = this.props;
    const { showDetail, detail, showLoading, showLoadingDelete, isDateUnlimited } = this.state;
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        {
          _showLoading
          ?
          <Basic.Loading showLoading={true} className="static"/>
          :
          <Basic.Panel className="no-border last">
            <Basic.Toolbar>
              <div className="pull-right">
                <Basic.Button className="btn-xs" onClick={this.openDetail.bind(this)}>
                  <Basic.Icon value="plus" />
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              </div>
              <div className="clearfix"></div>
            </Basic.Toolbar>
            <Basic.Table
              data={_entities}
              rowClass={({rowIndex, data}) => { return data[rowIndex]['state'] === DelegateStateEnum.findKeyBySymbol(DelegateStateEnum.DENIED) ? 'disabled' : ''}}
              noData={this.i18n('empty')}>
              <Basic.Column
                property="state"
                header={this.i18n('entity.Delegate.state')}
                cell={<Basic.EnumCell enumClass={DelegateStateEnum}/>}
              />
              <Basic.Column
                property="delegate"
                header={this.i18n('entity.Delegate.delegate')}
              />
              <Basic.Column
                property="from"
                header={this.i18n('entity.Delegate.from')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}
              />
              <Basic.Column
                property="till"
                header={this.i18n('entity.Delegate.till')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>
              <Basic.Column
                header={this.i18n('label.action')}
                className="action"
                cell={
                  ({rowIndex, data, property, ...props}) => {
                    return (
                      <Basic.Button
                        level="danger"
                        onClick={this.onDeleteDelegate.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.delete', { delegate: data[rowIndex].delegate })}
                        titlePlacement="bottom">
                        <Basic.Icon icon="trash"/>
                      </Basic.Button>
                    );
                  }
                }/>
            </Basic.Table>
          </Basic.Panel>
        }
        <Basic.Modal
          show={showDetail}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!showLoading}>
          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!showLoading} text={this.i18n('create.header')}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" >
                <Basic.SelectBox
                  ref="delegate"
                  service={this.identityManager.getService()}
                  searchInFields={['lastName', 'name','email']}
                  label={this.i18n('entity.Delegate.delegate')}
                  multiSelect={false}
                  required/>
                <Basic.Checkbox
                  ref="_isDateUnlimited"
                  label={this.i18n('entity.Delegate.isDateUnlimited')}
                  onChange={this.onDateUnlimitedChange.bind(this)}/>
                <Basic.DateTimePicker
                  mode="date"
                  ref="from"
                  label={this.i18n('entity.Delegate.from')}
                  hidden={isDateUnlimited}/>
                <Basic.DateTimePicker
                   mode="date"
                   ref="till"
                   label={this.i18n('entity.Delegate.till')}
                   hidden={isDateUnlimited}/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                rendered={!showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={showLoading}
                showLoadingIcon={true}
                showLoadingText={this.i18n('create.button.saving')}>
                {this.i18n('create.button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

        <Basic.Modal
          show={showLoadingDelete}
          backdrop="static"
          keyboard={false}>
          <Basic.Modal.Header text={this.i18n('delete.header')}/>
          <Basic.Modal.Body>
            <Basic.Loading isStatic showLoading={true}/>
          </Basic.Modal.Body>
        </Basic.Modal>

      </div>
    );
  }
}

Delegates.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object)
};
Delegates.defaultProps = {
  _showLoading: true,
  _entities: []
};

function select(state) {
  const identityDelegateManager = new IdentityDelegateManager('n/a'); // TODO: just for gain entities from store - move function to common manager
  return {
    _showLoading: identityDelegateManager.isShowLoading(state, uiKey),
    _entities: identityDelegateManager.getEntities(state, uiKey)
  };
}

export default connect(select)(Delegates);
