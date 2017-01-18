import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import moment from 'moment';
//
import * as Basic from '../components/basic';
import help from './Messages_cs.md';

class Messages extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectNavigationItem('messages');
  }

  removeAllMessages() {
    this.context.store.dispatch(this.getFlashManager().removeAllMessages());
  }

  removeMessage(id) {
    this.context.store.dispatch(this.getFlashManager().removeMessage(id));
  }

  render() {
    const messages = [];
    const isEmpty = !this.props.messages || !this.props.messages.length;
    if (isEmpty) {
      messages.push(<Basic.Alert icon="ok" level="success" text={this.i18n('content.messages.empty')}/>);
    } else {
      for (let i = 0; i < this.props.messages.length; i++) {
        const m = this.getFlashManager().createMessage(this.props.messages[i]);
        //
        const titleWithDatetime = (
          <div>
            <small> {moment(m.date).format(this.i18n('format.datetime'))}</small>
            <div>{m.title}</div>
          </div>
        );

        const key = 'flash-message-' + m.id;
        messages.push(
          <Basic.Alert
            key={key}
            level={m.level}
            title={titleWithDatetime}
            text={m.message}
            onClose={this.removeMessage.bind(this, m.id)}/>
        );
      }
    }

    const panelText = (
      <span>
        <span>
          <Basic.Icon value="envelope"/>
          {' '}
          {this.i18n('content.messages.header')}
        </span>
        {
          isEmpty
          ||
          (
            this.props.maxHistory === messages.length
            ?
            <small> (posledních {this.props.maxHistory})</small>
            :
            <small> ({messages.length})</small>
          )
        }
      </span>
    );

    return (
      <Basic.Row>
        <div className="col-lg-offset-2 col-lg-8">
          <Helmet title={this.i18n('navigation.menu.messages')} />
          <Basic.Panel>
            <Basic.PanelHeader text={panelText} help={help}/>
            <Basic.Toolbar viewportOffsetTop={0} container={this} rendered={!isEmpty}>
              <div className="pull-right">
                <Basic.Button level="warning" className="btn-xs" onClick={this.removeAllMessages.bind(this)}>
                  {this.i18n('content.messages.button.removeAll')}
                </Basic.Button>
              </div>
              <div className="clearfix"/>
            </Basic.Toolbar>

            {messages}
          </Basic.Panel>
        </div>
      </Basic.Row>
    );
  }
}

Messages.propTypes = {
  maxHistory: React.PropTypes.number,
  messages: React.PropTypes.array
};
Messages.defaultProps = {
  maxHistory: 100,
  messages: []
};

function select(state) {
  return {
    maxHistory: state.messages.maxHistory,
    messages: state.messages.messages.reverse().toArray()
  };
}

export default connect(select)(Messages);
