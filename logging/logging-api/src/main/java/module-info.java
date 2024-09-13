module huskit.logging.logging.api.main {
    requires huskit.common.main;
    requires org.slf4j;
    requires static lombok;
    requires static org.checkerframework.checker.qual;
    exports io.huskit.log;
}
