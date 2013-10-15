package eu.qleap.address.db.slurp.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

class ResourceHelp {

    private final static CLASS = ResourceHelp.class.name
    private final static Logger LOGGER_slurpResource = LoggerFactory.getLogger("${CLASS}.slurpResource")
    private final static Logger LOGGER_getStreamFromResource = LoggerFactory.getLogger("${CLASS}.getStreamFromResource")
    
    /**
     * This qualifies the resource name by the package of the Class "clazz".
     * If "clazz" is null, default package is assumed.
     * If the "nonQualifiedResourceNameIn" is null, null will be returned.
     */

    static String fullyQualifyResourceName(Class clazz, String nonQualifiedResourceName) {
        return fullyQualifyResourceName((Package)(clazz?.getPackage()), nonQualifiedResourceName)
    }

    /**
     * This qualifies the resource name by the package "packijj".
     * If "packijj" is null, default package is assumed.
     * If the "nonQualifiedResourceNameIn" is null, null will be returned.
     */

    static String fullyQualifyResourceName(Package packijj, String nonQualifiedResourceName) {
        return fullyQualifyResourceName((String)(packijj?.name), nonQualifiedResourceName)
    }

    /**
     * This qualifies the resource name by the packijjName, whereby "." is replaced by "/".
     * If "packageName" is null, default package is assumed.
     * If the "nonQualifiedResourceName" is null, null will be returned.
     */

    static String fullyQualifyResourceName(String packijjName, String nonQualifiedResourceName) {
        if (nonQualifiedResourceName == null) {
            return null;
        }
        String res = nonQualifiedResourceName.trim()
        if (res.isEmpty()) {
            throw new IllegalArgumentException("The passed 'non-qualified resource name' is empty");
        }
        if (res.indexOf('/')>=0) {
            throw new IllegalArgumentException("The passed 'non-qualified resource name' contains a '/': '${res}'");
        }
        if (packijjName == null) {
            return res
        }
        return packijjName.replace('.', '/') + "/" + res
    }

    /**
     * Read a String from a Resource given as a "hook class" (possibly null) which is supposed to reside in the same
     * package as the resource, and a non-qualified resource name. Throws if the resource could not be found.
     * A non-null encoding must be passed.
     */

    static String slurpResource(Class hookClazz, String nonQualifiedResourceName, String encoding) {
        String fqResourceName = fullyQualifyResourceName(hookClazz, nonQualifiedResourceName)
        return slurpResource(fqResourceName, encoding);
    }

    /**
     * Read a String from a Resource given as a "package" (possibly null) and a non-qualified resource name.
     * Throws if the resource could not be found.
     * A non-null encoding must be passed.
     */

    static String slurpResource(Package packijj, String nonQualifiedResourceName, String encoding) {
        String fqResourceName = fullyQualifyResourceName(packijj, nonQualifiedResourceName);
        return slurpResource(fqResourceName, encoding);
    }

    /**
     * Get a binary input stream from a resource. Throws IllegalArgumentException if the resource could not be found. Never returns null.
     */

    static InputStream getStreamFromResource(String fullyQualifiedResourceName) {
        def logger = LOGGER_getStreamFromResource;
        if (!fullyQualifiedResourceName || !(fullyQualifiedResourceName.trim())) {
            throw new IllegalArgumentException("The passed 'fully-qualified resource name' is null or empty");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //
        // the following call returns (null) if resource not found (instead of throwing)
        //
        InputStream is = classLoader.getResourceAsStream(fullyQualifiedResourceName.trim());
        if (!is) {
            throw new IllegalArgumentException("Could not find (fully qualified) resource named '${fullyQualifiedResourceName}'");
        }
        //
        // If we are here, loading the resource was successful!
        //
        if (logger.isInfoEnabled()) {
            URL url = classLoader.getResource(fullyQualifiedResourceName);
            logger.info("Resource '${fullyQualifiedResourceName}' accessed as '${url}'");
        }
        return is;
    }
    
    /**
     * Read a String from a Resource given as a fully qualified resource name.
     * Throws if the resource could not be found.
     * A non-null encoding must be passed.
     */

    static String slurpResource(String fullyQualifiedResourceName, String encoding) {
        def logger = LOGGER_slurpResource;
        InputStream is = getStreamFromResource(fullyQualifiedResourceName)
        try {
            String res
            is.withReader(encoding) {
                r -> res = r.text
            }
        } finally {
            try {
                is.close();
            } catch (Exception ignore) {
                logger.warn("While closing input stream -- ignoring this!", ignore);
            }
        }
    }

}
