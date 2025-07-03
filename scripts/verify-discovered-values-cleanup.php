<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA';

    $missingClear = [];
    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $fileContent = file_get_contents($file->getPathname());
            preg_match_all('/([ ]+)(final[ ]+)?[a-z<>]+[ ]+([a-z]+)[ ]+=\s+PossibleValuesDiscoveryUtil\s*\.\s*discover\(/ims', $fileContent, $discovery);
            if ($discovery[0] !== []) {
                /* match number of calls */
                preg_match_all('/([ ]+)('.implode('|', $discovery[3]).')\.clear\(/ims', $fileContent, $clearing);
                if (count($discovery[0]) !== count($clearing[0])) {
                    $missingClear []= $file->getFilename();
                    continue;
                }
                /* ensure same indentation level */
                foreach ($discovery[1] as $index => $offset) {
                    if (($matchedIndex = array_search($offset, $clearing[1])) === false) {
                        $missingClear []= $file->getFilename();
                        break;
                    }
                    unset($clearing[1][$matchedIndex]);
                }
            }
        }
    }

    if ($missingClear !== []) {
        echo 'Following files has discovered values cleanup flaws: ', PHP_EOL, implode(PHP_EOL, $missingClear), PHP_EOL, count($missingClear), ' in total', PHP_EOL;
        exit(1);
    }

    echo 'Verify discovered values cleanup: [OK]', PHP_EOL;