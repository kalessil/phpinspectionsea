<?php

function cases_holder() {
    if ($path == '' && <error descr="'$path == 0' seems to be always true when reached.">$path == 0</error>) {}
    if ($path == '' && <error descr="'$path == null' seems to be always true when reached.">$path == null</error>) {}
    if ($path == '' && <error descr="'$path == false' seems to be always true when reached.">$path == false</error>) {}
    if ($path == '' && <error descr="'$path == []' seems to be always true when reached.">$path == []</error>) {}
    if ($path == '' && <error descr="'!$path' seems to be always true when reached.">!$path</error>) {}

    if ($path == '' && <error descr="'$path != 0' seems to be always false when reached.">$path != 0</error>) {}
    if ($path == '' && <error descr="'$path != null' seems to be always false when reached.">$path != null</error>) {}
    if ($path == '' && <error descr="'$path != false' seems to be always false when reached.">$path != false</error>) {}
    if ($path == '' && <error descr="'$path != []' seems to be always false when reached.">$path != []</error>) {}
    if ($path == '' && <error descr="'$path' seems to be always false when reached.">$path</error>) {}

    if ($path != '' && <error descr="'$path != 0' seems to be always true when reached.">$path != 0</error>) {}
    if ($path != '' && <error descr="'$path != null' seems to be always true when reached.">$path != null</error>) {}
    if ($path != '' && <error descr="'$path != false' seems to be always true when reached.">$path != false</error>) {}
    if ($path != '' && <error descr="'$path != []' seems to be always true when reached.">$path != []</error>) {}
    if ($path != '' && <error descr="'$path' seems to be always true when reached.">$path</error>) {}

    if ($path != '' && <error descr="'$path == 0' seems to be always false when reached.">$path == 0</error>) {}
    if ($path != '' && <error descr="'$path == null' seems to be always false when reached.">$path == null</error>) {}
    if ($path != '' && <error descr="'$path == false' seems to be always false when reached.">$path == false</error>) {}
    if ($path != '' && <error descr="'$path == []' seems to be always false when reached.">$path == []</error>) {}
    if ($path != '' && <error descr="'!$path' seems to be always false when reached.">!$path</error>) {}


    if ($path == '' || <error descr="'$path == 0' seems to be always false when reached.">$path == 0</error>) {}
    if ($path == '' || <error descr="'$path == null' seems to be always false when reached.">$path == null</error>) {}
    if ($path == '' || <error descr="'$path == false' seems to be always false when reached.">$path == false</error>) {}
    if ($path == '' || <error descr="'$path == []' seems to be always false when reached.">$path == []</error>) {}
    if ($path == '' || <error descr="'!$path' seems to be always false when reached.">!$path</error>) {}

    if ($path == '' || <error descr="'$path != 0' seems to be always true when reached.">$path != 0</error>) {}
    if ($path == '' || <error descr="'$path != null' seems to be always true when reached.">$path != null</error>) {}
    if ($path == '' || <error descr="'$path != false' seems to be always true when reached.">$path != false</error>) {}
    if ($path == '' || <error descr="'$path != []' seems to be always true when reached.">$path != []</error>) {}
    if ($path == '' || <error descr="'$path' seems to be always true when reached.">$path</error>) {}

    if ($path != '' || <error descr="'$path != 0' seems to be always false when reached.">$path != 0</error>) {}
    if ($path != '' || <error descr="'$path != null' seems to be always false when reached.">$path != null</error>) {}
    if ($path != '' || <error descr="'$path != false' seems to be always false when reached.">$path != false</error>) {}
    if ($path != '' || <error descr="'$path != []' seems to be always false when reached.">$path != []</error>) {}
    if ($path != '' || <error descr="'$path' seems to be always false when reached.">$path</error>) {}

    if ($path != '' || <error descr="'$path == 0' seems to be always true when reached.">$path == 0</error>) {}
    if ($path != '' || <error descr="'$path == null' seems to be always true when reached.">$path == null</error>) {}
    if ($path != '' || <error descr="'$path == false' seems to be always true when reached.">$path == false</error>) {}
    if ($path != '' || <error descr="'$path == []' seems to be always true when reached.">$path == []</error>) {}
    if ($path != '' || <error descr="'!$path' seems to be always true when reached.">!$path</error>) {}
}