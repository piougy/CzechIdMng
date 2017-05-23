import React from 'react';
import _ from 'lodash';
import * as Advanced from '../../components/advanced';

/**
 * Cells for Candicates
 * maxEntry - max entry in candidates
 */
const CandicateUsersCell = ({candidates, maxEntry}) => {
  let isMoreResults = false;
  let infoCandidates = [];
  if (candidates) {
    isMoreResults = candidates.length > maxEntry;
    candidates = _.uniq(candidates);

    if (maxEntry !== undefined) {
      candidates = _.slice(candidates, 0, maxEntry);
    }
    for (const candidate of candidates) {
      infoCandidates.push(<Advanced.IdentityInfo entityIdentifier={candidate} face="popover" />);
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
};

export default CandicateUsersCell;
