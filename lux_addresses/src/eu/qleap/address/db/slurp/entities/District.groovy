package eu.qleap.address.db.slurp.entities;

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
 * "District" entity 
 * 
 * 2013.08.XX - First version
 ******************************************************************************/

class District {

    final String id // unique id, a short String
    final String nom // actual name of district
    final Date dateModif // modified when (precision is day); may be null
    
    /**
     * Constructor
     */
    
    District(Map map) {
        assert map
        assert map.containsKey('id')
        assert map.containsKey('nom')
        assert map.containsKey('dateModif')
        id        = (map['id'] as String).trim()
        nom       = (map['nom'] as String).trim()
        dateModif = map['dateModif'] // may be null
        assert id
        assert nom
    }
    
    /**
     * Stringification
     */
    
    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << id
        res << ", \""
        res << nom
        if (dateModif) {
            res << "\", "            
            res << "dateModif:" << String.format('%tF', dateModif)
        }
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static List<Rue> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def setOfNom = new HashSet()
        def setOfId  = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                "4  : STR&TRIM : Code",
                "40 : STR&TRIM : Nom",
                "10 : DATE     : DsTimestampModif")
            Map init = [:]
            init['id']        = values['Code']
            init['nom']       = values['Nom']
            init['dateModif'] = values['DsTimestampModif']
            def district = new District(init)
            boolean added1 = setOfNom.add(district.nom)
            boolean added2 = setOfId.add(district.id)
            assert added1 && added2
            res << district
        }
        return res
    }
    
    /**
     * Open the resource and read "District" entities
     */

    static List<District> makeEntitiesFromResource(Class hook, TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(hook, "DISTRICT", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<District> districts = makeEntitiesFromResource(HookLocation.hook, tz)
        districts.each {
            System.out << it << "\n"
        }
    }
}
