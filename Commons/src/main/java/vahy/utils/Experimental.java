package vahy.utils;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * This element has an experimental maturity.  Use with caution.
 *
 *
 * NOTE: Things like : The developers of this element is not responsible for the issues created,
 * using it is not suggested for production environment. If you see this annotation do this, do not do that etc
 * Enjoy responsibly....
 */


@Documented //this annotation maybe helpful for your custom annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
    ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
    ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface Experimental {}
