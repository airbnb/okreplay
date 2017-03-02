package walkman;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface Walkman {
  String tape() default "";
  TapeMode mode() default TapeMode.UNDEFINED;
  MatchRules[] match() default {};
}
