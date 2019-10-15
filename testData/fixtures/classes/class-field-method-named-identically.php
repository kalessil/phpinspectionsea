<?php

class NamesMatchesFieldTypeUnknown {
    private $entity;
    public function <weak_warning descr="[EA] There is a field with the same name, but its type can not be resolved.">entity</weak_warning> () {}
}
class NamesMatchesFieldsCallable {
    /* @var callable|closure */
    private $entity;
    public function <weak_warning descr="[EA] There is a field with the same name, please give the method another name like is*, get*, set* and etc.">entity</weak_warning> () {}
}

/* false-positives */
class NamesMatchesFieldNotCallable {
    /* @var int */
    private $entity;
    public function entity () {}
}
class NamesMatchesFieldConstant {
    const entity = '';
    public function entity () {}
}