<?php

namespace NamespaceWithAliasedNamespaceImport {

    use NamespaceWithAliasedNamespaceImport\Nested\Deeply\ForAliasing as AliasedNamespace;

    class Clazz
    {
        public function method() {
            return [
                AliasedNamespace\Clazz::class,
                <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">AliasedNamespace\clazz</error>::class,
                <error descr="[EA] ::class result and the class qualified name are not identical (case mismatch).">aliasednamespace\Clazz</error>::class,
            ];
        }
    }
}

namespace NamespaceWithAliasedNamespaceImport\Nested\Deeply\ForAliasing {
    class Clazz {}
}