package hu.akarnokd.reactiverpc;

import java.lang.annotation.*;

/**
 * Indicates a public method is a service with possible function name.
 * <p>Can be applied to incoming and outgoing service interfaces.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RsRpc {
    String name() default "";
}
