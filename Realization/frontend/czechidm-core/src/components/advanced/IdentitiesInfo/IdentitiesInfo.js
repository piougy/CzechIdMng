import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import IdentityInfo from '../IdentityInfo/IdentityInfo';
import * as Basic from '../../basic';

/**
 * Cells for Candicates
 * maxEntry - max entry in candidates
 *
 * @Deprecated since 10.4.1.
 * Use src.content.identity.IdentitiesInfo.IdentitiesInfo instead!
 *
 */
class IdentitiesInfo extends Basic.AbstractComponent {

  render() {
    const { identities, isUsedIdentifier, maxEntry, showOnlyUsername} = this.props;
    let candidatesResult;
    let isMoreResults = false;
    let infoCandidates = [];
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
    }

    return (
      <span>
        {infoCandidates}
        {
          !isMoreResults
          ||
          ', ...'
        }
      </span>
    );
  }
}

IdentitiesInfo.propTypes = {
  /* eslint-disable react/forbid-prop-types */
  identities: PropTypes.array,
  isUsedIdentifier: PropTypes.bool,
  maxEntry: PropTypes.number,
  showOnlyUsername: PropTypes.bool
};

IdentitiesInfo.defaultProps = {
  isUsedIdentifier: true,
  showOnlyUsername: true,
  maxEntry: 2
};

export default IdentitiesInfo;
