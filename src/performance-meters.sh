#!/bin/sh
#

CP=$HOME/lib/java/performance-meters.jar
CP=$CP:$HOME/lib/java/xcc.jar

FILES=
VMARGS=

for a in $*; do
    if [ -e $a ]; then
        FILES="$FILES $a"
    else
        VMARGS="$VMARGS $a"
    fi
done

if [ -d "$JAVA_HOME" ]; then
  JAVA=$JAVA_HOME/bin/java
else
  JAVA=java
fi

$JAVA -cp $CP $VMARGS com.marklogic.performance.PerformanceMeters $FILES

# end performance-meters.sh
