package org.asoem.greyfish.core.io.persistence;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.awt.*;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 18:39
 */
class FixedEnumMatcher implements Matcher {

    @Override
    public Transform match(final Class arg0) throws Exception {
        if(arg0.isEnum()
                || arg0.getSuperclass() != null
                && arg0.getSuperclass().isEnum()) { // This is a Workaround for a java bug. See: http://forums.oracle.com/forums/thread.jspa?threadID=1035332
            return new Transform<Enum<?>>() {

                @SuppressWarnings({"unchecked"}) // valueOf() always returns a constant of Enum arg0
                public Enum<?> read(String value) throws Exception {
                    return Enum.valueOf(arg0, value);
                }

                public String write(Enum<?> value) throws Exception {
                    return value.name();
                }
            };
        }
        else if (Color.class.equals(arg0)) {
            return new Transform<Color>() {

                @Override
                public Color read(String arg0) throws Exception {
                    return new Color(Integer.valueOf(arg0));
                }

                @Override
                public String write(Color arg0) throws Exception {
                    return String.valueOf(arg0.getRGB());
                }

            };
        }

        return null;
    }
}
