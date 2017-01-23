import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { BackendModuleManager, DataManager, SecurityManager } from '../../redux';
import * as Utils from '../../utils';


class BackendModules extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.backendModuleManager = new BackendModuleManager();
  }

  getContentKey() {
    return 'content.system.be-modules';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'modules', 'be-modules']);
    this.context.store.dispatch(this.backendModuleManager.fetchInstalledModules());
  }

  onEnable(entity, enable, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-${enable ? '' : 'de'}activate`].show(
      this.i18n(`action.${enable ? '' : 'de'}activate.message`, { count: 1, record: entity.name }),
      this.i18n(`action.${enable ? '' : 'de'}activate.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.backendModuleManager.setEnabled(entity.id, enable, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.${enable ? '' : 'de'}activate.success`, { count: 1, record: entity.name }) });
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }

  render() {
    const { installedModules, showLoading } = this.props;

    const _installedModules = [];
    if (installedModules) {
      installedModules.forEach(moduleDescriptor => {
        _installedModules.push(moduleDescriptor);
      });
    }
    _installedModules.sort((one, two) => {
      return one.id > two.id;
    });

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>

        <Basic.Table
          data={_installedModules}
          showLoading={showLoading}
          noData={this.i18n('component.basic.Table.noData')}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>

          <Basic.Column property="id" header={this.i18n('entity.Module.id')}/>
          <Basic.Column property="name" header={this.i18n('entity.Module.name')}/>
          <Basic.Column property="vendor" header={this.i18n('entity.Module.vendor')}/>
          <Basic.Column property="version" header={this.i18n('entity.Module.version')}/>
          <Basic.Column property="description" header={this.i18n('entity.Module.description')}/>
          <Basic.Column
            property="disabled"
            header={<Basic.Cell className="column-face-bool">{this.i18n('entity.Module.disabled')}</Basic.Cell>}
            cell={<Basic.BooleanCell className="column-face-bool"/>}
            width="100px"
            rendered={false}/>
          <Basic.Column
            header={this.i18n('label.action')}
            className="action"
            cell={
              ({rowIndex, data}) => {
                if (!data[rowIndex].disabled) {
                  return (
                    <Basic.Button
                      level="warning"
                      onClick={this.onEnable.bind(this, data[rowIndex], false)}
                      className="btn-xs"
                      title={this.i18n('button.deactivate')}
                      titlePlacement="bottom"
                      rendered={data[rowIndex].disableable}>
                      {this.i18n('button.deactivate')}
                    </Basic.Button>
                  );
                }
                return (
                  <Basic.Button
                    level="success"
                    onClick={this.onEnable.bind(this, data[rowIndex], true)}
                    className="btn-xs"
                    title={this.i18n('button.activate')}
                    titlePlacement="bottom">
                    {this.i18n('button.activate')}
                  </Basic.Button>
                );
              }
            }
            rendered={SecurityManager.hasAuthority('MODULE_WRITE')}/>
        </Basic.Table>
      </div>
    );
  }
}

BackendModules.propTypes = {
  userContext: PropTypes.object,
  installedModules: PropTypes.object,
  showLoading: PropTypes.bool
};
BackendModules.defaultProps = {
  userContext: null,
  installedModules: null,
  showLoading: true
};

function select(state) {
  return {
    userContext: state.security.userContext,
    installedModules: DataManager.getData(state, BackendModuleManager.UI_KEY_MODULES),
    showLoading: Utils.Ui.isShowLoading(state, BackendModuleManager.UI_KEY_MODULES)
  };
}

export default connect(select)(BackendModules);
