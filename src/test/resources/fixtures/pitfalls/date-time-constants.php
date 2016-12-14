<?php
    echo <error descr="The format is not compatible with ISO-8601. Use DateTime::ATOM/DATE_ATOM for compatibility with ISO-8601 instead.">DATE_ISO8601</error>;
    echo DateTime::<error descr="The format is not compatible with ISO-8601. Use DateTime::ATOM/DATE_ATOM for compatibility with ISO-8601 instead.">ISO8601</error>;


    /* false-positives */
    echo __DATE_ISO8601;

    class ClassWithIsoConstant {
        const ISO8601 = '';
    }
    echo ClassWithIsoConstant::ISO8601;