<?php

class MyException extends RuntimeException {}
throw new MyException();

<error descr="It's probably intended to throw an exception here.">new MyException()</error>;
throw new MyException(<error descr="It's probably intended to use sprintf here.">'%s'</error>);

class MyStdClass extends stdClass {}
new MyStdClass();
new MyStdClass('%s');