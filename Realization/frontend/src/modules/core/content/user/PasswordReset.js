'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { Link }  from 'react-router';
import { connect } from 'react-redux';
import classnames from 'classnames';
import _ from 'lodash';
import Immutable from 'immutable';
//
import * as Basic from '../../../../components/basic'
import { IdentityService } from '../../../../modules/core/services';
import { SettingManager } from '../../../../redux';
import { SecurityManager, DataManager, IdentityManager } from '../../redux';

const RESOURCE_IDM = 'czechidm';

class PasswordReset extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      identityResources: new Immutable.OrderedMap(),
      bulkLoading: false,
      bulkCounter: 0,
      bulkCount: 0,
      bulkAction: null
    }
    this.identityManager = new IdentityManager();
  }

  getContentKey() {
    return 'content.user.passwordReset';
  }

  componentDidMount() {
    const { usernames } = this.props;
    //
    this.selectNavigationItem('users');
    // load selected users accounts
    this._loadIdentityResources(usernames, 0);
  }

  _loadIdentityResources(usernames, index) {
    if (usernames.length < index + 1) {
      return;
    }
    return this.identityManager.getService().getAccounts(usernames[index])
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        let resources =  new Immutable.Map();
        resources = resources.set(RESOURCE_IDM, true); // TODO: default selection
        json._embedded.forEach(account => {
          resources = resources.set(account.resource, true); // TODO: default selection
        });
        this.setState({
          identityResources: this.state.identityResources.set(usernames[index], resources)
        }, () => {
          // next
          this._loadIdentityResources(usernames, index + 1);
        });
      } else {
        this.addErrorMessage({ title: this.i18n('message.loadResourcesFailed', { username: usernames[index] }) }, json.error);
      }
      return json;
    }).catch(error => {
      this.addErrorMessage({ title: this.i18n('message.loadResourcesFailed', { username: usernames[index] }) }, error);
    });
  }

  /**
   * Select resource for identity for pessword reset
   *
   * @param  {string} username
   * @param  {string} resource
   * @param  {event} event - checkbox event
   */
  onResourceSelect(username, resource, event) {
    this.setState({
      identityResources: this.state.identityResources.setIn([username, resource], event.currentTarget.checked)
    });
  }

  /**
   * Select all resources on username
   *
   * @param  {string} resource
   * @param  {event} event - checkbox event
   */
  onAllResourceSelect(resource, event) {
    const { identityResources }  = this.state;
    let newIdentityResources = new Immutable.OrderedMap();
    identityResources.forEach((resources, username) => {
      if (resources.has(resource)) {
        newIdentityResources = newIdentityResources.set(username, resources.set(resource, event.currentTarget.checked));
      } else {
        // preserve previous state
        newIdentityResources = newIdentityResources.set(username, resources);
      }
    });
    this.setState({
      identityResources: newIdentityResources
    });
  }

  onAllSelect(event) {
    let identityResources = new Immutable.OrderedMap();
    this.state.identityResources.keySeq().forEach(username => {
      let resources =  new Immutable.Map();
      this.state.identityResources.get(username).forEach((value, resource) => {
        resources = resources.set(resource, event.currentTarget.checked);
      });
      identityResources = identityResources.set(username, resources);
    });
    this.setState({
      identityResources: identityResources
    });
  }

  isAllChecked() {
    let checked = true;
    this.state.identityResources.keySeq().forEach(username => {
      checked = checked && this.isAllUsernameChecked(username);
    });
    return checked;
  }

  onAllUsernameSelect(username, event) {
    let resources =  new Immutable.Map();
    this.state.identityResources.get(username).forEach((value, resource) => {
      resources = resources.set(resource, event.currentTarget.checked);
    });
    this.setState({
      identityResources: this.state.identityResources.set(username, resources)
    });
  }

  isAllUsernameChecked(username) {
    return this.state.identityResources.get(username).reduce((result, value) => {
        return result && value;
    });
  }

  isAllResourceChecked(resource) {
    const { identityResources }  = this.state;
    let checked = true;
    identityResources.forEach((resources, username) => {
      if (resources.has(resource)) {
        checked= checked && resources.get(resource);
      }
    });
    return checked;
  }

  isSomeSelected() {
    const { identityResources }  = this.state;
    let checked = false;
    identityResources.forEach((resources, username) => {
      resources.forEach(resource => {
        checked = checked || resource;
      })
    });
    return checked;
  }

  //Uživatelům bylo resetováno heslo ve vybraných systémech

  onPasswordReset() {
    let { identityResources }  = this.state;
    // remove not selected usernnames
    identityResources = identityResources.filter(resources => {
      return resources.some(value => {
        return value;
      });
    });
    //
    this.refs['confirm'].show(
      this.i18n('confirm.message', { count: identityResources.size, username: identityResources.keySeq().toArray()[0] }),
      this.i18n('confirm.title')
    ).then(result => {
      // TODO: long runnig task on server
      this.setState({
        bulkAction: 'password-reset',
        bulkLoading: true,
        bulkCounter: 0,
        bulkCount: identityResources.size
      }, () => {
        this._resetPassword(identityResources, 0);
      });
    }, (err) => {
      // nothing
    });
  }

  _resetPassword(identityResources, index) {
    const usernames = identityResources.keySeq().toArray();
    if (usernames.length < index + 1) {
      stopBulkAction();
      return;
    }
    const passwordResetDto = {
      identity: usernames[index],
      idm: identityResources.get(usernames[index]).get(RESOURCE_IDM),
      resources: identityResources.get(usernames[index])
        .filter((value, resource) => {
          return value && resource !== RESOURCE_IDM;
        })
        .map((value, resource) => {
          return resource;
        })
        .toArray()
    };
    return this.identityManager.getService().passwordReset(usernames[index], passwordResetDto)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        const { bulkCounter } = this.state;
        this.setState({
          bulkCounter: bulkCounter + 1
        }, () => {
          // next
          if (usernames.length > index + 1) {
            this._resetPassword(identityResources, index + 1);
          } else {
            this.addMessage({ message: this.i18n('message.resetSuccess', { usernames: usernames.join(', ')}) });
            this.stopBulkAction();
          }
        });
      } else {
        this.addErrorMessage({ title: this.i18n('message.resetFailed', { username: usernames[index]}) }, json.error);
        this.stopBulkAction();
      }
      return json;
    }).catch(error => {
      this.addErrorMessage({ title: this.i18n('message.resetFailed', { username: usernames[index]}) }, error);
      this.stopBulkAction();
    });
  }

  stopBulkAction() {
    this.setState({
      bulkLoading: false,
      bulkCounter: 0,
    });
  }

  render() {
    const { usernames } = this.props;
    const { identityResources, bulkAction, bulkCounter, bulkCount, bulkLoading } = this.state;
    //
    if (identityResources.size !== usernames.length) {
      return (
        <Basic.Panel>
          <Basic.PanelHeader text={this.i18n('header')}/>
          <Basic.Loading isStatic showLoading={true}/>
        </Basic.Panel>
      );
    }
    // set of resource names
    let resources = new Immutable.OrderedSet();
    resources = resources.add(RESOURCE_IDM);
    identityResources.forEach((_resources) => {
      _resources.forEach((value, resource) => {
        resources = resources.add(resource);
      });
    });

    let data = [];
    usernames.forEach(username => {
      let row = {
        username: username
      };
      resources.forEach(resource => {
        _.merge(row, {
          [resource]: resource
        });
      });
      data.push(row);
    });

    let columns = [
      <Basic.Column
        property="username"
        header={() => {
          return (
            <div className="checkbox" style={{ margin: 0 }}>
              <label
                style={{ fontWeight: 'bold' }}
                title={this.i18n('label.selectAll')}>
                <input
                  type="checkbox"
                  checked={this.isAllChecked()}
                  onChange={this.onAllSelect.bind(this)}/>
                {this.i18n('entity.Identity._type')}
              </label>
            </div>
          );
        }}
        cell={({ property, rowIndex, data }) => {
          return (
            <div className="checkbox" style={{ margin: 0 }}>
              <label title={this.i18n('label.selectAllUsername')}>
                <input type="checkbox" checked={this.isAllUsernameChecked(data[rowIndex][property])} onChange={this.onAllUsernameSelect.bind(this, data[rowIndex][property])}/>
                {data[rowIndex][property]}
              </label>
            </div>
          );
        }}
        width="200px"/>
    ];
    resources.forEach(resource => {
      columns.push(
        <Basic.Column property={resource}
          header={({ property }) => {
            return (
              <div className="checkbox" style={{ margin: 0 }}>
                <label
                  title={this.i18n('label.selectAllResource')}
                  style={{ fontWeight: 'bold' }}>
                  <input type="checkbox" checked={this.isAllResourceChecked(resource)} onChange={this.onAllResourceSelect.bind(this, resource)}/>
                  {property}
                </label>
              </div>
            );
          }}
          cell={({ rowIndex, data }) => {
            if (identityResources.get(data[rowIndex].username).has(resource)) {
              return (
                <input type="checkbox" checked={identityResources.get(data[rowIndex].username).get(resource)} onChange={this.onResourceSelect.bind(this, data[rowIndex].username, resource)}/>
              );
            }
            return (
              <span title={this.i18n('accounts.empty', { username: data[rowIndex].username, resource: resource } )}>&nbsp;-&nbsp;</span>
            );
          }}
        />);
    });

    let deactivateLabel = (<span style={{color: '#000'}}>{this.i18n('component.basic.ProgressBar.start')}</span>);
    if (bulkCounter > 0) {
      deactivateLabel = this.i18n('component.basic.ProgressBar.processed') + ' %(now)s / %(max)s';
    }

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm" level="danger"/>

        <Basic.Panel>
          <Basic.PanelHeader text={this.i18n('header')}/>

          <Basic.Alert icon="info-sign" text={this.i18n('info')} rendered={data.length > 0}/>

          <Basic.Table
            data={data}
            noData={
              <span>{this.i18n('users.empty')}</span>
            }>
            {columns}
          </Basic.Table>

          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              level="danger"
              onClick={this.onPasswordReset.bind(this)}
              disabled={!this.isSomeSelected()}
              title={!this.isSomeSelected() ? this.i18n('selection.empty') : ''}
              titlePlacement="top">
              {this.i18n('button.reset')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>

        <Basic.Modal
          show={bulkLoading}
          bsSize="large" backdrop="static">
          <Basic.Modal.Header text={this.i18n('header', { count: bulkCount})}/>
          <Basic.Modal.Body>
            <Basic.ProgressBar min={0} max={bulkCount} now={bulkCounter} label={deactivateLabel} active style={{ marginBottom: 0}}/>
          </Basic.Modal.Body>
        </Basic.Modal>
      </div>
    );
  }
}

PasswordReset.propTypes = {
  usernames: PropTypes.array
}
PasswordReset.defaultProps = {
  usernames: ['cfhh', 'autoone', 'rtone']
}

function select(state) {
  return {
    usernames: DataManager.getData(state, 'selected-usernames') || []
  }
}

export default connect(select)(PasswordReset);
