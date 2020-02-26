# AbstractTableContent Component

Content with advanced table and CRUD methods id modal. Extends Basic.AbstractContent.

Usage is the same as other content, adds default CRUD methods only.


# DetailHeader Component

Detail header with title, system information and close button. Reuses ``PageHeader`` component - children will be rendered as ``PageHeader`` text.

## Parameters

All parameters from ``AbstractContextComponent`` are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entity | object | Entity for show system information. | |
| back  | string | Close button path. Close button will not be shown, if empty. |  |
| icon  | string | Header left icon |  ||
