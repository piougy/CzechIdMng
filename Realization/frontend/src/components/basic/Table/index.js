'use strict';

import _ from 'lodash';
import Table from './Table';
import Pagination from './Pagination';
import Column from './Column';
import DefaultCell from './DefaultCell';
import SortHeaderCell from './SortHeaderCell';
import TextCell from './TextCell';
import LinkCell from './LinkCell';
import DateCell from './DateCell';
import BooleanCell from './BooleanCell';
import EnumCell from './EnumCell';

var TableRoot = {
  Table: Table,
  Column: Column,
  Pagination: Pagination,
  Cell: DefaultCell,
  SortHeaderCell: SortHeaderCell,
  TextCell: TextCell,
  LinkCell: LinkCell,
  DateCell: DateCell,
  BooleanCell: BooleanCell,
  EnumCell: EnumCell
};
_.merge(TableRoot, { BasicTable: TableRoot });
TableRoot.version = '0.0.1';
module.exports = TableRoot;
