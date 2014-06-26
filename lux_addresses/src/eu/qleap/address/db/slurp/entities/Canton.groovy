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
 * "Canton" entity.
 * 
 * 2013.08.XX - First version
 ******************************************************************************/

class Canton {

    // Set at construction time

    final Integer id // unique id; not null, >= 0
    final String nom // name of canton (uppercased name is not retained as it can be derived)
    final Date dateModif // moidification date (precision is day); may be null
    final String fk_districtId // reference to owning district (a string, not an int)

    // Wired up later based on "fk_codeDistrict"; once wired up, "fk_districtId" becomes useless

    District district

    /**
     * Constructor
     */

    Canton(Map map) {
        assert map
        assert map.containsKey('id')
        assert map.containsKey('nom')
        assert map.containsKey('dateModif')
        assert map.containsKey('fk_districtId')
        id              = map['id']
        nom             = (map['nom'] as String).trim()
        dateModif       = map['dateModif'] // may be null
        fk_districtId   = (map['fk_districtId'] as String).trim()
        assert id != null && id >= 0
        assert nom
        assert fk_districtId
    }

    /**
     * Find the actual unqiue "District" instance corresponding to the store "fk_districtId"
     * and reference it.
     */

    void wireUp(List<District> districts) {
        assert districts != null
        def coll = districts.findAll { it.id == fk_districtId }
        assert coll.size() == 1
        district = coll.first()
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
        res << "\""
        if (dateModif) {
            res << ", "
            res << "dateModif:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << ", district:"
        if (district) {
            res << district.nom
        }
        else {
            res << fk_districtId
        }
        res << ")"
        return res as String
    }

    /**
     * Read entities from "reader"
     */

    static List<Canton> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def setOfNom = new HashSet()
        def setOfCode = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "2  : INT      : Code",
                    "40 : STR&TRIM : Nom",
                    "10 : DATE     : DsTimestampModif",
                    "4  : STR&TRIM : FkDistrictCode",
                    "1  : STR&TRIM : FkDistrictCodeNullIf")
            Map init = [:]
            init['id']              = values['Code']
            init['nom']             = values['Nom']
            init['dateModif']       = values['DsTimestampModif']
            init['fk_districtId']   = values['FkDistrictCode']
            def canton = new Canton(init)
            boolean added1 = setOfNom.add(canton.nom)
            boolean added2 = setOfCode.add(canton.id)
            assert added1 && added2
            res << canton
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<Canton> makeEntitiesFromResource(Class hook, TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(hook, "CANTON", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Canton> cantons = makeEntitiesFromResource(HookLocation.hook, tz)
        cantons.each {
            System.out << it << "\n"
        }
    }
}
