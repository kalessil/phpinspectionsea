<?php

shell_exec("");
shell_exec("double-quote should be escaped: \"");

echo shell_exec("without space between echo and backtick");
