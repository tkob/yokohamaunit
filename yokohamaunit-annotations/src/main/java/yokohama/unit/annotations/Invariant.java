package yokohama.unit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Invariant {
    String value(); 
    String lang() default "groovy";
}
