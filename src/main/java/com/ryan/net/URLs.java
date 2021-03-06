package com.ryan.net;

import com.ryan.util.Parameters;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * URLs utilities.
 *
 * @author Osman KOCAK
 */
public final class URLs {
    /**
     * Creates a new {@code URL} by parsing the given {@code String}. This
     * function invokes the {@link URL#URL(java.lang.String)} constructor;
     * any {@code MalformedURLException} thrown by the constructor is caught
     * and wrapped in a new {@code IllegalArgumentException}, which is then
     * thrown. This method is provided for use in situations where it is
     * known that the given {@code String} is a legal {@code URL}.
     *
     * @param url the {@code String} to be parsed into a {@code URL}.
     * @return the created {@code URL}.
     * @throws NullPointerException     if {@code url} is {@code null}.
     * @throws IllegalArgumentException if {@code url} is not a valid URL.
     */
    public static URL create(String url) {
        Parameters.checkNotNull(url);
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Relativizes the specified full {@code URL} against the given base.
     * Behaves as {@link java.net.URI#relativize(java.net.URI)}.
     *
     * @param base the base {@code URL}.
     * @param full the full {@code URL}.
     * @return the relativized path.
     * @throws NullPointerException     if one of the arguments is {@code null}.
     * @throws IllegalArgumentException if the relativization fails.
     */
    public static String relativize(URL base, URL full) {
        try {
            return base.toURI().relativize(full.toURI())
                    .normalize().toString();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Resolves the specified path against the given base {@code URL}.
     * Behaves as {@link java.net.URI#resolve(java.lang.String)}.
     *
     * @param base the base {@code URL}.
     * @param path the path to resolve.
     * @return the resolved {@code URL}.
     * @throws NullPointerException     if one of the arguments is {@code null}.
     * @throws IllegalArgumentException if the path can't be resolved.
     */
    public static URL resolve(URL base, String path) {
        try {
            return base.toURI().resolve(path).normalize().toURL();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private URLs() {
        /* ... */
    }
}
