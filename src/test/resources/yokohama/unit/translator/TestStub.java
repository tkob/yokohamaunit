package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStub {
    private Object eval(String expression, OgnlContext env, String fileName, int startLine, String span) throws OgnlException {
        try {
            return Ognl.getValue(expression, env);
        } catch (OgnlException e) {
            Throwable reason = e.getReason();
            OgnlException e2 = reason == null ? new OgnlException(span + " " + e.getMessage(), e) : new OgnlException(span + " " + reason.getMessage(), reason);
            StackTraceElement[] st = { new StackTraceElement("", "", fileName, startLine) };
            e2.setStackTrace(st);
            throw e2;
        }
    }
    @Test
    public void Submit_a_task_and_get_the_result_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("exec", Ognl.getValue("@java.util.concurrent.Executors@newSingleThreadExecutor()", env));
        {
            java.util.concurrent.Callable stub = mock(java.util.concurrent.Callable.class);
            when((Object)stub.call()).thenReturn(Ognl.getValue("42", env));
            env.put("task", stub);
        }
        {
            Object actual = Ognl.getValue("exec.submit(task).get()", env);
            Object expected = Ognl.getValue("42", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void Collections_unmodifiableMap_preserves_lookup_1() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            java.util.Map stub = mock(java.util.Map.class);
            when((Object)stub.get(isA(java.lang.Object.class))).thenReturn(Ognl.getValue("42", env));
            env.put("map", stub);
        }
        env.put("unmodifiableMap", Ognl.getValue("@java.util.Collections@unmodifiableMap(map)", env));
        {
            Object actual = Ognl.getValue("unmodifiableMap.get(\"answer\")", env);
            Object expected = Ognl.getValue("42", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void StringBuilder_append_CharSequence_int_int_calls_CharSequence_charAt() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            java.lang.CharSequence stub = mock(java.lang.CharSequence.class);
            when((Object)stub.charAt(anyInt())).thenReturn(Ognl.getValue("'a'", env));
            when((Object)stub.length()).thenReturn(Ognl.getValue("13", env));
            env.put("seq", stub);
        }
        env.put("sb", Ognl.getValue("new java.lang.StringBuilder()", env));
        Ognl.getValue("sb.append(seq, 10, 13)", env);
        {
            Object actual = Ognl.getValue("sb.toString()", env);
            Object expected = Ognl.getValue("\"aaa\"", env);
            assertThat(actual, is(expected));
        }
    }
}
