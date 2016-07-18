

import Table from './Table/Table';
import Column from './Table/Column';
import ColumnLink from './Table/ColumnLink';
import IdentityInfo from './IdentityInfo/IdentityInfo';
import Navigation from './Navigation/Navigation';
import NavigationItem from './Navigation/NavigationItem';
import TabPanel from './TabPanel/TabPanel';
import Filter from './Filter/Filter';
import DateValue from './DateValue/DateValue';
import Tree from './Tree/Tree';
import DetailButton from './Table/DetailButton';
import ModalProgressBar from './ModalProgressBar/ModalProgressBar';

var Components = {
  Table: Table,
  Column: Column,
  ColumnLink: ColumnLink,
  IdentityInfo: IdentityInfo,
  Navigation: Navigation,
  TabPanel: TabPanel,
  Filter: Filter,
  _ToogleButton: Filter.ToogleButton,
  _FilterButtons: Filter.FilterButtons,
  DateValue: DateValue,
  ModalProgressBar: ModalProgressBar,
  Tree: Tree,
  DetailButton: DetailButton
};

Components.version = '0.0.1';
module.exports = Components;
