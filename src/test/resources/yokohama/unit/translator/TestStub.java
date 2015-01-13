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
        Object exec = eval("@java.util.concurrent.Executors@newSingleThreadExecutor()", env, "TestStub.docy", 4, "TestStub.docy:4.17-4.74");
        env.put("exec", exec);
        java.util.concurrent.Callable task = mock(java.util.concurrent.Callable.class);
        when((Object)task.call()).thenReturn(eval("42", env, "TestStub.docy", 6, "TestStub.docy:6.51-6.53"));
        env.put("task", task);
        {
            Object actual = eval("exec.submit(task).get()", env, "TestStub.docy", 3, "TestStub.docy:3.9-3.32");
            Object expected = eval("42", env, "TestStub.docy", 3, "TestStub.docy:3.38-3.40");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void Collections_unmodifiableMap_preserves_lookup_1() throws Exception {
        OgnlContext env = new OgnlContext();
        java.util.Map map = mock(java.util.Map.class);
        when((Object)map.get(isA(java.lang.Object.class))).thenReturn(eval("42", env, "TestStub.docy", 12, "TestStub.docy:12.65-12.67"));
        env.put("map", map);
        Object unmodifiableMap = eval("@java.util.Collections@unmodifiableMap(map)", env, "TestStub.docy", 13, "TestStub.docy:13.28-13.71");
        env.put("unmodifiableMap", unmodifiableMap);
        {
            Object actual = eval("unmodifiableMap.get(\"answer\")", env, "TestStub.docy", 10, "TestStub.docy:10.9-10.38");
            Object expected = eval("42", env, "TestStub.docy", 10, "TestStub.docy:10.44-10.46");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void StringBuilder_append_CharSequence_int_int_calls_CharSequence_charAt() throws Exception {
        OgnlContext env = new OgnlContext();
        java.lang.CharSequence seq = mock(java.lang.CharSequence.class);
        when((Object)seq.charAt(anyInt())).thenReturn(eval("'a'", env, "TestStub.docy", 20, "TestStub.docy:20.52-20.55"));
        when((Object)seq.length()).thenReturn(eval("13", env, "TestStub.docy", 21, "TestStub.docy:21.49-21.51"));
        env.put("seq", seq);
        Object sb = eval("new java.lang.StringBuilder()", env, "TestStub.docy", 22, "TestStub.docy:22.13-22.42");
        env.put("sb", sb);
        eval("sb.append(seq, 10, 13)", env, "TestStub.docy", 26, "TestStub.docy:26.5-26.27");
        {
            Object actual = eval("sb.toString()", env, "TestStub.docy", 30, "TestStub.docy:30.9-30.22");
            Object expected = eval("\"aaa\"", env, "TestStub.docy", 30, "TestStub.docy:30.28-30.33");
            assertThat(actual, is(expected));
        }
    }
}
