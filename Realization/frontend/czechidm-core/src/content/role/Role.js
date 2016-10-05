import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import * as Advanced from '../../components/advanced';


const manager = new RoleManager();

class Role extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  componentDidUpdate() {
  }

  render() {
    const { entity, showLoading } = this.props;

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.profile')} />

        <Basic.PageHeader showLoading={showLoading}>
          {manager.getNiceLabel(entity)} <small> {this.i18n('content.roles.edit.header')}</small>
        </Basic.PageHeader>

        <Basic.Panel>
          <div className="tab-vertical clearfix">
            <Advanced.TabPanel parentId="roles" params={this.props.params}>
              {this.props.children}
            </Advanced.TabPanel>
          </div>
        </Basic.Panel>
      </div>
    );
  }
}

Role.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
Role.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(Role);
