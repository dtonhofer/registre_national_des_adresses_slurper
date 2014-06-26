package eu.qleap.address.db.slurp.helpers


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
 * Functions that help in processing resources
 *
 * 2013.08.XX - First version
 ******************************************************************************/

class ParsingHelp {

    /**
     * Remove diacritics from a string of uppercase characters
     */

    static String deacriticize(String x) {
        assert x!=null
        StringBuilder res = new StringBuilder()
        x.toCharArray().each { char ch ->
            switch (ch) {
                case 'Ç':
                    res << 'C'
                    break
                case 'Ä':
                case 'Â':
                case 'À':
                case 'Á':
                    res << 'A'
                    break
                case 'Ê':
                case 'Ë':
                case 'É':
                case 'È':
                    res << 'E'
                    break
                case 'Ü':
                case 'Û':
                    res << 'U'
                    break
                case 'Î':
                case 'Ï':
                    res << 'I'
                    break
                case 'Ô':
                case 'Ö':
                    res << 'O'
                    break
                default:
                    res << ch
            }
        }
        return res as String
    }

    /**
     * Given a String "x", try to deduce a "Boolean" from it. Throws if that is impossible
     */

    static Boolean makeBoolean(String x) {
        assert x!=null
        def xlow = x.toLowerCase().trim()
        if (xlow == 'o' || xlow == 'y' || xlow == 't' || xlow == '1')  {
            return true
        }
        else if (xlow == 'n' || xlow == 'f' || xlow == '0')  {
            return false
        }
        else {
            throw new IllegalArgumentException("The passed string '${x}' cannot be transformed into a boolean")
        }
    }

    /**
     * Given a String DD.MM.YYYY, interprete it in TimeZone "tz" and generate the corresponding "Date"
     * No need to pull in JodaTime for this.
     */

    static Date makeDate(String x, TimeZone tz) {
        assert x!=null
        assert tz != null
        // x looks like "DD.MM.YYYY" or else is the empty string
        if (x =~ /^\d\d\.\d\d\.\d\d\d\d$/) {
            def day   = x.substring(0,2) as Integer
            def month = x.substring(3,5) as Integer
            def year  = x.substring(6,10) as Integer
            Calendar cal = Calendar.getInstance(tz)
            cal.set(year, month-1, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.getTime()
        }
        else if (x =~ /^\s*$/) {
            return null
        }
        else {
            throw new IllegalArgumentException("The passed string '${x}' does not look like a date")
        }
    }

    /**
     * Split the database dump file into several columns of various types (given by the "instructions")
     * Return a map fieldName --> value
     */

    static Map splitMgr(String line, TimeZone tz, String ... instructions) {
        assert line != null
        assert instructions != null
        assert instructions.length > 0
        Map res = [:]
        int curIndex = 0
        instructions.each { String inst ->
            //
            // What are the instructions
            //
            def instSplit = inst.split(":")
            assert instSplit.length == 3
            def fieldSize   = (instSplit[0].trim()) as Integer
            def fieldType   = (instSplit[1].trim())
            def fieldName   = (instSplit[2].trim())
            assert fieldSize > 0
            assert fieldType
            assert fieldName
            assert !res.containsKey(fieldName)
            //
            // Extract field data
            //
            String thisField = line.substring(curIndex, curIndex+fieldSize)
            curIndex += fieldSize
            //
            // Process ass indicated by the type
            //
            def value
            switch (fieldType) {
                case "BOOL":
                    value = makeBoolean(thisField)
                    break
                case "INT":
                    try {
                        thisField = thisField.trim()
                        if (thisField) {
                            value = (thisField as BigInteger)
                        }
                        else {
                            value = null
                        }
                    }
                    catch (Exception exe) {
                        throw new IllegalArgumentException("Bad number '${thisField}' named '${fieldName}'")
                    }
                    break
                case "STR":
                    value = thisField
                    break
                case "STR&TRIM":
                    value = thisField.trim()
                    break
                case "DATE":
                    value = makeDate(thisField, tz)
                    break
                default:
                    throw new IllegalArgumentException("Unhandled field type '${fieldType}' named '${fieldName}'")
            }
            res[fieldName] = value
        }
        assert curIndex == line.length()
        return res
    }

}
