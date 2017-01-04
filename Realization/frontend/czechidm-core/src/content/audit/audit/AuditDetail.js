import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import moment from 'moment';
//
import { AuditManager, DataManager } from '../../../redux';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import AuditDetailTable from '../audit/AuditDetailTable';

const auditManager = new AuditManager();

const AUDIT_DETAIL_DIFF = 'auditDiff';

/**
 * Audit detail content
 *
 * TODO: Add better fieldLabel to select box
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
    this.context.store.dispatch(auditManager.fetchEntity(entityId));
    if (revID) {
      this.context.store.dispatch(auditManager.fetchEntity(revID, null, (selectItem) => {
        if (this.refs.revisionDiff) {
          this.refs.revisionDiff.setValue(selectItem);
        }
      }));
      this.context.store.dispatch(auditManager.fetchDiffBetweenVersion(entityId, revID, AUDIT_DETAIL_DIFF));
    }
  }

  changeSecondRevision(rev) {
    const { entityId } = this.props.params;
    if (rev) {
      this.context.router.push(`/audit/entities/${entityId}/diff/${rev.id}`);
    } else {
      this.context.router.push(`/audit/entities/${entityId}/diff`);
    }
  }

  _transformLabelForCheckBox(item) {
    return item.id + ' (' + moment(item.revisionDate).format('d. M. Y  H:mm:ss') + ')';
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
        niceLabelTransform={this._transformLabelForCheckBox}
        manager={auditManager}/>
    );
  }

  render() {
    const {
      auditDetailFirst,
      auditDetailSecond, diffValues } = this.props;

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

        <Basic.Panel >
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
                {
                  !auditDetailFirst
                  ||
                  <div className="col-md-6">
                    <big>
                      {this.i18n('revision.id') + ' '}<span className="pull-right">{auditDetailFirst.id}</span>
                      <br/>
                      {this.i18n('revision.modifier') + ' '}<span className="pull-right">{auditDetailFirst.modifier}</span>
                      <br/>
                      {this.i18n('revision.revisionDate') + ' '}
                      <span className="pull-right">
                        <Advanced.DateValue
                          value={auditDetailFirst.revisionDate}
                          format="d. M. Y  H:mm:ss"/>
                      </span>
                    </big>
                  </div>
                }
                {
                  !auditDetailSecond
                  ||
                  <div className="col-md-6 last">
                    <big>
                      {this.i18n('revision.id') + ' '}<span className="pull-right">{auditDetailSecond.id}</span>
                      <br/>
                      {this.i18n('revision.modifier') + ' '}<span className="pull-right">{auditDetailSecond.modifier}</span>
                      <br/>
                      {this.i18n('revision.revisionDate') + ' '}
                        <span className="pull-right">
                          <Advanced.DateValue
                            value={auditDetailSecond.revisionDate}
                            format="d. M. Y  H:mm:ss"/>
                        </span>
                    </big>
                  </div>
                }
              </div>
            </Basic.Row>
            <AuditDetailTable detail={auditDetailFirst} />
            {
              !diffValues
              ||
              <AuditDetailTable
                detail={auditDetailSecond}
                diffValues={diffValues.diffValues}/>
            }
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
    diffValues: DataManager.getData(state, AUDIT_DETAIL_DIFF)
  };
}

export default connect(select)(AuditDetail);
