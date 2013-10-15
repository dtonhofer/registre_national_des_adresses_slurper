package eu.qleap.address.db.slurp.entities

import java.io.Reader;
import java.util.Collection;
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
 * "Commune" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class Commune {

    // Set at construction time

    final String  nom // name of municipality (uppercased name is not retained); also the id
    final Integer seqNumberInCanton // unique id within the canton; >= 0
    final Date    dateModif // modified when (precision is day); may be null  
    final Integer fk_cantonId // reference to owning canton; not null;  >= 0

    // wired up later from fk_codeCanton

    Canton canton

    /**
     * Constructor
     */

    Commune(Map map) {
        assert map
        assert map.containsKey('seqNumberInCanton')
        assert map.containsKey('nom')
        assert map.containsKey('dateModif')
        assert map.containsKey('fk_cantonId')
        nom               = (map['nom'] as String).trim()
        seqNumberInCanton = map['seqNumberInCanton']        
        dateModif         = map['dateModif'] // may be null
        fk_cantonId       = map['fk_cantonId']
        assert nom
        assert seqNumberInCanton != null && seqNumberInCanton >= 0
        assert fk_cantonId != null && fk_cantonId >= 0
    }
    
    /**
     * Wire up this Commune to its Canton
     */

    void wireUp(List<Canton> cantons) {
        assert cantons != null
        def coll = cantons.findAll { Canton canton -> canton.id == fk_cantonId }
        assert coll.size() == 1
        canton = coll.first()
    }

    /**
     * Stringification
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << "\""
        res << nom
        res << "\""
        if (dateModif) {
            res << ", "
            res << "dateModif:" << String.format('%tF', dateModif)
        }
        res << ", "
        res << "canton:"
        if (canton) {
            res << canton.nom
        }
        else {
            res << fk_cantonId
        }
        res << ", "
        res << "seqNumberInCanton:" << seqNumberInCanton        
        res << ")"
        return res as String
    }

    /**
     * Find the "communes" from "allCommunes" that belong to "the Canton"
     */

    static Collection<Commune> communesOfCanton(Collection<Commune> allCommunes, Canton theCanton) {
        assert allCommunes != null
        assert theCanton
        return allCommunes.findAll { Commune x ->
            assert x.canton : "Commune ${x.nom} is not wired"
            theCanton.nom == x.canton.nom
            // theCanton.is(x.canton)
        }
    }

    /**
     * Read entities from "reader"
     */

    static List<Commune> readEntities(Reader reader, TimeZone tz) {
        assert reader !=null
        def res = []
        def setOfNom               = new HashSet()
        def setOfSeqNumberInCanton = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "2  : INT       : Code",
                    "40 : STR&TRIM  : Nom",
                    "40 : STR&TRIM  : NomMajuscule",
                    "10 : DATE      : DsTimestampModif",
                    "2  : INT       : FkCantoCode")
            Map init = [:]
            init['nom']               = values['Nom']
            init['dateModif']         = values['DsTimestampModif']            
            init['seqNumberInCanton'] = values['Code'] // sequential number within canton
            init['fk_cantonId']       = values['FkCantoCode']
            assert ParsingHelp.deacriticize(values['Nom'].toUpperCase()) == values['NomMajuscule']
            def commune = new Commune(init)
            boolean added1 = setOfNom.add(commune.nom)            
            def compositeKey = "${commune.fk_cantonId};${commune.seqNumberInCanton}"
            boolean added2 = setOfSeqNumberInCanton.add(compositeKey)
            assert added1 && added2
            res << commune
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<Commune> makeEntitiesFromResource(TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(Hook.class, "COMMUNE", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Commune> communes = makeEntitiesFromResource(tz)
        communes.each {
            System.out << it << "\n"
        }
    }

}
