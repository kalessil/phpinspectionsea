<?php

    echo <weak_warning descr="'null === ...' construction shall be used instead">is_null($x)</weak_warning>;
    echo !<weak_warning descr="'null !== ...' construction shall be used instead">is_null($x)</weak_warning>;

    echo is_null();