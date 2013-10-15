package eu.qleap.address.db.slurp.main;

import eu.qleap.address.db.slurp.entities.AliasLocalité
import eu.qleap.address.db.slurp.entities.AliasRue
import eu.qleap.address.db.slurp.entities.Canton
import eu.qleap.address.db.slurp.entities.Commune
import eu.qleap.address.db.slurp.entities.District
import eu.qleap.address.db.slurp.entities.Immeuble
import eu.qleap.address.db.slurp.entities.Localité
import eu.qleap.address.db.slurp.entities.Quartier
import eu.qleap.address.db.slurp.entities.Rue

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
 * The in-memory structure holding all the entities read from resources.
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class NationalTree {

    final List<District> districts
    final List<Canton>   cantons
    final List<Commune>  communes
    final List<Localité> localités
    final List<Quartier> quartiers
    final List<Rue>      rues
    final List<Immeuble> immeubles

    /**
     * Constructor
     */

    NationalTree(boolean printThem) {
        def tz = TimeZone.getTimeZone("Europe/Luxembourg")
        districts = Collections.unmodifiableList(makeDistricts(tz, printThem))
        cantons   = Collections.unmodifiableList(makeCantons(districts,  tz, printThem))
        communes  = Collections.unmodifiableList(makeCommunes(cantons,  tz, printThem))
        localités = Collections.unmodifiableList(makeLocalités(communes, tz))
        quartiers = Collections.unmodifiableList(makeQuartiers(localités, tz))
        rues      = Collections.unmodifiableList(makeRues(localités, tz))
        immeubles = Collections.unmodifiableList(makeImmeubles(rues, quartiers, tz))
        linkRueToQuartier(immeubles, rues, quartiers)
    }

    /**
     * Helper
     */

    private static List<District> makeDistricts(TimeZone tz, boolean printThem) {
        List<District> districts = District.makeEntitiesFromResource(tz)
        if (printThem) {
            districts.each {
                System.out << it << "\n"
            }
        }
        return districts
    }

    /**
     * Helper
     */

    private static List<Canton> makeCantons(List<District> districts, TimeZone tz, boolean printThem) {
        List<Canton> cantons = Canton.makeEntitiesFromResource(tz)
        cantons.each {  it.wireUp(districts) }
        if (printThem) {
            cantons.each {
                System.out << it << "\n"
            }
        }
        return cantons
    }

    /**
     * Helper
     */

    private static List<Commune> makeCommunes(List<Canton> cantons, TimeZone tz, boolean printThem) {
        List<Commune> communes = Commune.makeEntitiesFromResource(tz)
        communes.each { it.wireUp(cantons) }
        if (printThem) {
            communes.each {
                System.out << it << "\n"
            }
        }
        return communes
    }

    /**
     * Helper
     */

    private static List<Localité> makeLocalités(List<Commune> communes, TimeZone tz) {
        List<Localité> localités = Localité.makeEntitiesFromResource(tz)
        localités.each {
            boolean itWorked = it.wireUp(communes)
            if (!itWorked) {
                System.out << "WARNING: Could not wire up: ${it} -- disregarding this problem!" << "\n"
            }
        }
        //
        // Connect aliases to localités
        //
        List<AliasLocalité> aliases = AliasLocalité.makeEntitiesFromResource(tz)
        //
        // No need to be efficient in search (go with an n² algorithm); just make sure all the aliases are handled
        //
        aliases.each { AliasLocalité alias ->
            def coll = localités.findAll { Localité loc ->
                alias.fk_localitéId == loc.id
            }
            assert coll.size() == 1
            Localité theLoc = coll.first()
            theLoc.aliases << alias
            // sort aliases in place using the AliasLocalité natural ordering, which is by "numéro séquentiel"
            theLoc.aliases.sort(true)
        }
        return localités
    }

    /**
     * Helper
     */

    private static List<Quartier> makeQuartiers(List<Localité> localités, TimeZone tz) {
        List<Quartier> quartiers = Quartier.makeEntitiesFromResource(tz)
        quartiers.each { it.wireUp(localités) }
    }

    /**
     * Helper
     */

    private static List<Rue> makeRues(List<Localité> localités, TimeZone tz) {
        List<Rue> rues = Rue.makeEntitiesFromResource(tz)
        rues.each { it.wireUp(localités) }
        //
        // Connect aliases to rues
        //
        List<AliasRue> aliases = AliasRue.makeEntitiesFromResource(tz)
        //
        // No need to be efficient in search (go with an n² algorithm); just make sure all the aliases are handled
        //
        aliases.each { AliasRue alias ->
            def coll = rues.findAll { Rue rue ->
                alias.fk_rueId == rue.id
            }
            assert coll.size() == 1
            Rue theRue = coll.first()
            theRue.aliases << alias
            // sort aliases in place using the AliasRue natural ordering, which is by "numéro séquentiel"
            theRue.aliases.sort(true)
        }
        return rues
    }

    /**
     * Helper
     */

    private static List<Immeuble> makeImmeubles(List<Rue> rues, List<Quartier> quartiers, TimeZone tz) {
        List<Immeuble> immeubles = Immeuble.makeEntitiesFromResource(tz)
        def rueMap = [:]
        rues.each { Rue rue ->
            assert !rueMap.containsKey(rue.id)
            rueMap[rue.id] = rue
        }
        immeubles.each { it.wireUp(rueMap, quartiers) }
    }


    /**
     * Helper
     */

    private static void linkRueToQuartier(List<Immeuble> immeubles, List<Rue> rues, List<Quartier> quartiers) {
        immeubles.each { Immeuble immo ->
            Rue rueOfImmo = immo.rue
            assert rueOfImmo
            Quartier quartierOfImmo = immo.quartier
            if (immo.quartier) {
                if (!rueOfImmo.isAssignedToQuartier(quartierOfImmo)) {
                    rueOfImmo.quartiers << quartierOfImmo
                }
            }
        }
        rues.each { Rue rue ->
            assert rue.quartiers.size() <= 1 : "${rue} is assigned to ${rue.quartiers.size()} quartiers"
            if (rue.quartiers.isEmpty() <=> !rue.localité.hasQuartiers) {
                System.out.println "WARNING: ${rue} belongs to a quartier: ${!rue.quartiers.isEmpty()} but its localité ${rue.localité} has quartiers: ${rue.localité.hasQuartiers}"
            }
        }
    }
}
