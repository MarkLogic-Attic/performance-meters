#!/bin/sh
#

CP=$HOME/lib/java/performance-meters.jar
CP=$CP:$HOME/lib/java/xdbc.jar
CP=$CP:$HOME/lib/java/xdmp.jar

$JAVA_HOME/bin/java -cp $CP com.marklogic.performance.PerformanceMeters $*

# end performance-meters.sh
