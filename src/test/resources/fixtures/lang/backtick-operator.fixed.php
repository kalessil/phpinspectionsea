<?php

shell_exec("double-quote should be escaped: \"");
return shell_exec("without space between echo and backtick");

``;
