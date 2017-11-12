<?php

simplexml_load_file();

simplexml_load_string(file_get_contents($filename));
simplexml_load_string(file_get_contents($filename), $class_name);
simplexml_load_string(file_get_contents($filename), $class_name, $options);
simplexml_load_string(file_get_contents($filename), $class_name, $options, $ns);
simplexml_load_string(file_get_contents($filename), $class_name, $options, $ns, $is_prefix);
