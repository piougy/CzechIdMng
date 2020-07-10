import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import IdentityInfo from '../../components/advanced/IdentityInfo/IdentityInfo';
import AbstractTableContent from '../../components/advanced/Content/AbstractTableContent';
import * as Basic from '../../components/basic';
import IdentityTable from './IdentityTable';
import SearchParameters from '../../domain/SearchParameters';

/**
 * Component for show identities. Input can be array of UUID or IdentityDto.
 *
 * This component cannot be moved to the Advanced components, because has
 * relations on IdentityTable (wich uses Advanced component too). Prevent of cycling.
 *
 * @author Vít Švanda
 */
class IdentitiesInfo extends AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showIdentityTableModal: false
    };
  }

  closeModal() {
    this.setState({
      showIdentityTableModal: false
    });
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showIdentityTableModal: true
    });
  }

  getHeader() {
    const { header} = this.props;
    // prop or default
    return header || this.i18n('content.identities.header');
  }

  render() {
    const { identities, isUsedIdentifier, maxEntry, showOnlyUsername} = this.props;
    const { showIdentityTableModal } = this.state;

    let candidatesResult;
    let isMoreResults = false;
    let infoCandidates = [];
    let moreResultComponent = null;

    if (identities) {
      isMoreResults = identities.length > maxEntry;
      candidatesResult = _.uniq(identities);

      if (maxEntry !== undefined) {
        candidatesResult = _.slice(candidatesResult, 0, maxEntry);
      }
      for (const candidate of candidatesResult) {
        if (isUsedIdentifier) {
          infoCandidates.push(<IdentityInfo key={candidate} entityIdentifier={candidate} face="popover" showOnlyUsername={showOnlyUsername} />);
        } else {
          infoCandidates.push(<IdentityInfo key={candidate.id} entity={candidate} face="popover" showOnlyUsername={showOnlyUsername} />);
        }
        infoCandidates.push(', ');
      }

      infoCandidates = _.slice(infoCandidates, 0, infoCandidates.length - 1);

      if (isMoreResults) {
        moreResultComponent =
          <Basic.Button
            className="embedded"
            level="link"
            onClick={ this.showDetail.bind(this) }>
            { '...' }
          </Basic.Button>;
      }
    }
    // GET request is has limmited size. We will cut the list of IDs to 100 items (cca 4000 characters).
    let cuttedIdentities = null;
    if (identities) {
      // Filter works only with UUID, we have to made conversion if full identity is input value.
      if (isUsedIdentifier) {
        cuttedIdentities = identities;
      } else {
        cuttedIdentities = identities.map(identity => identity.id);
      }
    }

    let isCutted = false;
    let count = 0;
    const limit = 100;
    if (identities && identities.length >= limit) {
      cuttedIdentities = identities ? _.slice(_.uniq(identities), 0, limit) : null;
      isCutted = true;
      count = identities.length;
    }

    return (
      <Basic.Div>
        <span>
          {infoCandidates}
          { isMoreResults ? ', ' : '' }
          {moreResultComponent}
        </span>
        <Basic.Modal
          show={ showIdentityTableModal }
          onHide={ this.closeModal.bind(this) }
          backdrop="static"
          bsSize="large"
          keyboard>
          <Basic.Modal.Header
            closeButton
            icon="fa:user"
            text={ this.getHeader() }/>
          <Basic.Modal.Body style={{padding: 0}}>
            <Basic.Alert
              rendered={isCutted}
              level="warning"
              style={{margin: 15}}
              text={this.i18n('content.identities.info.warningTooMuchIds', {count, limit})}/>
            <IdentityTable
              ref="identitiesByIdsTable"
              forceSearchParameters={
                new SearchParameters()
                  .setFilter('id', cuttedIdentities)
              }
              showAddButton={ false }
              columns={ ['entityInfo', 'state'] }/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.closeModal.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

IdentitiesInfo.propTypes = {
  /* eslint-disable react/forbid-prop-types */
  identities: PropTypes.array,
  isUsedIdentifier: PropTypes.bool,
  maxEntry: PropTypes.number,
  showOnlyUsername: PropTypes.bool,
  header: PropTypes.string
};

IdentitiesInfo.defaultProps = {
  isUsedIdentifier: true,
  showOnlyUsername: true,
  maxEntry: 2
};

export default IdentitiesInfo;
