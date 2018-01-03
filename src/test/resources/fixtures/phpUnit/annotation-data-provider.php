<?php

class DependsAnnotationHolder {
    private function unnamedDataProvider() { return [''];       }
    private function namedDataProvider()   { return ['' => '']; }

    /** @dataProvider outer */
    abstract public function <error descr="@dataProvider referencing to a non-existing entity.">dataProviderTypo</error>();

    /** @dataProvider unnamedDataProvider */
    abstract public function <weak_warning descr="It would be better for maintainability to to use named datasets in @dataProvider.">consumesUnnamedDataProvider</weak_warning>();

    /** @dataProvider unnamedDataProvider */
    abstract public function consumesNamedDataProvider();
}