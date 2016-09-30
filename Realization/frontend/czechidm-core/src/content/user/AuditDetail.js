import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { IdentityManager, DataManager } from '../../redux';
import IdentityDetail from './IdentityDetail';
import * as Advanced from '../../components/advanced';

const identityManager = new IdentityManager();

const uiKey = 'audit-';

class AuditDetail extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    const { userID, revID } = this.props.params;
    this.selectSidebarItem('profile-audit-profile-personal');
    this.context.store.dispatch(identityManager.fetchRevision(userID, revID, uiKey + revID));
  }

  componentDidUpdate() {
  }

  render() {
    const { auditIdentity, showLoading } = this.props;
    const { userID } = this.props.params;
    return (
      <div>
          <Helmet title={this.i18n('navigation.menu.audit.profile')} />
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !auditIdentity
            ||
            <div>
              <Basic.PageHeader>
              {identityManager.getNiceLabel(auditIdentity.entity)} <small> {this.i18n('content.audit.profile.userDetail')} <Advanced.DateValue value={auditIdentity.metadata.delegate.revisionDate} showTime/> </small>
              </Basic.PageHeader>
              <Basic.Panel>
                <Basic.PanelHeader text={<span>{identityManager.getNiceLabel(auditIdentity.entity)} <small> Detail uživatele</small></span>} className="hidden">
                </Basic.PanelHeader>
                <div className="tab-vertical clearfix">
                  <Advanced.TabPanel parentId="profile-audit" params={this.props.params}>
                    <IdentityDetail identity={auditIdentity.entity} userID={userID} readOnly />
                  </Advanced.TabPanel>
                </div>
              </Basic.Panel>
            </div>
          }
      </div>
    );
  }
}

AuditDetail.propTypes = {
  showLoading: PropTypes.bool
};

AuditDetail.defaultProps = {
  showLoading: false
};

function select(state, component) {
  const { revID } = component.params;
  return {
    auditIdentity: DataManager.getData(state, uiKey + revID),
    showLoading: identityManager.isShowLoading(state, uiKey + revID)
  };
}

export default connect(select)(AuditDetail);
