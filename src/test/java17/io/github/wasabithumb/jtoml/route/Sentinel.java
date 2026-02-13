package io.github.wasabithumb.jtoml.route;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sentinel {
    String value();
}
