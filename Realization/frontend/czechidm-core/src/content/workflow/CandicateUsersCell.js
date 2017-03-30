import React from 'react';
import _ from 'lodash';

/**
 * Cells for Candicates
 * maxEntry - max entry in candidates
 */
const CandicateUsersCell = ({rowIndex, data, property, maxEntry}) => {
  let candidates = data[rowIndex][property];
  let isMoreResults = false;
  if (candidates) {
    isMoreResults = candidates.length > maxEntry;
    candidates = _.uniq(candidates);

    if (maxEntry !== undefined) {
      candidates = _.slice(candidates, 0, maxEntry);
    }

    candidates = _.join(candidates, ', ');
  }

  return (
    <span>
      {candidates}
      {
        !isMoreResults
        ||
        ', ...'
      }
    </span>
  );
};

export default CandicateUsersCell;
