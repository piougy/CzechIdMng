import React, { PropTypes } from 'react';
import * as Basic from 'app/components/basic';
import EmailRecipient from './EmailRecipient';

/**
 * Email audit log detail content
 */
export default class EmailDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.email';
  }

  componentDidMount() {
    const { email } = this.props;
    const data = {
      ...email,
      subject: email.message.subject,
      textMessage: email.message.textMessage,
      htmlMessage: email.message.htmlMessage
    };
    this.refs.form.setData(data);
  }

  render() {
    const { email } = this.props;
    if (!email) {
      return null;
    }
    return (
      <div>
        <Basic.AbstractForm ref="form" className="form-horizontal">
          <Basic.DateTimePicker ref="created" label={this.i18n('entity.Email.created')} readOnly/>
          <Basic.DateTimePicker ref="sent" label={this.i18n('entity.Email.sent')} readOnly/>
          <Basic.LabelWrapper
            label={this.i18n('entity.Email.from')}>
            <EmailRecipient recipient={email.from} style={{ margin: '7px 0' }} identityOnly/>
          </Basic.LabelWrapper>

          <Basic.LabelWrapper
            label={this.i18n('entity.Email.recipients')}>
            {
              email.recipients.map(recipient => {
                return (
                  <EmailRecipient recipient={recipient} style={{ margin: '7px 0' }} identityOnly/>
                );
              })
            }
          </Basic.LabelWrapper>

          <Basic.TextField ref="subject" label={this.i18n('entity.Email.message.subject')} readOnly/>
          <Basic.TextArea ref="sentLog" label={this.i18n('entity.Email.sentLog')} readOnly/>
        </Basic.AbstractForm>
      </div>
    );
  }
}

EmailDetail.propTypes = {
  notification: PropTypes.object
};
EmailDetail.defaultProps = {
};
