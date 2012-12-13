JarFinder
=========

JarFinder is used to find a given class inside jar files.

Usage: `java com.mozilla.jarfinder.JarFinder classname dir [ dir2 ] [ dir3 ] [ ... ]`

Example: 

    $ java com.mozilla.jarfinder.JarFinder org.eclipse.mylyn.internal.commons.core.XmlStringConverter /home/yourname/eclipse/3.7/
    Search results for 'org.eclipse.mylyn.internal.commons.core.XmlStringConverter':
    /home/yourname/eclipse/3.7/eclipse/plugins/org.eclipse.mylyn.commons.core_3.6.0.v20110608-1400.jar contains the class 'org.eclipse.mylyn.internal.commons.core.XmlStringConverter'

You can also specify a bare classname to search entire jars:

    $ java com.mozilla.jarfinder.JarFinder UserDetails /home/yourname/spring-security/
    Search results for 'UserDetails':
    /home/yourname/spring-security/spring-security-core-3.1.2.RELEASE.jar contains the class 'org.springframework.security.core.userdetails.UserDetails'
