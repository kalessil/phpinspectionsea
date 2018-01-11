<?php

class DependsAnnotationHolder {
    private function unnamedDataProvider() { return [''];       }
    private function namedDataProvider()   { return ['' => '']; }

    /** @dataProvider outer */
    abstract public function <error descr="@dataProvider referencing to a non-existing entity.">dataProviderWrongReference</error>();

    /** @dataProvider \DependsAnnotationHolder::outer */
    abstract public function <error descr="@dataProvider referencing to a non-existing entity.">dataProviderWrongFqReference</error>();

    /** @dataProvider unnamedDataProvider */
    abstract public function <weak_warning descr="It would be better for maintainability to to use named datasets in @dataProvider.">consumesUnnamedDataProvider</weak_warning>();

    /** @dataProvider namedDataProvider */
    abstract public function consumesNamedDataProvider();
}