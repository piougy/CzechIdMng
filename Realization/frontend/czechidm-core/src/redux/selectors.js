/**
 * Redux selectors
 *
 * @author Vít Švanda
 *
 * For separate extraction from the state on one place.
 *
 * Performance reason:
 * The redux invoke render if some of selected properties were changed.
 * Problem is that the redux use "===" for detection of changes. It means this does not work for immutable object.
 * For example every array use in select method will invoke rendering on any change of redux state.
 * For prevent it we need to use memoizing (cache) and return same instance of properties when object was not changed.
 */

// import { createSelector } from 'reselect';
import equal from 'fast-deep-equal';

// This should be work, but not for me (result is still new instance). Reselect project is recommended in official Redux documentation.
const getEntities = (state, component) => {
  const uiKey = component.manager.resolveUiKey(component.uiKey);
  return component.manager.getEntities(state, uiKey);
};

// export const makeSelectEntities = () => {
//   return createSelector(
//     getEntities,
//     (entities) => entities
//   );
// };

// Because Reselect version not working. I use more naive solution for now. I impelmented cache, it is same idea as use Reselect.
export const selectEntities = () => {
  let cachedEntities = null;

  return (state, props) => {
    const newEntities = getEntities(state, props);
    if (newEntities && equal(newEntities, cachedEntities)) {
      return cachedEntities;
    }
    cachedEntities = newEntities;
    return cachedEntities;
  };

};
