module huskit.common.main {
    requires static lombok;
    requires static org.checkerframework.checker.qual;
    requires org.jetbrains.annotations;
    exports io.huskit.common;
    exports io.huskit.common.concurrent;
    exports io.huskit.common.function;
}
