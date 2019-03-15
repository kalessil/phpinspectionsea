#/bin/sh
if [ -z "$INSPECTIONCONFIGFILE" ]
    then INSPECTIONCONFIGFILE=/var/ci/project/.idea/inspectionProfiles/Project_Default.xml
fi
if [ -z "$INSPECTIONRESULTFILE" ]
    then INSPECTIONRESULTFILE=/var/ci/phpinspections_cs.xml
fi
if [ -n "$INSPECTIONCONFIG" ]
    then echo $INSPECTIONCONFIG > /var/ci/Project_Default.xml
    INSPECTIONCONFIGFILE=/var/ci/Project_Default.xml
fi

rm -f ${INSPECTIONRESULTFILE} >> /dev/null 2>&1
/vendor/bin/phpstorm-inspect `ls -d /PhpStorm-*/`bin/inspect.sh /root/.PhpStorm${PHPSTORMVERSION}/system /var/ci ${INSPECTIONCONFIGFILE} /var/ci/project checkstyle > ${INSPECTIONRESULTFILE}
(stat ${INSPECTIONRESULTFILE} >> /dev/null 2>&1 && echo 'Inspections results: exported') || echo 'Inspections results: missing'