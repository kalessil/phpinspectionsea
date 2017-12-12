<?php

class MyException extends RuntimeException {}
class MyStdClass extends stdClass          {}

function cases_holder() {
    throw new MyException();
    throw new MyException();
    throw new MyException(sprintf('%s', ));

    throw new MyException();

    new MyStdClass();
    new MyStdClass('%s');
}