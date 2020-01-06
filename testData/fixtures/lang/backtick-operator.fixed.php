<?php

shell_exec("double-quote should be escaped: \"");
shell_exec("variables should be preserved: $variable");
return shell_exec("without space between echo and backtick");

``;
