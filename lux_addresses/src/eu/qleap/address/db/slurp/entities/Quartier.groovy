package eu.qleap.address.db.slurp.entities;

import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import eu.qleap.address.db.slurp.helpers.ParsingHelp;
import eu.qleap.address.db.slurp.helpers.ResourceHelp;
import eu.qleap.address.db.slurp.resources.Hook;

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
 * "Quartier" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class Quartier implements Comparable<Quartier> {

    // passed in constructor

    final Integer id // the record id > 0 or a sequence number; not sure which
    final String nom // name; the "nom majuscule" is not retained as it can be derived directly
    final Date dateModif // when modified (precision is day); may be null
    final Integer fk_localitéId // identifiant de la localité

    // wired up later

    Localité localité

    /**
     * Constructor
     */

    Quartier(Map map) {
        assert map
        assert map.containsKey('id')
        assert map.containsKey('nom')
        assert map.containsKey('dateModif')
        assert map.containsKey('fk_localitéId')
        id            = map['id']
        nom           = (map['nom'] as String).trim()
        dateModif     = map['dateModif'] // may be null
        fk_localitéId = map['fk_localitéId']
        assert id != null && id > 0
        assert nom
        assert fk_localitéId != null && fk_localitéId > 0
    }

    /**
     * Wire up to the actual Commune instance; returns false if that failed!
     */

    boolean wireUp(List<Localité> localités) {
        assert localités != null
        def coll = localités.findAll {  Localité it ->
            it.id == fk_localitéId
        }
        assert coll.size() == 1
        localité = coll.first()
        localité.hasQuartiers = true
    }

    /**
     * Printout
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << id
        res << ","
        res << "\""
        res << nom
        res << "\""
        if (dateModif) {
            res << ", "
            res << "dateModif:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << "fk_localitéId:" << fk_localitéId
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static List<Quartier> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def compositeNameSet = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "5  : INT      : Numéro",
                    "40 : STR&TRIM : Nom",
                    "10 : DATE     : DsTimestampModif",
                    "5  : INT      : FkLocalNuméro",
                    "1  : STR&TRIM : FkLocalNuméroNullIf")
            Map init = [:]
            init['id']            = values['Numéro']
            init['nom']           = values['Nom']
            init['dateModif']     = values['DsTimestampModif']
            init['fk_localitéId'] = values['FkLocalNuméro']
            Quartier quartier = new Quartier(init)
            String compositeName = "${quartier.nom}+${quartier.fk_localitéId}"
            boolean added = compositeNameSet.add(compositeName)
            assert added : "Already saw '${compositeName}': '${quartier}' vs. '${res[compositeName]}'"
            res << quartier
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<Quartier> makeEntitiesFromResource(TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(Hook.class, "QUARTIER", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Quartier> quartiers = makeEntitiesFromResource(tz)
        quartiers.each {
            System.out << it << "\n"
        }
    }

    /**
     * Compare by name
     */

    @Override
    public int compareTo(Quartier o) {
        assert o
        return this.nom <=> o.nom
    }
}
