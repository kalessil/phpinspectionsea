<?php

if (!<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning>)          {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning>)           {}

if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> || false)  {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> or false)  {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> OR false)  {}

if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> && true)   {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> and true)  {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</weak_warning> AND true)  {}

if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array()) === false</weak_warning>) {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">false === array_search('', array())</weak_warning>) {}

if (array_search('', array()) !== <error descr="This makes no sense, as array_search(...) never returns true.">true</error>)   {}
if (<error descr="This makes no sense, as array_search(...) never returns true.">true</error> !== array_search('', array()))   {}

if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array()) !== false</weak_warning>) {}
if (<weak_warning descr="'in_array(...)' should be used instead (clearer intention).">false !== array_search('', array())</weak_warning>) {}

if (array_search('', array()) === <error descr="This makes no sense, as array_search(...) never returns true.">true</error>)   {}
if (<error descr="This makes no sense, as array_search(...) never returns true.">true</error> === array_search('', array()))   {}
