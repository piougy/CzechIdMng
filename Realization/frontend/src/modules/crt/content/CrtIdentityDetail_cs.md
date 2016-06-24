# Certifikáty

Je možné si zažádat o dva typy certifikátů:
* **Podepisovací** - slouží pro elektoronické podepsaní dokumentů
* **Šifrovací** - souží pro zašifrování dat. O tento typ certifikátu může zádat **pouze interní zaměstnanec**.

Před vytvořením certifikátu je třeba mít od vedoucího schváleno, že je možné vytvořit daný typ certifikátu. Žádost pro schválení vedoucím je možné odeslat prostřednictvím tlačítka _Zažádat_ v panelu _Oprávnění k certifikátům_.

## <button class="btn btn-xs btn-primary" disabled><span class="glyphicon glyphicon-certificate"></span></button> Vytvoření certifikátu

Při vytvoření certifikátu je třeba vybrat typ certifikátu a vyplnit heslo k privátní části certifikátu. Heslo je třeba si zapamatovat, nebude nikde uloženo. Heslo bude třeba zadat k otevření staženého certifikátu. **Není doporučeno použití stejného hesla, které je použito k přihlášení do systému.** Před vytvořením certifikátu je vyžadováno seznámit se s certifikační politikou a podmínkami používání certifikátu.

Po požádání o certifikát je nejprve vytvořena žádost, která se zpracovává. Zpracování žádosti trvá obvykle přibližne 30s. Po zpracování žádosti je vytvořen požadovaný certifikát. Pokud zpracování žádosti potrvá déle a žádost nebude vyřízena v časovém okně 30s, je možné dokončení zpracování žádosti zkontrolovat použitím tlačítka pro kontrolu stavu u žádosti.

<div class="alert alert-info">
Je možné mít v jednu chvíli pouze jeden platný certifikát daného typu. Pokud je zažádáno o nový certifikát stejného typu, který je již vytvořený, tak je původní certifikát automaticky zneplatněn.
</div>

## <button class="btn btn-xs btn-success" disabled><span class="glyphicon glyphicon-download-alt "></span></button> Stažení certifikátu

Tlačítko stahuje certifikát včetně privátní části.

V seznamu certifikátů je možné si přes odkaz stáhnout konkrétní formát:
* **PEM** - certifikát
* **PFX** - certifikát včetně privátní části

## <button class="btn btn-xs btn-danger" disabled><span class="glyphicon glyphicon-trash"></span></button> Zneplatnění certifikátu
Zneplatnění neboli revokace certifikátu může býti provedena z důvodu:
* ukončení pracovního poměru - všechny vytvořené platné certifikáty jsou automaticky revokovány
* při vytvoření nového certifikátu stejného typu je původní certifikát automaticky revokován
* v případě zcizení a podobně je možné certifikát revokovat ručně prostředníctvím tlacítka pro zneplatnění u certifikátu.
