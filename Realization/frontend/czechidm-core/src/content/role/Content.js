import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import RoleDetail from './RoleDetail';
import RoleTypeEnum from '../../enums/RoleTypeEnum';

const roleManager = new RoleManager();

class Content extends Basic.AbstractContent {

  constructor(props) {
    super(props);
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    const isNew = this._getIsNew();
    this.selectNavigationItems(['roles', 'role-detail']);
    if (isNew) {
      this.context.store.dispatch(roleManager.receiveEntity(entityId, { roleType: RoleTypeEnum.findKeyBySymbol(RoleTypeEnum.TECHNICAL) }));
    } else {
      this.context.store.dispatch(roleManager.fetchEntity(entityId));
    }
  }

  componentDidUpdate() {
  }

  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { role, showLoading } = this.props;
    if (this._getIsNew()) {
      return (
        <Basic.Row>
          <div className="col-lg-offset-1 col-lg-10">
            <Helmet title={this.i18n('create.header')} />
              {
              !role
              ||
              <Basic.Panel>
                <Basic.PanelHeader text={this.i18n('create.header')} />
                <div style={ {margin: 10 }}>
                  <RoleDetail entity={role} showLoading={showLoading} isNew={this._getIsNew()} />
                </div>
              </Basic.Panel>
              }
            </div>
          </Basic.Row>
      );
    }
    return (
      <div>
      {
        !role
        ||
        <Basic.Row>
          <Basic.Panel className="col-lg-12 no-border last">
            <RoleDetail entity={role} showLoading={showLoading} isNew={this._getIsNew()} />
          </Basic.Panel>
        </Basic.Row>
      }
      </div>
    );
  }
}
Content.propTypes = {
};

Content.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    role: roleManager.getEntity(state, entityId),
    showLoading: roleManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(Content);
