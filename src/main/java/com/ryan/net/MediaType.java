package com.ryan.net;

import com.ryan.charset.ASCII;
import com.ryan.collection.ImmutableMap;
import com.ryan.util.Joiner;
import com.ryan.util.Parameters;
import com.ryan.util.Strings;
import com.ryan.util.XObjects;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Internet Media Type. This class also supports the concept of
 * media ranges defined by HTTP/1.1. The {@code *} character is treated as a
 * wildcard and is used to represent any acceptable type or subtype value. All
 * values for type, subtype, parameter attributes or parameter values must be
 * valid according to RFCs 2045 and 2046. All portions of the media type that
 * are case-insensitive (type, subtype, parameter attributes) are normalized to
 * lowercase. Parameters' values are not modified except for the {@code charset}
 * parameter which is normalised to uppercase. Instances of this class are
 * immutable.
 *
 * @author Osman KOCAK
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2046.txt">RFC 2046</a>
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.1">RFC 2616 (§ 14.1)</a>
 * @since 0.3
 */
public final class MediaType implements Serializable {
    private static final long serialVersionUID = 30386013071308392L;
    private static final String CHARSET;
    private static final String WILDCARD;
    private static final Joiner PARAMS_JOINER;
    private static final Pattern TOKEN_PATTERN;
    private static final Pattern MEDIA_TYPE_PATTERN;

    static {
        WILDCARD = "*";
        CHARSET = "charset";
        PARAMS_JOINER = Joiner.on("; ").withPrefix("; ");
        String token = "[\\p{ASCII}&&[^\\p{Cntrl}\\s\\(\\)<>@,;:\"/\\[\\]\\?=\\\\]]+";
        TOKEN_PATTERN = Pattern.compile(token);
        String quotedString = "\"([\\p{ASCII}&&[^\"\\\\]]|\\\\\\p{ASCII})*\"";
        String regex = Strings.concat("(", token, ")/(", token, ")",
                "((;[ \t\r\n]*", token, "=(", token, "|", quotedString, "))*)");
        MEDIA_TYPE_PATTERN = Pattern.compile(regex);
    }

    /**
     * <code>*&#47;*</code>
     */
    public static final MediaType ANY_TYPE = create(WILDCARD, WILDCARD);

    /**
     * {@code text/*}
     */
    public static final MediaType ANY_TEXT_TYPE = create("text", WILDCARD);

    /**
     * {@code image/*}
     */
    public static final MediaType ANY_IMAGE_TYPE = create("image", WILDCARD);

    /**
     * {@code audio/*}
     */
    public static final MediaType ANY_AUDIO_TYPE = create("audio", WILDCARD);

    /**
     * {@code video/*}
     */
    public static final MediaType ANY_VIDEO_TYPE = create("video", WILDCARD);

    /**
     * {@code application/*}
     */
    public static final MediaType ANY_APPLICATION_TYPE = create("application", WILDCARD);

    /**
     * {@code image/jpeg}
     */
    public static final MediaType JPEG = create("image", "jpeg");

    /**
     * {@code image/png}
     */
    public static final MediaType PNG = create("image", "png");

    /**
     * {@code image/gif}
     */
    public static final MediaType GIF = create("image", "gif");

    /**
     * {@code text/css}
     */
    public static final MediaType CSS = create("text", "css");

    /**
     * {@code text/html}
     */
    public static final MediaType HTML = create("text", "html");

    /**
     * {@code text/plain}
     */
    public static final MediaType PLAIN_TEXT = create("text", "plain");

    /**
     * {@code application/json}
     */
    public static final MediaType JSON = create("application", "json");

    /**
     * {@code application/xml}
     */
    public static final MediaType XML = create("application", "xml");

    /**
     * {@code application/octet-stream}
     */
    public static final MediaType OCTET_STREAM = create("application", "octet-stream");

    /**
     * Parses a {@code MediaType} from its {@code String} representation.
     *
     * @param input the input {@code String} to be parsed.
     * @return the parsed {@code MediaType} instance.
     * @throws NullPointerException     if {@code input} is {@code null}.
     * @throws IllegalArgumentException if {@code input} can't be parsed or
     *                                  if a wildcard is used for the type, but not for the subtype.
     */
    public static MediaType parse(String input) {
        Parameters.checkNotNull(input);
        Matcher m = MEDIA_TYPE_PATTERN.matcher(input);
        Parameters.checkCondition(m.matches());
        String type = m.group(1);
        String subtype = m.group(2);
        String params = m.group(3);
        ParametersParser parser = new ParametersParser();
        return create(type, subtype, parser.parse(params));
    }

    /**
     * Creates a new {@code MediaType} with the given type and subtype.
     *
     * @param type    the type.
     * @param subtype the subtype.
     * @return the created {@code MediaType} instance.
     * @throws NullPointerException     if one of the arguments is {@code null}.
     * @throws IllegalArgumentException if type or subtype is invalid or if
     *                                  a wildcard is used for the type, but not for the subtype.
     */
    public static MediaType create(String type, String subtype) {
        Map<String, String> params = Collections.emptyMap();
        return create(type, subtype, params);
    }

    private static MediaType create(String type, String subtype,
                                    Map<String, String> parameters) {
        Parameters.checkCondition(!WILDCARD.equals(type) || WILDCARD.equals(subtype));
        String normalizedType = normalizeToken(type);
        String normalizedSubtype = normalizeToken(subtype);
        Map<String, String> params = normalizeParameters(parameters);
        return new MediaType(normalizedType, normalizedSubtype, params);
    }

    private static String normalizeToken(String token) {
        Parameters.checkNotNull(token);
        Parameters.checkCondition(TOKEN_PATTERN.matcher(token).matches());
        return ASCII.toLowerCase(token);
    }

    private static Map<String, String> normalizeParameters(Map<String, String> parameters) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        for (Entry<String, String> entry : parameters.entrySet()) {
            String attribute = normalizeToken(entry.getKey());
            String value = CHARSET.equals(attribute)
                    ? ASCII.toUpperCase(entry.getValue())
                    : Parameters.checkNotNull(entry.getValue());
            params.put(attribute, value);
        }
        return params;
    }

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;

    private MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = ImmutableMap.copyOf(parameters);
    }

    /**
     * Returns the top-level media type.
     *
     * @return the top-level media type.
     */
    public String type() {
        return type;
    }

    /**
     * Returns the media subtype.
     *
     * @return the media subtype.
     */
    public String subtype() {
        return subtype;
    }

    /**
     * Returns the parameters of this media type. The returned {@code Map}
     * is immutable.
     *
     * @return the parameters of this media type.
     */
    public Map<String, String> parameters() {
        return parameters;
    }

    /**
     * Returns whether the type or the subtype of this media type is the
     * wildcard.
     *
     * @return whether this media type has a wildcard.
     */
    public boolean hasWildcard() {
        return WILDCARD.equals(type) || WILDCARD.equals(subtype);
    }

    /**
     * Returns the value of the charset parameter if it is specified, or
     * {@code null} otherwise.
     *
     * @return the charset parameter's value if present, or {@code null}
     * otherwise.
     * @throws IllegalCharsetNameException if the charset is illegal.
     * @throws UnsupportedCharsetException if the charset is not supported.
     */
    public Charset charset() {
        String charset = parameters.get(CHARSET);
        return charset == null ? null : Charset.forName(charset);
    }

    /**
     * Returns a new {@code MediaType} instance similar to this one but with
     * the {@code charset} parameter set to the {@link Charset#name name} of
     * the given charset.
     *
     * @param charset the charset.
     * @return the created {@code MediaType}.
     * @throws NullPointerException if {@code charset} is {@code null}.
     */
    public MediaType withCharset(Charset charset) {
        return withParameter(CHARSET, charset.name());
    }

    /**
     * Returns a new {@code MediaType} instance similar to this one but with
     * the specified parameter set to the given value.
     *
     * @param attribute the attribute of the parameter to add.
     * @param value     the value of the parameter to add.
     * @return the created {@code MediaType}.
     * @throws NullPointerException     if one of the arguments is {@code null}.
     * @throws IllegalArgumentException if {@code attribute} is invalid.
     */
    public MediaType withParameter(String attribute, String value) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(attribute, value);
        return withParameters(params);
    }

    /**
     * Returns a new {@code MediaType} instance having the same type and
     * subtype as this one but whose parameters is the "union" of this
     * instances' parameters with the given ones (the given parameters have
     * precedence over this instance's parameters).
     *
     * @param parameters the parameters.
     * @return the created {@code MediaType}.
     * @throws NullPointerException     if {@code parameters} is {@code null} or
     *                                  if it contains the {@code null} key or a {@code null} value.
     * @throws IllegalArgumentException if {@code parameters} contains an
     *                                  invalid attribute.
     */
    public MediaType withParameters(Map<String, String> parameters) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.putAll(this.parameters);
        params.putAll(normalizeParameters(parameters));
        return new MediaType(type, subtype, params);
    }

    /**
     * Returns a new {@code MediaType} instance similar to this one but
     * without the specified parameter.
     *
     * @param attribute the attribute of the parameter to remove.
     * @return the created {@code MediaType}.
     * @throws NullPointerException if {@code attribute} is {@code null}.
     */
    public MediaType withoutParameter(String attribute) {
        Parameters.checkNotNull(attribute);
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.putAll(parameters);
        params.remove(attribute);
        return new MediaType(type, subtype, params);
    }

    /**
     * Returns a new {@code MediaType} instance with the same type and
     * subtype as this instance but without any parameters.
     *
     * @return the created {@code MediaType}.
     */
    public MediaType withoutParameters() {
        Map<String, String> params = Collections.emptyMap();
        return new MediaType(type, subtype, params);
    }

    /**
     * Returns whether this media type is within the specified media range.
     * Namely, it returns {@code true} if the type of {@code range} is the
     * wildcard or is equal to the type of this instance, and, if the
     * subtype of {@code range} is the wildcard or is equal to the subtype
     * of this instance, and, if all the parameters present in {@code range}
     * are also present in this instance.
     *
     * @param range the range to check this media type against.
     * @return whether this media type is within the specified range.
     * @throws NullPointerException if {@code range} is {@code null}.
     */
    public boolean is(MediaType range) {
        return (range.type.equals(WILDCARD) || range.type.equals(type))
                && (range.subtype.equals(WILDCARD) || range.subtype.equals(subtype))
                && parameters.entrySet().containsAll(range.parameters.entrySet());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MediaType)) {
            return false;
        }
        MediaType mediaType = (MediaType) o;
        return type.equals(mediaType.type)
                && subtype.equals(mediaType.subtype)
                && parameters.equals(mediaType.parameters);
    }

    @Override
    public int hashCode() {
        return XObjects.hashCode(type, subtype, parameters);
    }

    @Override
    public String toString() {
        if (parameters.isEmpty()) {
            return Strings.concat(type, "/", subtype);
        }
        List<String> params = new ArrayList<String>();
        for (Entry<String, String> parameter : parameters.entrySet()) {
            String attribute = parameter.getKey();
            String value = parameter.getValue();
            if (!TOKEN_PATTERN.matcher(value).matches()) {
                value = escapeAndQuote(value);
            }
            params.add(attribute + "=" + value);
        }
        return Strings.concat(type, "/", subtype, PARAMS_JOINER.join(params));
    }

    private String escapeAndQuote(String value) {
        StringBuilder escaped = new StringBuilder(value.length() * 2);
        escaped.append('"');
        for (char c : value.toCharArray()) {
            if (c == '\\' || c == '"') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.append('"').toString();
    }

    private static final class ParametersParser {
        private String params;
        private int index;

        Map<String, String> parse(String params) {
            this.params = params;
            Map<String, String> m = new LinkedHashMap<String, String>();
            while (index < params.length() - 1) {
                m.put(readAttribute(), readValue());
            }
            return m;
        }

        private String readAttribute() {
            StringBuilder attribute = new StringBuilder();
            char c = params.charAt(++index);
            while (c != '=') {
                attribute.append(c);
                c = params.charAt(++index);
            }
            return attribute.toString().trim();
        }

        private String readValue() {
            char c = params.charAt(++index);
            boolean quoted = c == '"';
            c = quoted ? params.charAt(++index) : c;
            boolean escaped = false;
            StringBuilder value = new StringBuilder();
            while ((c != ';' || quoted) && (c != '"' || escaped)) {
                if (c == '\\' && !escaped) {
                    escaped = true;
                } else {
                    escaped = false;
                    value.append(c);
                }
                if (index == params.length() - 1) {
                    break;
                }
                c = params.charAt(++index);
            }
            index += quoted ? 1 : 0;
            return value.toString();
        }
    }
}
