package eu.qleap.address.db.slurp.main

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
 * Create the in-memory structure from resources, print results.
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class MainLoadNationalTree {
   
    static printLocalités(List<Localité> localités, List<Quartier> quartiers, List<Rue> rues) {
        localités.each { Localité loc ->
            System.out << loc << "\n"
            //
            // list quartiers hanging underneath
            //
            if (loc.hasQuartiers) {
                quartiers.each { Quartier q ->
                    if (q.localité.id == loc.id) {
                        System.out << "     " << q << "\n"
                    }
                }
            }
            //
            // in a reversal of the structure, list the "rues" which belong to "localité"
            //
            def rueColl = loc.findRues(rues)
            rueColl.each { Rue rue ->
                System.out << "     " << rue << "\n"
            }
        }
    }

    static printImmeubles(List<Immeuble> immeubles) {
        immeubles.each { Immeuble immo ->
            System.out << immo << "\n"
        }
    }

    static csvLocalités(List<Localité> localités) {
        Map locMap = [:]
        int maxLocColumns = 0
        localités.each { Localité loc ->
            if (loc.dateFinValidité == null) {
                assert !locMap.containsKey(loc.nom)
                List csv = loc.toCSV()
                locMap[loc.nom] = csv 
                maxLocColumns = Math.max(maxLocColumns, csv.size())
            }
        }
        locMap.keySet().sort().each {
            List csv = locMap[it]
            boolean addComma = false
            csv.each {
                if (addComma) {
                    System.out << ","
                }
                System.out << it
                addComma = true
            }
            for (int i = csv.size(); i<maxLocColumns; i++) {
                if (addComma) {
                    System.out << ","
                }
                addComma = true
            }
            System.out << "\n"
        }
    }

    static void main(def argv) {
        //
        // Load data from resources in one step
        //        
        NationalTree tc = new NationalTree(HookLocation.hook, true)
        //
        // Print!
        //        
        // printLocalités(tc.localités, tc.quartiers, tc.rues)
        // printImmeubles(tc.immeubles)
        csvLocalités(tc.localités)
        System.out << "DONE\n"
    }
}
