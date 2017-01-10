import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { RuleManager } from '../../redux';
import RuleDetail from './RuleDetail';

const ruleManager = new RuleManager();

/**
 * Rule detail content, there is difference between create new rule and edit.
 * If set params new is new :-).
 */
class RuleContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.rules';
  }

  componentDidMount() {
    this.selectNavigationItem('rules');
    const { entityId } = this.props.params;

    if (this._getIsNew()) {
      this.context.store.dispatch(ruleManager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(ruleManager.fetchEntity(entityId));
    }
  }

  /**
   * Function check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:clone"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create.header')
              :
              <span>{entity.name} <small>{this.i18n('edit.header')}</small></span>
            }
          </Basic.PageHeader>
        }

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <RuleDetail entity={entity} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

RuleDetail.propTypes = {
  showLoading: PropTypes.bool
};
RuleDetail.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: ruleManager.getEntity(state, entityId),
    showLoading: ruleManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(RuleContent);
