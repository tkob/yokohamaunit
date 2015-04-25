package yokohama.unit.util;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GenSymTest {
    
    @Test
    public void testGenerate() {
        // Setup
        String prefix = "prefix";
        GenSym instance = new GenSym();

        // Exercise
        String actual = instance.generate(prefix);

        // Verify
        assertThat(actual, is("prefix"));
    }
    
    @Test
    public void testGenerate1() {
        // Setup
        String prefix = "prefix";
        GenSym instance = new GenSym();
        instance.generate(prefix);

        // Exercise
        String actual = instance.generate(prefix);

        // Verify
        assertThat(actual, is("prefix2"));
    }
    
    @Test
    public void testGenerate2() {
        // Setup
        String prefix = "prefix";
        GenSym instance = new GenSym();
        instance.generate("another");

        // Exercise
        String actual = instance.generate(prefix);

        // Verify
        assertThat(actual, is("prefix"));
    }
    
    @Test
    public void testGenerate3() {
        // Setup
        String prefix = "prefix";
        GenSym instance = new GenSym(Arrays.asList("prefix"));

        // Exercise
        String actual = instance.generate(prefix);

        // Verify
        assertThat(actual, is("prefix2"));
    }
    
}
