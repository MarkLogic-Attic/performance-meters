#!/bin/sh
#

CP=$HOME/lib/java/performance-meters.jar
CP=$CP:$HOME/lib/java/xcc.jar

$JAVA_HOME/bin/java -cp $CP com.marklogic.performance.PerformanceMeters $*

# end performance-meters.sh
