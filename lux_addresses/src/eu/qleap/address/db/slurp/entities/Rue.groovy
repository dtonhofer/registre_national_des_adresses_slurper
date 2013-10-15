package eu.qleap.address.db.slurp.entities

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
 * "Rue" entity
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class Rue {

    // Filled-in via constructor

    final Integer id
    final String  nom
    final String  nomAbrégé
    final String  motTri
    final Integer codeNomenclature
    final Boolean isLieuDit
    final Boolean isProvisoire
    final Date    dateFinValidité
    final Date    dateModif
    final String  fk_codePontChausséesTypeRue
    final String  fk_codePontChausséesNuméroRue
    final Integer fk_localitéId

    // Filled-in later based on "fk_localitéId"

    Localité localité

    // Filled-in after "AliasRue" have been read

    final List<AliasRue> aliases = []

    // Filled-in after "Immeubles" have been read; not null only in certain cases
    
    final List<Quartier> quartiers = []
    
    /**
     * Constructor
     */

    Rue(Map map) {
        assert map
        assert(map.containsKey('id'))
        assert(map.containsKey('nom'))
        assert(map.containsKey('motTri'))
        assert(map.containsKey('nomAbrégé'))
        assert(map.containsKey('codeNomenclature'))
        assert(map.containsKey('isLieuDit'))
        assert(map.containsKey('dateFinValidité'))
        assert(map.containsKey('dateModif'))
        assert(map.containsKey('fk_codePontChausséesTypeRue'))
        assert(map.containsKey('fk_codePontChausséesNuméroRue'))
        assert(map.containsKey('fk_localitéId'))
        assert(map.containsKey('isProvisoire'))
        id                            = map['id']
        nom                           = map['nom']
        motTri                        = map['motTri']
        nomAbrégé                     = map['nomAbrégé']
        codeNomenclature              = map['codeNomenclature']
        isLieuDit                     = map['isLieuDit']
        dateFinValidité               = map['dateFinValidité']
        dateModif                     = map['dateModif']
        fk_codePontChausséesTypeRue   = map['fk_codePontChausséesTypeRue']
        fk_codePontChausséesNuméroRue = map['fk_codePontChausséesNuméroRue']
        fk_localitéId                 = map['fk_localitéId']
        isProvisoire                  = map['isProvisoire']
        assert id != null && id > 0
        assert nom
        assert motTri
        assert nomAbrégé
        assert isLieuDit != null
        assert isProvisoire != null
        assert fk_localitéId != null && fk_localitéId > 0
    }

    /**
     * Helper
     */
    
    boolean isAssignedToQuartier(Quartier q) {
        assert q
        def coll = quartiers.findAll {
            // one could use instance equality, but here, compare on localité and id
            it.id == q.id && it.fk_localitéId == q.fk_localitéId
        }
        assert coll.size() <= 1
        return !coll.isEmpty()
    }
    
    /**
     * Find the actual unqiue "Localité" instance corresponding to the store "fk_localitéId"
     * and reference it.
     */

    void wireUp(List<Localité> localités) {
        assert localités != null
        def coll = localités.findAll { it.id == fk_localitéId }
        assert coll.size() == 1
        localité = coll.first()
    }

    /**
     * Stringify
     */

    String toString() {
        def res = new StringBuilder()
        res << this.getClass().name
        res << "("
        res << id
        res << ", \""
        res << nom
        res << "\", \""
        res << nomAbrégé
        res << "\", \""
        res << motTri
        res << "\""
        if (codeNomenclature) {
            res << ", "
            res << "codeNomenclature:" << codeNomenclature
        }
        if (isLieuDit) {
            res << ", "
            res << "lieu dit"
        }
        if (isProvisoire) {
            res << ", "
            res << "provisoire"
        }
        if (dateFinValidité) {
            res << ", "
            res << "fin validité:" << String.format('%tF', dateFinValidité)
        }
        if (dateModif) {
            res << ", "
            res << "modifié:" << String.format('%tF', dateModif)
        }
        if (fk_codePontChausséesTypeRue) {
            res << ", "
            res << "type rue P&Ch:" << fk_codePontChausséesTypeRue
        }
        if (fk_codePontChausséesNuméroRue) {
            res << ", "
            res << "numéro rue P&Ch:"<<  fk_codePontChausséesNuméroRue
        }
        res << ", "
        res << "localité:"
        if (localité) {
            res << localité.nom
        }
        else {
            res << fk_localitéId
        }
        // aliases on the same line
        aliases.each { it ->
            assert it != null
            res << " -- alias -- " << it
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
        def setOfId   = new HashSet()
        def setOfName = new HashSet()
        reader.eachLine { String line ->
            Map values = ParsingHelp.splitMgr(line, tz,
                    "5  : INT      : Numéro",
                    "40 : STR&TRIM : Nom",
                    "40 : STR&TRIM : NomMaj",
                    "10 : STR&TRIM : MotTri",
                    "5  : INT      : CodeNomenclature",
                    "1  : STR&TRIM : CodeNomenclatureNullIf",
                    "1  : BOOL     : IndicLieuDit",
                    "10 : DATE     : DateFinValid",
                    "1  : STR&TRIM : DateFinValidNullIf",
                    "10 : DATE     : DsTimestampModif",
                    "2  : STR&TRIM : FkCptchTypeRue",
                    "1  : STR&TRIM : FkCptchTypeRueNullIf",
                    "4  : STR&TRIM : FkCptchNumeroRue",
                    "1  : STR&TRIM : FkCptchNumeroRueNullIf",
                    "5  : INT      : FkLocalNuméro",
                    "1  : STR&TRIM : FkLocalNuméroNullIf",
                    "1  : BOOL     : IndicProvisoire",
                    "30 : STR&TRIM : NomAbrégé")
            Map init = [:]
            init['id']                            = values['Numéro']
            init['nom']                           = values['Nom']
            init['motTri']                        = values['MotTri']
            init['nomAbrégé']                     = values['NomAbrégé']
            init['codeNomenclature']              = values['CodeNomenclature']
            init['isLieuDit']                     = values['IndicLieuDit']
            init['dateFinValidité']               = values['DateFinValid']
            init['dateModif']                     = values['DsTimestampModif']
            init['fk_codePontChausséesTypeRue']   = values['FkCptchTypeRue']
            init['fk_codePontChausséesNuméroRue'] = values['FkCptchNumeroRue']
            init['fk_localitéId']                 = values['FkLocalNuméro']
            init['isProvisoire']                  = values['IndicProvisoire']
            assert ParsingHelp.deacriticize(values['Nom'].toUpperCase()) == values['NomMaj']
            Rue rue = new Rue(init)
            String compositeName = "${rue.nom};${rue.fk_localitéId}"
            if (rue.dateFinValidité != null) {
                compositeName = "${compositeName}+${rue.dateFinValidité}"
            }
            boolean added1 = setOfId.add(rue.id)
            boolean added2 = setOfName.add(compositeName)
            assert added1 && added2
            res << rue
        }
        return res
    }

    /**
     * Open the resource and read entities
     */

    static List<Rue> makeEntitiesFromResource(TimeZone tz) {
        assert tz != null
        String txt = ResourceHelp.slurpResource(Hook.class, "RUE", "ISO-8859-1")
        (new StringReader(txt)).withReader { reader ->
            return readEntities(reader, tz)
        }
    }

    /**
     * Test run
     */

    static void main(def argv) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        List<Rue> rues = makeEntitiesFromResource(tz)
        rues.each {
            System.out << it << "\n"
        }
    }
}
