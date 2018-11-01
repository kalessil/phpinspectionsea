<?php

function cases_holder(\SplObjectStorage $storage, array $array) {
    return [
        <error descr="'preg_match('/.../', '...') == -1' seems to be always false.">preg_match('/.../', '...') == -1</error>,
        preg_match('/.../', '...') == 0,
        preg_match('/.../', '...') == 1,
        <error descr="'preg_match('/.../', '...') == 2' seems to be always false.">preg_match('/.../', '...') == 2</error>,

        <error descr="'preg_match('/.../', '...') != -1' seems to be always true.">preg_match('/.../', '...') != -1</error>,
        preg_match('/.../', '...') != 0,
        preg_match('/.../', '...') != 1,
        <error descr="'preg_match('/.../', '...') != 2' seems to be always true.">preg_match('/.../', '...') != 2</error>,

        <error descr="'preg_match('/.../', '...') === -1' seems to be always false.">preg_match('/.../', '...') === -1</error>,
        preg_match('/.../', '...') === 0,
        preg_match('/.../', '...') === 1,
        <error descr="'preg_match('/.../', '...') === 2' seems to be always false.">preg_match('/.../', '...') === 2</error>,

        <error descr="'preg_match('/.../', '...') !== -1' seems to be always true.">preg_match('/.../', '...') !== -1</error>,
        preg_match('/.../', '...') !== 0,
        preg_match('/.../', '...') !== 1,
        <error descr="'preg_match('/.../', '...') !== 2' seems to be always true.">preg_match('/.../', '...') !== 2</error>,

        <error descr="'preg_match('/.../', '...') > -1' seems to be always true.">preg_match('/.../', '...') > -1</error>,
        preg_match('/.../', '...') > 0,
        <error descr="'preg_match('/.../', '...') > 1' seems to be always false.">preg_match('/.../', '...') > 1</error>,
        <error descr="'preg_match('/.../', '...') > 2' seems to be always false.">preg_match('/.../', '...') > 2</error>,

        <error descr="'preg_match('/.../', '...') >= -1' seems to be always true.">preg_match('/.../', '...') >= -1</error>,
        <error descr="'preg_match('/.../', '...') >= 0' seems to be always true.">preg_match('/.../', '...') >= 0</error>,
        preg_match('/.../', '...') >= 1,
        <error descr="'preg_match('/.../', '...') >= 2' seems to be always false.">preg_match('/.../', '...') >= 2</error>,

        <error descr="'preg_match('/.../', '...') < -1' seems to be always false.">preg_match('/.../', '...') < -1</error>,
        <error descr="'preg_match('/.../', '...') < 0' seems to be always false.">preg_match('/.../', '...') < 0</error>,
        preg_match('/.../', '...') < 1,
        <error descr="'preg_match('/.../', '...') < 2' seems to be always true.">preg_match('/.../', '...') < 2</error>,

        <error descr="'preg_match('/.../', '...') <= -1' seems to be always false.">preg_match('/.../', '...') <= -1</error>,
        preg_match('/.../', '...') <= 0,
        <error descr="'preg_match('/.../', '...') <= 1' seems to be always true.">preg_match('/.../', '...') <= 1</error>,
        <error descr="'preg_match('/.../', '...') <= 2' seems to be always true.">preg_match('/.../', '...') <= 2</error>,

        $storage->count() == 0,
        $storage->count() != 0,
        $storage->count() === 0,
        $storage->count() !== 0,
        $storage->count() > 0,
        <error descr="'$storage->count() >= 0' seems to be always true.">$storage->count() >= 0</error>,
        <error descr="'$storage->count() < 0' seems to be always false.">$storage->count() < 0</error>,
        $storage->count() <= 0,
    ];
}