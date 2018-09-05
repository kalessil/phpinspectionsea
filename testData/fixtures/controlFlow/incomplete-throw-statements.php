<?php

class MyException extends RuntimeException {}
class MyStdClass extends stdClass          {}

function cases_holder() {
    <error descr="It's probably intended to throw an exception here.">new MyException()</error>;
    throw <error descr="It's probably intended to instantiate the exception here.">MyException()</error>;
    throw new MyException(<error descr="It's probably intended to use 'sprintf(...)' here.">'%s'</error>);

    throw new MyException();

    new MyStdClass();
    new MyStdClass('%s');
}