<?php

namespace CasesHolder {
    abstract class InvalidCase1 implements \<error descr="[EA] Provokes fatal error: '\Traversable' should be replaced with one of [\Iterator, \IteratorAggregate] instead.">Traversable</error> {}
    interface ValidCase1 extends \Traversable {}

    abstract class InvalidCase2 implements \<error descr="[EA] Provokes fatal error: '\DateTimeInterface' can not be implemented by user classes.">DateTimeInterface</error>, \Traversable {}
    interface ValidCase2 extends \DateTimeInterface, \Traversable {}

    abstract class InvalidCase3 implements \ArrayAccess, \<error descr="[EA] Provokes fatal error: '\Traversable' should be replaced with one of [\Iterator, \IteratorAggregate] instead.">Traversable</error> {}
    interface ValidCase3 extends \ArrayAccess, \Traversable {}

    abstract class InvalidCase4 implements \<error descr="[EA] Provokes fatal error: '\Traversable' should be replaced with one of [\Iterator, \IteratorAggregate] instead.">Traversable</error>, \Iterator {}
    interface ValidCase4 extends \Traversable, \Iterator {}

    abstract class InvalidCase6 implements \<error descr="[EA] Provokes fatal error: '\Throwable' should be replaced with one of [\Exception, \Error] instead.">Throwable</error> {}
    interface ValidCase6 extends \Throwable {}


    abstract class ValidCase7 implements \Iterator {}
}