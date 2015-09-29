package yokohama.unit.annotations;

public @interface Invariant {
    String value(); 
    String lang() default "groovy";
}
