package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStub {
    @Test
    public void Submit_a_task_and_get_the_result_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("exec", Ognl.getValue("@java.util.concurrent.Executors@newSingleThreadExecutor()", env));
        {
            java.util.concurrent.Callable stub = mock(java.util.concurrent.Callable.class);
            when(stub.call()).thenReturn(Ognl.getValue("42", env));
            env.put("task", stub);
        }
        {
            Object actual = Ognl.getValue("exec.submit(task).get()", env);
            Object expected = Ognl.getValue("42", env);
            assertThat(actual, is(expected));
        }
    }
}
