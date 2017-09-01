# IdentitiesInfo component

Show multiple identities (candidates) in one cell. Component use standard IdentityInfo component inner.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| identities | Array of UUIDs  |  Array of identity identifiers. |  |
| maxEntry | int  |  Number of candidates who will be showing before will be using '...'  |  | 
| showOnlyUsername | bool | If true, then show only username instead niceLabel | true |


## Usage

```html
<Advanced.IdentitiesInfo identities={entity.candicateUsers} maxEntry={2} />
```
