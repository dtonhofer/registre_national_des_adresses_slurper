package eu.qleap.address.db.slurp.entities

import eu.qleap.address.db.slurp.helpers.ParsingHelp
import eu.qleap.address.db.slurp.helpers.ResourceHelp
import eu.qleap.address.db.slurp.main.HookLocation

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, q-leap S.A.
 *                     14, rue Aldringen
 *                     L-1118 LUXEMBOURG
 *
 * Distributed under "The MIT License" 
 * http://opensource.org/licenses/MIT
 *******************************************************************************
 *******************************************************************************
 * "Localité" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class Localité {

    // passed in constructor

    final Integer id // the record id > 0
    final String nom // name; the "nom majuscule" is not retained as it can be derived directly
    final Integer seqNumberInCommune  // >= 0
    final Boolean isVille // town or not?
    final Date dateFinValidité // when record becomes invalid (precision is day); may be null
    final Date dateModif // when modified (precision is day); may be null
    final Integer fk_cantonId // id of owning canton
    final Integer fk_communeSeqNumberInCanton  // sequence number of commune found in owning canton

    // Wired-up later from fk values
    // Stays null for (224, "Obereisenbach") (226, "Untereisenbach") (362, "Fond de Heiderscheid (E.)")
    // which seem to be in Germany....

    Commune commune

    // indicate whether this localité has any quartiers...

    boolean hasQuartiers = false

    // filled-in later

    final List<AliasLocalité> aliases = []

    /**
     * Constructor
     */

    Localité(Map map) {
        assert map
        assert map.containsKey('id')
        assert map.containsKey('nom')
        assert map.containsKey('seqNumberInCommune')
        assert map.containsKey('isVille')
        assert map.containsKey('fk_cantonId')
        assert map.containsKey('fk_communeSeqNumberInCanton')
        assert map.containsKey('dateFinValidité')
        assert map.containsKey('dateModif')
        id                          = map['id']
        nom                         = (map['nom'] as String).trim()
        seqNumberInCommune          = map['seqNumberInCommune']
        isVille                     = map['isVille']
        dateFinValidité             = map['dateFinValidité'] // may be null
        dateModif                   = map['dateModif'] // may be null
        fk_cantonId                 = map['fk_cantonId']
        fk_communeSeqNumberInCanton = map['fk_communeSeqNumberInCanton']
        assert id != null && id > 0
        assert nom
        assert seqNumberInCommune != null && seqNumberInCommune >= 0
        assert isVille != null
        assert fk_cantonId != null  && fk_cantonId >= 0
        assert fk_communeSeqNumberInCanton != null && fk_communeSeqNumberInCanton >= 0
    }

    /**
     * Wire up to the actual Commune instance; returns false if that failed!
     */

    boolean wireUp(List<Commune> communes) {
        assert communes != null
        def coll = communes.findAll {  Commune it ->
            // Commune "it" must be in the same "canton" than the "localité" and have the same "commune sequence number"
            it.fk_cantonId == fk_cantonId && it.seqNumberInCanton == fk_communeSeqNumberInCanton
        }
        if (coll.isEmpty()) {
            return false
        }
        else {
            assert coll.size() == 1
            commune = coll.first()
            return true
        }
    }

    /**
     * Find all the rues belonging to this localité
     * This should be made a tad more efficient
     */

    List<Rue> findRues(List<Rue> allRues) {
        assert allRues
        def res =
                allRues.findAll { Rue rue ->
                    rue.localité.id == this.id
                }
        return res as List
    }

    /** 
     * Check whether wired to a "commune"
     */

    boolean isWired() {
        return commune != null
    }

    /**
     * Helper
     */

    private static String csvEscapeAndQuote(String x) {
        assert x != null
        // x may alread have quotes; if so rip them away
        if (!x.isEmpty() && x[0] == '"') {
            x = x.substring(1)
        }
        if (!x.isEmpty() && x[-1] == '"') {
            x = x.substring(0,x.length()-1)
        }
        String[] splits = x.split('"',-1)
        StringBuilder buf = new StringBuilder()
        buf << '"'
        for (int i=0; i<splits.length-1; i++) {
            buf << splits[i]
            buf << '"'
            buf << '"'
        }
        buf << splits[-1]
        buf << '"'
        // System.err << x << " splits into " << splits << " and yields " << buf << "\n"
        return buf as String
    }

    /**
     * Return a list of strings describing this for CSV.
     * See http://en.wikipedia.org/wiki/Comma-separated_values for information on CSV "format".
     * One does not know how many fields there will be over all "Localités", so the caller is supposed
     * to collect all the lists before starting printout. 
     */

    List<String> toCSV(def quoteChar = '"') {
        def res = []
        res << csvEscapeAndQuote(nom)
        res << isVille
        if (isWired()) {
            res << csvEscapeAndQuote(commune.nom)
        }
        else {
            res << '?'
        }
        aliases.each { alias ->
            res << csvEscapeAndQuote(alias.nom)
        }
        return res
    }

    /**
     * Printout; do not print "quartiers"; that is left to the caller
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << id
        res << ", \""
        res << nom
        res << "\", "
        res << "seqNumberInCommune:"
        res << seqNumberInCommune
        res << ", "
        res << "ville:" << isVille
        if (dateFinValidité) {
            res << ", "
            res << "dateFinValidité: " << String.format('%tF', dateFinValidité)
        }
        if (dateModif) {
            res << ", "
            res << "dateModif:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << "commune:"
        if (isWired()) {
            res << commune.nom
        }
        else {
            res << "("
            res << "canton:" << fk_cantonId
            res << ", "
            res << "communeSeqNumber:" << fk_communeSeqNumberInCanton
            res << ")"
        }
        // aliases on the same line
        aliases.each { alias ->
            res << " -- alias -- " << alias
        }
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static List<Localité> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def setOfId        = new HashSet()
        def setOfNom       = new HashSet()
        def setOfFkCommune = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "5   : INT       : Numéro",
                    "40  : STR&TRIM  : Nom",
                    "40  : STR&TRIM  : NomMajuscule",
                    "2   : INT       : Code", // seq. à l'intérieur de la commune
                    "1   : BOOL      : IndicVille",
                    "10  : DATE      : DateFinValid",
                    "1   : STR&TRIM  : DateFinValidNullIf",
                    "10  : DATE      : DsTimestampModif",
                    "2   : INT       : FkCantoCode",
                    "1   : STR&TRIM  : FkCantoCodeNullIf",
                    "2   : INT       : FkCommuCode",
                    "1   : STR&TRIM  : FkCommuCodeNullIf",
                    )
            Map init = [:]
            init['id']                          = values['Numéro']
            init['nom']                         = values['Nom']
            init['dateModif']                   = values['DsTimestampModif']
            init['dateFinValidité']             = values['DateFinValid']
            init['seqNumberInCommune']          = values['Code'] // sequential number within commune
            init['isVille']                     = values['IndicVille']
            init['fk_cantonId']                 = values['FkCantoCode']
            init['fk_communeSeqNumberInCanton'] = values['FkCommuCode']
            assert ParsingHelp.deacriticize(values['Nom'].toUpperCase()) == values['NomMajuscule']
            def localité = new Localité(init)
            boolean added1 = setOfNom.add(localité.nom + ";" + localité.dateFinValidité)
            boolean added2 = setOfId.add(localité.id + ";" + localité.dateFinValidité)
            assert added1 && added2 : "Already seen: ${localité}"
            res << localité
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<Localité> makeEntitiesFromResource(Class hook, TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(hook, "LOCALITE", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Localité> localités = makeEntitiesFromResource(HookLocation.hook, tz)
        localités.each {
            System.out << it << "\n"
        }
    }
}
