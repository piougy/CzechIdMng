import Table from './Table/Table';
import Column from './Table/Column';
import ColumnLink from './Table/ColumnLink';
import IdentityInfo from './IdentityInfo/IdentityInfo';
import Navigation from './Navigation/Navigation';
import TabPanel from './TabPanel/TabPanel';
import Filter from './Filter/Filter';
import DateValue from './DateValue/DateValue';
import Tree from './Tree/Tree';
import DetailButton from './Table/DetailButton';
import ModalProgressBar from './ModalProgressBar/ModalProgressBar';
import EavForm from './Form/EavForm';
import EavContent from './Form/EavContent';
import PasswordField from './PasswordField/PasswordField';
import ProgressBar from './ProgressBar/ProgressBar';
import RichTextArea from './RichTextArea/RichTextArea';
import AbstractTableContent from './Content/AbstractTableContent';

const Components = {
  Table,
  Column,
  ColumnLink,
  IdentityInfo,
  Navigation,
  TabPanel,
  Filter,
  _ToogleButton: Filter.ToogleButton,
  _FilterButtons: Filter.FilterButtons,
  DateValue,
  ProgressBar,
  ModalProgressBar,
  Tree,
  DetailButton,
  EavForm,
  EavContent,
  PasswordField,
  RichTextArea,
  AbstractTableContent
};

Components.version = '0.0.1';
module.exports = Components;
