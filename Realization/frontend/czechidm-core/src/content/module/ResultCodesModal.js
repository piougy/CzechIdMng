import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { BackendModuleManager, DataManager } from '../../redux';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import Immutable from 'immutable';

/**
 * Modal window for result codes
 *
 * @author Roman Kučera
*/
class ResultCodesModal extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: this.props.detail,
      filterOpened: true,
      module: this.props.module
    };
    this.backendModuleManager = new BackendModuleManager();
  }

  getContentKey() {
    return 'content.system.be-modules';
  }

  componentDidMount() {
  }

  _closeModal(event) {
    if (event) {
      event.preventDefault();
    }
    const { detail } = this.state;
    detail.show = false;
    this.setState({
      ...detail
    });
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchResultCodes(SearchParameters.getSearchParameters(SearchParameters.getFilterData(this.refs.filterForm), this.props._searchParameters));
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchResultCodes(null);
    this.refs.filterForm.setData({});
  }

  fetchResultCodes(searchParameters) {
    const { module } = this.state;
    this.context.store.dispatch(this.backendModuleManager.getResultCodes(module.id, searchParameters));
  }

  sortByStatusEnum(one, two) {
    if (one < two) {
      return -1;
    } else if (one > two) {
      return 1;
    }
    return 0;
  }

  render() {
    const { resultCodes, _searchParameters, showLoading } = this.props;
    const { detail, filterOpened, module } = this.state;

    let _statusEnum = new Immutable.OrderedSet();
    const _resultCodes = [];
    if (resultCodes) {
      resultCodes.forEach(resultCode => {
        _resultCodes.push(resultCode);
        _statusEnum = _statusEnum.add(resultCode.statusEnum);
      });
    }
    _resultCodes.sort((one, two) => {
      // Sort by status code number
      if (parseInt(one.statusCode, 10) > parseInt(two.statusCode, 10)) {
        return 1;
      } else if (parseInt(one.statusCode, 10) < parseInt(two.statusCode, 10)) {
        return -1;
      }
      // Then sort by code
      return this.sortByStatusEnum(one.statusEnum, two.statusEnum);
    });

    _statusEnum = _statusEnum.sort((one, two) => {
      return this.sortByStatusEnum(one, two);
    });

    return (
      <Basic.Modal bsSize="large" show={detail.show} onHide={this._closeModal.bind(this)}>
        <Basic.Modal.Header text={this.i18n('result-codes.title', { moduleName: module.name })} />

        <Basic.Toolbar>
          <div>
            <div className="pull-right">
              <Advanced.Filter.ToogleButton
                filterOpen={(open) => this.setState({ filterOpened: open })}
                filterOpened={filterOpened}
                style={{ marginLeft: 3 }}
                searchParameters={_searchParameters} />
              <Advanced.RefreshButton
                onClick={this.fetchResultCodes.bind(this, _searchParameters)}
                title={this.i18n('button.refresh')}
                showLoading={showLoading} />
            </div>
            <div className="clearfix"></div>
          </div>
          <Basic.Collapse in={filterOpened}>
            <div>
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <Basic.Col lg={6}>
                      <Advanced.Filter.EnumSelectBox
                        ref="statusEnum"
                        placeholder={this.i18n('result-codes.filter.statusEnum.placeholder')}
                        options={_statusEnum.toArray().map(value => { return {value, niceLabel: value}; })}
                        searchable />
                    </Basic.Col>
                    <Basic.Col lg={6} className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)} />
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            </div>
          </Basic.Collapse>
        </Basic.Toolbar>

        <Basic.Modal.Body>
          <Basic.Table
            ref="table"
            data={_resultCodes}
            noData={this.i18n('component.basic.Table.noData')}
            rowClass={({ rowIndex, data }) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
            <Basic.Column property="statusCode" header={this.i18n('result-codes.status')} />
            <Basic.Column property="statusEnum" header={this.i18n('result-codes.code')} />
            <Basic.Column property="message" header={this.i18n('result-codes.message')}
              cell={
                ({ rowIndex, data }) => {
                  const message = this.getFlashManager().convertFromResultModel(data[rowIndex]);
                  const key = 'flash-message-' + message.id;
                  return (
                    <Basic.FlashMessage
                      key={key}
                      message={message}
                      showDate={false} />);
                }
              } />
          </Basic.Table>
        </Basic.Modal.Body>
        <Basic.Modal.Footer>
          <Basic.Button level="link" onClick={this._closeModal.bind(this)}>{this.i18n('button.cancel')}</Basic.Button>
        </Basic.Modal.Footer>
      </Basic.Modal>
    );
  }
}

ResultCodesModal.propTypes = {
  resultCodes: PropTypes.object,
  showLoading: PropTypes.bool,
  userContext: PropTypes.object
};

ResultCodesModal.defaultProps = {
  resultCodes: null,
  showLoading: true,
  userContext: null
};

function select(state) {
  return {
    resultCodes: DataManager.getData(state, BackendModuleManager.UI_KEY_RESULT_CODES),
    _searchParameters: Utils.Ui.getSearchParameters(state, BackendModuleManager.UI_KEY_RESULT_CODES),
    showLoading: Utils.Ui.isShowLoading(state, BackendModuleManager.UI_KEY_RESULT_CODES),
    userContext: state.security.userContext
  };
}

export default connect(select)(ResultCodesModal);
