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
 * "Immeuble" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class Immeuble {

    // passed in constructor

    final Integer id // the record id > 0
    final Integer numéro // houseNumber; null or else > 0
    final String  suffixe // if houseNumber is null, then null else empty or something like "A", "B", "C"
    final Date    dateFinValidité // when record becomes invalid (precision is day); may be null
    final Date    dateModif // when modified (precision is day); may be null
    final String  codePostal // may be empty (but not null)
    final Integer fk_idQuartier // reference to a "quartier" (may be unset)
    final Integer fk_idRue // reference to a "rue" (always set)
    final Boolean isProvisoire // not sure what this is; not null
    final String  désignation // possibly null

    // filled in later

    Rue rue
    Quartier quartier

    /**
     * The implication
     */

    private static boolean imply(boolean a, boolean b) {
        return !a || b
    }

    /**
     * Constructor
     */

    Immeuble(Map map) {
        assert map
        assert map.containsKey('id')
        assert map.containsKey('numéro')
        assert map.containsKey('suffixe')
        assert map.containsKey('dateFinValidité')
        assert map.containsKey('dateModif')
        assert map.containsKey('codePostal')
        assert map.containsKey('fk_idRue')
        assert map.containsKey('isProvisoire')
        assert map.containsKey('isNoNuméro')
        id           = map['id']
        isProvisoire = map['isProvisoire']
        //
        // There is a flag which may indicate that the building has, in fact, no number
        // The value of number is then 0; we set it to null
        //
        NUMBER: {
            Boolean isNoNuméro = map['isNoNuméro']
            Integer tmpNuméro  = map['numéro']
            String tmpSuffixe  = (map['suffixe'] as String).trim()
            assert tmpNuméro  != null && tmpNuméro >= 0
            assert isNoNuméro != null
            // In principle "isNoNuméro" should be equivalent to "numéro == 0"
            // That is nearly always the case except for an entitiy that is "provisoire"
            // Just fix this mess
            // assert imply( (tmpNuméro == 0) , !tmpSuffixe) this may be false!!
            // assert imply( (tmpNuméro == 0) , isNoNuméro) this may be false!!
            if (tmpNuméro == 0) {
                numéro  = null
                suffixe = null
            }
            else {
                numéro  = tmpNuméro
                suffixe = tmpSuffixe
                // explain what this means:
                assert numéro != null && numéro > 0 && suffixe != null
            }
        }
        dateFinValidité = map['dateFinValidité'] // may be null
        dateModif       = map['dateModif'] // may be null
        codePostal      = map['codePostal'] // may be empty
        fk_idQuartier   = map["fk_idQuartier"] // may be null
        fk_idRue        = map["fk_idRue"] // not null
        if (map['désignation']) {
            désignation = (map['désignation'] as String).trim()
        }
        else {
            désignation = null
        }
        assert id != null && id > 0
        assert isProvisoire != null
        assert codePostal != null
        assert !codePostal || (codePostal as Integer) > 1000
        assert fk_idQuartier == null || fk_idQuartier > 0
        assert fk_idRue != null &&  fk_idRue > 0

    }

    /**
     * Find the actual unqiue "Rue" instance corresponding to the "fk_idRue"
     * and reference it.
     */

    void wireUp(Map rueMap, List<Quartier> quartiers) {
        assert rueMap != null
        assert quartiers != null
        RUE: {
            rue = rueMap[fk_idRue]
            assert rue
        }
        QUARTIER: {
            if (fk_idQuartier) {
                def coll = quartiers.findAll { it.id == fk_idQuartier }
                assert coll.size() == 1
                quartier = coll.first()
            }
        }
    }

    /**
     * Combined numéro; never returns null but may return the empty string
     */

    public String getCombinedNuméro() {
        StringBuilder buf = new StringBuilder()
        if (numéro) {
            buf << numéro
            if (suffixe) {
                buf << suffixe
            }
        }
        return buf as String
    }

    /**
     * Printout
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << "id:"
        res << id
        res << ", "
        res << "addr:"
        res << getCombinedNuméro()
        res << ", "
        res << "codePostal:"
        res << codePostal
        if (dateFinValidité) {
            res << ", "
            res << "dateFinValidité: " << String.format('%tF', dateFinValidité)
        }
        if (dateModif) {
            res << ", "
            res << "modifié:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << "rue:"
        res << fk_idRue
        if (fk_idQuartier) {
            res << ", "
            res << "quartier:"
            res << fk_idQuartier
        }
        res << ", "
        res << isProvisoire
        res << ", "
        if (désignation) {
            res << "\"" << désignation << "\""
        }
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static void readEntitiesIntoMap(Reader reader, TimeZone tz, boolean withDésignation, Map mapByImmoId) {
        assert reader !=null
        assert mapByImmoId !=null
        //
        // Structure of the resource varies depending on whether the "désignation" is expected or not
        //
        def structure = [
            "8  : INT      : NuméroInterne",
            "3  : INT      : Numéro",
            "3  : STR&TRIM : CodeMultiple",
            "10 : DATE     : DateFinValid",
            "1  : STR&TRIM : DateFinValidNullIf",
            "10 : DATE     : DsTimestampModif",
            "4  : STR&TRIM : FkCodePtNuméro",
            "1  : STR&TRIM : FkCodePtNuméroNullIf",
            "5  : INT      : FkQuartNuméro",
            "1  : STR&TRIM : FkQuartNuméroNullIf",
            "5  : INT      : FkRueNuméro",
            "1  : STR&TRIM : FkRueNuméroNullIf",
            "1  : BOOL     : IndicNoIndef",
            "1  : BOOL     : IndicProvisoire"
        ]
        if (withDésignation) {
            structure << "40 : STR&TRIM : Désignation"
            structure << "1  : STR&TRIM : DésignationNullIf"
        }
        //
        // Now read using the variant structure; filling the passed map
        //
        def mayByNumberInRue = [:]
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz, structure as String[])
            Map init = [:]
            init['id']              = values['NuméroInterne']
            init['numéro']          = values['Numéro']
            init['suffixe']         = values['CodeMultiple']
            init['dateFinValidité'] = values['DateFinValid']
            init['dateModif']       = values['DsTimestampModif']
            init['codePostal']      = values['FkCodePtNuméro']
            init['rueId']           = values['FkRueNuméro']
            init['isNoNuméro']      = values['IndicNoIndef']
            init['isProvisoire']    = values['IndicProvisoire']
            init['codePostal']      = values['FkCodePtNuméro']
            init['fk_idQuartier']   = values['FkQuartNuméro']
            init['fk_idRue']        = values['FkRueNuméro']
            init['désignation']     = values['Désignation'] // may not exist
            Immeuble immo  = new Immeuble(init)
            //
            // Immeuble is bizarre: There may well be several records for an "Immeuble" at the same rue & numéro,
            // with differing code postal, or the same code postal, and no "fin date validité" :-(
            // Looks like data cleanup is needed..
            // Just check by id (comon to both resources)
            //
            if (!withDésignation) {
                assert !mapByImmoId.containsKey(immo.id) : "Already seen: ${immo} seen as ${mapByImmoId[immo.id]}"
            }
            else {
                // REPLACE the earlier entry by this new one, bearing a descriptive text
                assert mapByImmoId.containsKey(immo.id) : "Not yet seen ${immo}"
            }
            mapByImmoId[immo.id] = immo
        }
    }

    /**
     * Open the resource and read entities
     */

    static List<Immeuble> makeEntitiesFromResource(Class hook, TimeZone tz) {
        assert tz != null
        Map mapByImmoId = new TreeMap() // autosort by id
        // note that one MUST read those "without désignation" first
        [false, true].each { boolean avecDésignation ->
            String resourceName = avecDésignation ? "IMMDESIG" : "IMMEUBLE"
            String txt = ResourceHelp.slurpResource(hook, resourceName, "ISO-8859-1")
            (new StringReader(txt)).withReader { reader ->
                readEntitiesIntoMap(reader, tz, avecDésignation, mapByImmoId)
            }
        }
        // fill res; traqversal of map happens in order of id
        List res = new ArrayList(mapByImmoId.size())
        mapByImmoId.values().each {  res << it }
        return res
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Immeuble> immos = makeEntitiesFromResource(HookLocation.hook, tz)
        immos.each {
            System.out << it << "\n"
        }
    }
}
