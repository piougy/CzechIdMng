import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { IdentityManager, DataManager } from 'core/redux';
import IdentityDetail from './IdentityDetail';

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
    this.selectSidebarItem('profile-audit');
    this.context.store.dispatch(identityManager.fetchRevision(userID, revID, uiKey + revID));
  }

  componentDidUpdate() {
  }

  render() {
    const { auditIdentity, showLoading } = this.props;
    const { userID } = this.props.params;
    return (
      <div>
        <Basic.Loading isStatic showLoading={showLoading} />
        {
          !auditIdentity
          ||
          <div>
            <Basic.PanelHeader text={<span>{identityManager.getNiceLabel(auditIdentity.entity)} <small> Auditní log</small></span>} />
            <IdentityDetail identity={auditIdentity.entity} userID={userID} readOnly />
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
