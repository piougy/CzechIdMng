import React from 'react';
import CandicateUserCell from './CandicateUserCell';

/**
 * Cells for Candicates
 * maxEntry - max entry in candidates
 */
const CandicateUsersCell = ({rowIndex, data, property, maxEntry}) => {
  let moreResults = false;
  return (
    <span>
      {
        data[rowIndex][property].map(function iterate(candicate, index) {
          if (moreResults || index >= maxEntry) {
            moreResults = true;
            return null;
          }
          return (
            <CandicateUserCell candicate={candicate} />
          );
        })}
        {
          !moreResults
          ||
          '...'
        }
    </span>
  );
};

export default CandicateUsersCell;
