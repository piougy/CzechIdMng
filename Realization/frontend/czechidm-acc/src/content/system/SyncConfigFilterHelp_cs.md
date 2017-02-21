# <span class="glyphicon glyphicon-filter"></span> Definice vlastního filtru synchronizace.

Vlastní filtr je možné definovat jednoduše zvolením atributu (**Filtrovat dle atributu**) podle kterého chceme hledat a příslušné operace (**Filtrovací operace**).

Pokud potřebujeme vytvořit složitější filtrovací kritérium, je k tomu možné využít tento skript.
V příkladu níže je uvedena situace, kdy chceme filtrovat dle vybraného atributu a operace (do skriptu vstupuje jako proměnná **filter**), ale zároveň chceme výsledky omezit další podmínkou. V tomto případě tak, že všechny výsledky musí mít atribut "**lastname**" rovný hodnotě "**Doe**".

```javascript
import  eu.bcvsolutions.idm.ic.filter.impl.IcFilterBuilder;
import  eu.bcvsolutions.idm.ic.api.IcAttribute;
import  eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;

IcAttribute attr = new IcAttributeImpl("lastname", "Doe");

return IcFilterBuilder.and(filter, IcFilterBuilder.equalTo(attr));
```

### IcFilterBuilder poskytuje operace:
* IcFilterBuilder.equalTo(...)
* IcFilterBuilder.contains(...)
* IcFilterBuilder.startsWith(...)
* IcFilterBuilder.endsWith(...)
* IcFilterBuilder.or(...)
* IcFilterBuilder.and(...)
* IcFilterBuilder.not(...)

Výstupem tohto skriptu musí být objekt typu **IcFilter**. Pokud bude výstup **null**, pak nebude filtr aplikován a synchronizace bude spuštěna nad všemi účty.
