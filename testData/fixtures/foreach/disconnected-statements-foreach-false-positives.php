<?php

/* @var array $files */
/* @var array $processed */
/* @var stdClass $processedBag */
/* @var string $hash */

/* dependencies resolution correctness */
foreach ($files as & $file1) {
    $file1 .= "?$hash";
}
foreach ($files as & $file2) {
    $file2 .= '?hash';
}
foreach ($files as & $file3) {
    $file3 = "$file3?$hash";
}

/* accumulating in external storage, e.g. bulk operations */
foreach ($files as & $file4) {
    if (count($processed) >= 10) {
        break;
    }
    if (count($processedBag->processed) >= 10) {
        break;
    }

    $processed []= 'Next file has been processed';
    $processed []= $file4;

    $processedBag->processed []= $file4;
}

/* increments/decrements, e.g. counters */
foreach ($files as & $file5) {
    ++$processed; $processed++;
    --$processed; $processed--;
}

/* list will introduce new variables in the loop */
foreach ($files as & $file7) {
    list ($first, $second) = explode('...', '...');
    // [$third, $fourth]      = explode('...', '...'); <- PS 2016.2 parser doesn't support this

    unset($first, $second, $third, $fourth);
}

/* different assignments: clone, reassigning variables with variables; control statements */
$tomorrow = new \DateTime();
foreach ($files as & $file8) {
    $tomorrowInstance = clone $tomorrow;
    $tomorrowBackup   = $tomorrow;
    $bool             = true;
    $array            = [];
    $const            = \DateTime::ATOM;

    break;
    continue 1;
    return;

    $file8->timestamp            = $tomorrowInstance;
    $tomorrowInstance->timestamp = $file8;
    $tomorrow->timestamp         = $file8;
}

/* nested dependencies resolution correctness */
$count = 0;
foreach ($files as & $file9) {
    if (++$count > 0) {
        $file9->save();
    }
}

/* loop with unpacking array into multiple variables */
foreach ([[], [], []] as list($a, $b, $c)) {
    if ($b === $count) {
        $c->save();
    }
}

/* false-positives: inner break, continue, throw, return statements */
foreach ($files as $file10) {
    if ($false1) { break; }
    if ($false2) { continue; }
    if ($false3) { return; }
    if ($false4) { throw $exception; }
}

/* false-positives: statement is not operating with any variable */
foreach ($links as $link) {
    echo '<li>';
    echo $link;
    echo '</li>';
}

/* false-positives: object modifications */
foreach ([] as $item) {
    $object1->method1($item);
    $object2->method2($object1);
    $object3->method3($object2);
}

/* false-positives: compact function usage */
foreach ([] as $variable) {
    $object->method(compact('variable'));
}

/* false-positive: variables by reference */
foreach ([] as $innerArray) {
    array_shift($unknownVariable);
    preg_match('...', $innerArray[0], $matched);
    preg_match_all('...', $innerArray[0], $matchedAll);

    unset($unknownVariable, $matched, $matchedAll);
}

/* false-positive: variable introduced in call arguments */
foreach ([] as $item) {
    $object->method($first = new Clazz());
    $first->method();

    call($second = new Clazz());
    $second->method();
}

/* false-positive: outer loop variables */
foreach ([] as $outerIndex => $outerValue) {
    foreach ([] as $innerValue) {
        echo $outerIndex;
        echo $outerValue;
    }
}

/* false-positive: templating */
foreach ([] as $value) {
    ?><input type="text" class="<?php echo $class ?>"/><?php
}