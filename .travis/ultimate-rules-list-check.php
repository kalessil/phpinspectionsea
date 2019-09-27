<?php

    $basePath = __DIR__ . '/..';

    $rulesUltimate = file($basePath . '/RULES.md');
    $rulesExtended = file($basePath . '/RULES.master.md');
    if (false === $rulesExtended || false === $rulesUltimate) {
        throw new \RuntimeException('Failed to load resources');
    }

    /* collect documented ultimate rules */
    $documentedUltimateRules = array();
    foreach ($rulesUltimate as $lineNumber => $lineContent) {
        $lineContent = trim($lineContent);
        if (false !== stripos($lineContent, 'inspect') && false !== strpos($lineContent, '|')) {
            $fragments = explode('|', $lineContent);
            if (count($fragments) >= 8) {
                $groupName        = trim($fragments[1]);
                $shortName        = trim($fragments[2]);
                $displayName      = trim($fragments[3]);
                $hasTests         = 'yes' === trim($fragments[5]);
                $hasDocumentation = 'yes' === trim($fragments[7]);

                $documentedUltimateRules[$shortName] = $valueObject = new \stdClass();
                $valueObject->groupName              = $groupName;
                $valueObject->shortName              = $shortName;
                $valueObject->displayName            = $displayName;
                $valueObject->hasTests               = $hasTests;
                $valueObject->hasDocumentation        = $hasDocumentation;
            }
        }
    }

    /* collect documented ultimate rules */
    $documentedExtendedRules = array();
    foreach ($rulesExtended as $lineNumber => $lineContent) {
        $lineContent = trim($lineContent);
        if (false !== stripos($lineContent, 'inspect') && false !== strpos($lineContent, '|')) {
            $fragments = explode('|', $lineContent);
            if (count($fragments) >= 8) {
                $groupName        = trim($fragments[1]);
                $shortName        = trim($fragments[2]);
                $displayName      = trim($fragments[3]);
                $hasTests         = 'yes' === trim($fragments[5]);
                $hasDocumentation = 'yes' === trim($fragments[7]);

                $documentedExtendedRules[$shortName]   = $valueObject = new \stdClass();
                $valueObject->groupName                = $groupName;
                $valueObject->shortName                = $shortName;
                $valueObject->displayName              = $displayName;
                $valueObject->hasTests                 = $hasTests;
                $valueObject->hasDocumentation         = $hasDocumentation;
            }
        }
    }

    /* find mismatches */
    $mismatchingRules = [];
    foreach ($documentedExtendedRules as $shortName => $rule) {
        if (array_key_exists($shortName, $documentedUltimateRules)) {
            $ultimateRule = $documentedUltimateRules[$shortName];
            if ((array)$ultimateRule != (array)$rule) {
                $mismatchingRules [] = $shortName;
            }
        }
    }

    if ($mismatchingRules !== []) {
        echo 'Following rules are not in sync: ' . PHP_EOL . implode(',' . PHP_EOL, $mismatchingRules) . PHP_EOL;
        exit(1);
    }
