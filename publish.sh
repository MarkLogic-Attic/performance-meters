#!/bin/sh
#

set -e

ant jar \
    && rsync -vaP lib/performance-meters.jar ../performance-meters-gh-pages/ \
    && rsync -vaP lib/performance-meters.jar $HOME/lib/java/ \
    && rsync -vazP lib/performance-meters.jar ssh.marklogic.com:lib/java/

date