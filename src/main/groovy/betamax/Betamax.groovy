package betamax

import java.lang.annotation.*
import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Retention(RUNTIME)
@Target(METHOD)
@interface Betamax {
	String tape()
}
