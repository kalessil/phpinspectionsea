<?php

    echo true  && <weak_warning descr="[EA] 'null === $x' construction should be used instead.">is_null($x)</weak_warning>;
    echo false || <weak_warning descr="[EA] 'null === $x' construction should be used instead.">is_null($x)</weak_warning>;

    echo <weak_warning descr="[EA] 'null === $x' construction should be used instead.">is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null === $x' construction should be used instead.">true  == is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null === $x' construction should be used instead.">false != is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null === $x' construction should be used instead.">is_null($x) === true</weak_warning>;
    echo <weak_warning descr="[EA] 'null === $x' construction should be used instead.">is_null($x) !== false</weak_warning>;

    echo <weak_warning descr="[EA] 'null !== $x' construction should be used instead.">!is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null !== $x' construction should be used instead.">false == is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null !== $x' construction should be used instead.">true  != is_null($x)</weak_warning>;
    echo <weak_warning descr="[EA] 'null !== $x' construction should be used instead.">is_null($x) === false</weak_warning>;
    echo <weak_warning descr="[EA] 'null !== $x' construction should be used instead.">is_null($x) !== true</weak_warning>;

    echo <weak_warning descr="[EA] 'null !== ($x = null)' construction should be used instead.">is_null($x = null) !== true</weak_warning>;

    echo is_null();