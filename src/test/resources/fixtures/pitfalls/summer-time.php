<?php

    echo $x + (<error descr="Consider using \DateTime for DST safe date/time manipulation.">24 * 3600</error>);
    echo (<error descr="Consider using \DateTime for DST safe date/time manipulation.">60 * 60 * 24</error>) + $x;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x + 86400</error>;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">86400 + $x</error>;
    echo $x - (<error descr="Consider using \DateTime for DST safe date/time manipulation.">24 * 3600</error>);
    echo (<error descr="Consider using \DateTime for DST safe date/time manipulation.">60 * 60 * 24</error>) - $x;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x - 86400</error>;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">86400 - $x</error>;
    echo $x * (<error descr="Consider using \DateTime for DST safe date/time manipulation.">24 * 3600</error>);
    echo (<error descr="Consider using \DateTime for DST safe date/time manipulation.">60 * 60 * 24</error>) * $x;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x * 86400</error>;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">86400 * $x</error>;
    echo $x / (<error descr="Consider using \DateTime for DST safe date/time manipulation.">24 * 3600</error>);
    echo (<error descr="Consider using \DateTime for DST safe date/time manipulation.">60 * 60 * 24</error>) / $x;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x / 86400</error>;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">86400 / $x</error>;
    echo $x % (<error descr="Consider using \DateTime for DST safe date/time manipulation.">24 * 3600</error>);
    echo (<error descr="Consider using \DateTime for DST safe date/time manipulation.">60 * 60 * 24</error>) % $x;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x % 86400</error>;
    echo <error descr="Consider using \DateTime for DST safe date/time manipulation.">86400 % $x</error>;

    /* false-positives: 60/3600 companion numbers are not in the context */
    echo $x + 24;
    echo 24 + $x;

    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x += 24</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x += 86400</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x -= 24</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x -= 86400</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x *= 24</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x *= 86400</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x /= 24</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x /= 86400</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x %= 24</error>;
    <error descr="Consider using \DateTime for DST safe date/time manipulation.">$x %= 86400</error>;