#!/bin/sh
#

ant \
    && rsync -vaP lib/performance-meters.jar /home/mblakele/lib/java/ \
    && rsync -vaP lib/performance-meters.jar ../performance-meters-gh-pages/ \
    && rsync -vaP lib/performance-meters.jar $HOME/lib/java/ \
    && rsync -vazP lib/performance-meters.jar ssh.marklogic.com:lib/java/
