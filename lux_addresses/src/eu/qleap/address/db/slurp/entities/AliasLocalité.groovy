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
 * "AliasLocalité" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class AliasLocalité implements Comparable<AliasLocalité> {

    // passed in constructor

    final Integer numéroSeq // a counting number (unique for a fk_localitéId); not an id
    final String nom // name; the "nom majuscule" is not retained as it can be derived directly
    final String langue // 1-char language code
    final Date dateModif // when modified (precision is day); may be null
    final Integer fk_localitéId // pointer to localité

    /**
     * Constructor
     */

    AliasLocalité(Map map) {
        assert map
        assert map.containsKey('numéroSeq')
        assert map.containsKey('nom')
        assert map.containsKey('langue')
        assert map.containsKey('dateModif')
        assert map.containsKey('fk_localitéId')
        numéroSeq     = map['numéroSeq']
        nom           = (map['nom'] as String).trim()
        langue        = (map['langue'] as String).trim()
        dateModif     = map['dateModif'] // may be null
        fk_localitéId = map['fk_localitéId']
        assert numéroSeq != null && numéroSeq > 0
        assert nom
        assert langue != null // may be empty though
        assert fk_localitéId != null && fk_localitéId > 0
    }

    /**
     * Stringify
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << "\"" << nom << "\""
        res << ", "
        res << numéroSeq
        if (langue) {
            res << ", "
            res << langue
        }
        if (dateModif) {
            res << ", "
            res << "modifié:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << "fk_localitéId:" << fk_localitéId
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static List<AliasLocalité> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def setOfId = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "3  : INT      : NuméroSéquentiel",
                    "40 : STR&TRIM : Nom",
                    "40 : STR&TRIM : NomMaj",
                    "1  : STR&TRIM : Langue",
                    "10 : DATE     : DsTimestampModif",
                    "5  : INT      : FkLocalNuméro")
            Map init = [:]
            init['numéroSeq']     = values['NuméroSéquentiel']
            init['nom']           = values['Nom']
            init['langue']        = values['Langue']
            init['dateModif']     = values['DsTimestampModif']
            init['fk_localitéId'] = values['FkLocalNuméro']
            assert ParsingHelp.deacriticize(values['Nom'].toUpperCase()) == values['NomMaj']
            def alias = new AliasLocalité(init)            
            String compositeId = "${alias.nom};${alias.fk_localitéId}"
            boolean added = setOfId.add(compositeId)
            assert added
            res << alias
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<AliasLocalité> makeEntitiesFromResource(Class hook, TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(hook, "ALIAS.LOCALITE", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }
    
    /**
     * Used when sorting by "numéro séquentiel"
     */

    @Override
    public int compareTo(AliasLocalité o) {
        assert o 
        return this.numéroSeq <=> o.numéroSeq
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")         
        List<AliasLocalité> aliasLocalités = makeEntitiesFromResource(HookLocation.hook, tz)
        aliasLocalités.each {
            System.out << it << "\n"
        }
    }

}
