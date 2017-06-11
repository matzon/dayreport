package dk.matzon.dayreport.domain.model;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public enum Region {
    NORDJYLLAND("Nordjylland"),
    MIDTVESTJYLLAND("Midt- og Vestjylland"),
    OESTJYLLAND("Østjylland"),
    SYDOESTJYLLAND("Sydøstjylland"),
    SYDSOENDERJYLLAND("Syd- og Sønderjylland"),
    FYN("Fyn"),
    MIDTVESTSJAELLAND("Midt- og Vestsjælland"),
    SYDSJAELLAND("Sydsjælland og Lolland-Falster"),
    NORDSJAELLAND("Nordsjælland"),
    VESTEGNEN("Københavns Vestegn"),
    KOEBENHAVN("København"),
    BORNHOLM("Bornholm"),
    GROENLAND("Grønland"),
    FAEROEERNE("Færøerne");

    private final String reqionName;

    Region(String _regionName) {
        this.reqionName = _regionName;
    }

    public String getReqionName() {
        return reqionName;
    }
}
