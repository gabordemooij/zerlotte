package com.zerlotemplate;

import com.zerlotemplate.Templateable;

/**
 * Interface of a generated template object.
 * Generated Zerlotemplate object contain methods to obtain
 * subregions (cut regions) and paste typed regions at paste
 * regions. This interface acts as mostly as a marker interface
 * but also contains some general use methods.
 */
public interface Snippet {

    /**
     * Unboxes a Snippet into a Templateable.
     * Snippets are only wrappers. Under the hood they use the
     * dynamic typeless templating system provided by Zerlotte.
     * The typesafety merely comes from wrapping the raw
     * Templateables. Therefore, each Snippet requires an
     * unbox() to unwrap the template region hiding within
     * the instance.
     *
     * @return Templateable
     */
    public Templateable unbox();

    /**
     * Renders the snippet
     *
     * @return String
     */
    public String toString();

    /**
     * Returns a string describing the snippet.
     * Does not render the snippet.
     *
     * @return
     */
    public String info();
}
