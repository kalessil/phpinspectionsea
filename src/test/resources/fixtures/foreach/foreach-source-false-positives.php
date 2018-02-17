<?php

    /* false-positive: global context influenced by foreach-source-false-positives.setup.php */
    $files = ['...'];
    foreach ($files as $file) {}

    /* false-positive: overriding variable before foreach */
    /** @param null|string|string[] $values */
    function mixedParams($values) {
        $values = is_array($values) ? $values : explode('...', $values);
        foreach ($values as $string) {}
    }

    /* false-positive: mysterious appearance of mixed */
    /** @param array[] $array */
    function mysteriousMixedTypeAppearance(array $array) {
        foreach ($array['key'] as $something) {}
    }

    function falsePositivesHolder() {
        /* ensures that type[] is handled properly */
        /* @var $intOrIntArray int[] */
        foreach ($intOrIntArray as $value) {}

        /* general pattern: null as indication of failed operation */
        /* @var $nullOrArray null|array */
        foreach ($nullOrArray as $value) {}

        /* general pattern: false as indication of failed operation */
        /* @var $boolOrArray bool|array */
        foreach ($boolOrArray as $value) {}

        /* general pattern: generators and transitively iterators */
        /* @var $generator \Generator */
        foreach ($generator as $value) {}

        /* general pattern: iterable */
        /* @var $iterable \iterable */
        foreach ($iterable as $value) {}

        /* ensures not reporting stub-functions */
        foreach (array_rand([]) as $random) {}

        /* pre-defined variables support */
        foreach ($_REQUEST as $value) {}

        /* types confusion when parent if has implicit validity check */
        /* @var $objectOrIterable object|iterable */
        if (false || is_iterable($objectOrIterable)) {
            foreach ($objectOrIterable as $value) {}
        }
        /* @var $objectOrArray object|array */
        if (false || is_array($objectOrArray)) {
            foreach ($objectOrArray as $value) {}
        }
    }

    /* false-positives: php-doc + type hint result */
    /** @return string[][] */
    function returns_array(): array {}
    function consumes_array() {
        foreach (returns_array() as $array) {
            foreach ($array as $string) {}
        }
    }

    /* false-positive: IDE doesn't handle null-coalescing operator properly */
    function foo(?\Traversable $traversable) {
        foreach ($traversable ?? [] as $value) {}
    }

    /** @param \stdClass[] $array */
    function issue800(array $array)
    {
        foreach (array_reverse($array) as $item) {
        }
    }
