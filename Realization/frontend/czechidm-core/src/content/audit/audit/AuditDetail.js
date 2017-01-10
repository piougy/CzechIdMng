import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import { AuditManager, DataManager } from '../../../redux';
import * as Basic from '../../../components/basic';
import AuditDetailTable from '../audit/AuditDetailTable';
import AuditDetailInfo from './AuditDetailInfo';

const auditManager = new AuditManager();

/**
 * uiKey for diff detail values
 */
const AUDIT_DETAIL_DIFF = 'auditDiff';

/**
 * uiKey for previous version
 */
const AUDIT_PREVIOUS_VERSION = 'auditPreviousVersion';

const FIRST_ENTITY_UIKEY = 'firstEntityUiKey';
const SECOND_ENTITY_UIKEY = 'secondEntityUiKey';

/**
 * Audit detail content
 *
 */
class AuditDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    this.selectSidebarItem('audit-entities');
    this._reloadComponent(this.props);
  }

  /**
   * After change props is necessary to reload tables with data
   */
  componentWillReceiveProps(nextProps) {
    const { entityId, revID } = this.props.params;

    if (entityId !== nextProps.params.entityId || revID !== nextProps.params.revID) {
      this._reloadComponent(nextProps);
    }
  }

  _reloadComponent(props) {
    const { entityId, revID } = props.params;
    this.context.store.dispatch(auditManager.fetchEntityIfNeeded(entityId, FIRST_ENTITY_UIKEY));
    if (revID) {
      this.context.store.dispatch(auditManager.fetchEntityIfNeeded(revID, SECOND_ENTITY_UIKEY, (selectItem) => {
        if (this.refs.revisionDiff) {
          this.refs.revisionDiff.setValue(selectItem);
        }
      }));
      this.context.store.dispatch(auditManager.fetchDiffBetweenVersion(entityId, revID, AUDIT_DETAIL_DIFF));
    } else {
      this.context.store.dispatch(auditManager.fetchPreviousVersion(entityId, AUDIT_DETAIL_DIFF, (previousVersion) => {
        if (this.refs.revisionDiff && previousVersion) {
          this.refs.revisionDiff.setValue(previousVersion);
          this.context.router.replace(`/audit/entities/${entityId}/diff/${previousVersion.id}`);
        }
      }));
    }
  }

  changeSecondRevision(rev) {
    const { entityId } = this.props.params;
    if (rev) {
      this.context.router.replace(`/audit/entities/${entityId}/diff/${rev.id}`);
    } else {
      this.context.router.replace(`/audit/entities/${entityId}/diff`);
    }
  }

  _getSelectBoxWithRevision() {
    const { auditDetailFirst } = this.props;

    return (
      <Basic.SelectBox
        ref="revisionDiff"
        label={this.i18n('pickRevision')}
        labelSpan=""
        componentSpan=""
        onChange={this.changeSecondRevision.bind(this)}
        forceSearchParameters={auditManager.getDefaultSearchParameters().setFilter('entityId', auditDetailFirst ? auditDetailFirst.entityId : null)}
        manager={auditManager}/>
    );
  }

  /**
   * Method check if version types are same
   */
  _sameType(firstVersion, secondRevision) {
    return firstVersion && secondRevision && firstVersion.entityId === secondRevision.entityId;
  }

  render() {
    const {
      auditDetailFirst,
      auditDetailSecond, diffValues,
      showLoadingFirstDetail } = this.props;

    return (
      <Basic.Row>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="eye-open"/>
          {' '}
          {this.i18n('header')}
          {' '}
          <small>{this.i18n('detail')}</small>
        </Basic.PageHeader>

        <Basic.Panel>
          <div className="col-lg-12">
            <Basic.Row>
              <div className="col-md-6 pull-right" style={ {marginTop: '15px', marginBottom: '15px' } }>
                {
                  this._getSelectBoxWithRevision()
                }
              </div>
            </Basic.Row>
            <Basic.Row >
              <div className="col-md-12">
                <div className="col-md-6">
                  <AuditDetailInfo auditDetail={auditDetailFirst} showLoading={showLoadingFirstDetail}/>
                </div>
                <div className="col-md-6 last">
                  <AuditDetailInfo auditDetail={auditDetailSecond}/>
                </div>
              </div>
            </Basic.Row>
            <AuditDetailTable detail={auditDetailFirst} showLoading={showLoadingFirstDetail} />

            <AuditDetailTable
              detail={this._sameType(auditDetailFirst, auditDetailSecond) ? auditDetailSecond : null}
              diffValues={diffValues ? diffValues.diffValues : null}/>

          </div>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </Basic.Row>
    );
  }
}

AuditDetail.propTypes = {
  auditDetailFirst: PropTypes.object
};

AuditDetail.defaultProps = {
};

function select(state, component) {
  const { entityId, revID } = component.params;

  return {
    userContext: state.security.userContext,
    auditDetailFirst: auditManager.getEntity(state, entityId),
    auditDetailSecond: auditManager.getEntity(state, revID),
    previousVersion: DataManager.getData(state, AUDIT_PREVIOUS_VERSION),
    diffValues: DataManager.getData(state, AUDIT_DETAIL_DIFF),
    showLoadingFirstDetail: auditManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(AuditDetail);
