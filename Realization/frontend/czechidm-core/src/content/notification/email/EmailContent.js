import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../../components/basic';
import { EmailManager } from '../../../redux';
import NotificationDetail from '../NotificationDetail';

const emailManager = new EmailManager();

/**
 * Email audit log detail content.
 *
 * @author Radek Tomiška
 */
class EmailContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.email';
  }

  componentDidMount() {
    this.selectNavigationItem('notification-emails');
    const { entityId } = this.props.match.params;
    //
    this.getLogger().debug(`[EmailContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(emailManager.fetchEntity(entityId));
  }

  render() {
    const { email, showLoading } = this.props;
    return (
      <Basic.Div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:envelope-o"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Loading isStatic showLoading={showLoading} />
        {
          !email
          ||
          <NotificationDetail notification={ email } showTopic={ false }/>
        }

      </Basic.Div>
    );
  }
}

EmailContent.propTypes = {
  email: PropTypes.object,
  showLoading: PropTypes.bool
};
EmailContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    email: emailManager.getEntity(state, entityId),
    showLoading: emailManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(EmailContent);
