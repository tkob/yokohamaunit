package yokohama.unit.util;

import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OptionalsTest {
    @Test
    public void testMatch() {
        boolean actual = Optionals.match(Optional.empty(), () -> true, obj -> false);
        assertThat(actual, is(true));
    }
    
    @Test
    public void testMatch1() {
        boolean actual = Optionals.match(Optional.of(""), () -> true, obj -> false);
        assertThat(actual, is(false));
    }
}
