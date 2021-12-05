<?php

namespace Foo {
	class Bar {
		public function __construct() {}
	}

	class Foo {
		public function create(): Bar {
			return new Bar();
		}
	}
}

namespace Bar {
	class Bar extends \Foo\Bar {}

	class Foo extends \Foo\Foo  {
		public function create(): \Foo\Bar {
			return new Bar();
		}
	}
}