package org.wsock;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.typetools.TypeResolver;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by joco on 05.10.16.
 */

public class JacksonConverterTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    public <T> Object readValueAsTypeFromFnArg(String json, Function<T, ?> fn) throws IOException {
        Class<?> type = TypeResolver.resolveRawArguments(Function.class, fn.getClass())[0];
        return objectMapper.readValue(json, type);
    }

    @Test
    public void testVoid() throws IOException {
        Object objAsVoid = readValueAsTypeFromFnArg("{}", (Void a) -> 0);
        Assert.assertNotNull(objAsVoid);
        Assert.assertTrue(objAsVoid instanceof Void);
    }

    @Test
    public void testPrimitives() throws IOException {
        // ... well java primitives does not compile to fn arg, have to use wrappers

        Object num = readValueAsTypeFromFnArg("1", (Integer a) -> 0);
        Assert.assertNotNull(num);
        Assert.assertEquals(1, num);

        Object btrue = readValueAsTypeFromFnArg("true", (Boolean a) -> 0);
        Assert.assertNotNull(btrue);
        Assert.assertEquals(true, btrue);

        Object bfalse = readValueAsTypeFromFnArg("false", (Boolean a) -> 0);
        Assert.assertNotNull(bfalse);
        Assert.assertEquals(false, bfalse);

        Object dbl = readValueAsTypeFromFnArg("2.55", (Double a) -> 0);
        Assert.assertNotNull(dbl);
        Assert.assertEquals(2.55, (double)dbl, 0.001);
    }

    @Test
    public void testString() throws IOException {
        // ... well primitive does not compile, so Integer class ...
        Object str = readValueAsTypeFromFnArg("\"a message\"", (String a) -> 0);
        Assert.assertNotNull(str);
        Assert.assertTrue(str instanceof String);
        Assert.assertEquals("a message", str);
    }
}
