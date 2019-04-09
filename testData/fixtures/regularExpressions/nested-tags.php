<?php

    preg_match(<weak_warning descr="/s modifier is probably missing (not matching multiline tag content).">'/<tag>.+</tag>/'</weak_warning>, '...');
    preg_match(<weak_warning descr="/s modifier is probably missing (not matching multiline tag content).">'/<tag>.+?</tag>/'</weak_warning>, '...');
    preg_match(<weak_warning descr="/s modifier is probably missing (not matching multiline tag content).">'/<tag>.*</tag>/'</weak_warning>, '...');
    preg_match(<weak_warning descr="/s modifier is probably missing (not matching multiline tag content).">'/<tag>.*?</tag>/'</weak_warning>, '...');

    preg_match('/<tag>.*</tag>/s', '...');