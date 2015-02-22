package yokohama.unit.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static yokohama.unit.translator.TranslatorUtils.parseDocy;
import yokohama.unit.util.SBuilder;

@RunWith(Theories.class)
public class VariableCheckVisitorTest {
    /*
    test: test, fourPhase
    let: none, consistent, inconsistent
    where: none, consistent, inconsistent, duplicate
    tableRef: none, consistnet, inconsistent, duplicate
    table: none, consistnet, inconsistent
    instanceOfSuchThat: none, consistnet, duplicate
    instanceOfSuchThat2: none, consistnet, duplicate

    IF [where] <> "none" THEN [tableRef] = "none";
    IF [where] <> "none" THEN [table] = "none";
    IF [where] <> "none" THEN [instanceOfSuchThat] = "none";
    IF [where] <> "none" THEN [instanceOfSuchThat2] = "none";
    IF [tableRef] <> "none" THEN [where] = "none";
    IF [tableRef] <> "none" THEN [table] = "none";
    IF [tableRef] <> "none" THEN [instanceOfSuchThat] = "none";
    IF [tableRef] <> "none" THEN [instanceOfSuchThat2] = "none";
    IF [table] <> "none" THEN [where] = "none";
    IF [table] <> "none" THEN [tableRef] = "none";
    IF [table] <> "none" THEN [instanceOfSuchThat] = "none";
    IF [table] <> "none" THEN [instanceOfSuchThat2] = "none";

    IF [let] = "none" THEN [where] <> "duplicate";
    IF [let] = "none" THEN [tableRef] <> "duplicate";
    IF [let] = "none" THEN [instanceOfSuchThat] <> "duplicate";
    IF [let] = "none" AND [instanceOfSuchThat] = "none" THEN [instanceOfSuchThat2] <> "duplicate";

    IF [let] <> "none" THEN [test] = "fourPhase";
    */
    /*
    test        let             where           tableRef        table           instanceOfSuchThat  instanceOfSuchThat2
    fourPhase   consistent      consistent      none            none            none                none
    fourPhase   consistent      duplicate       none            none            none                none
    fourPhase   consistent      inconsistent    none            none            none                none
    fourPhase   consistent      none            consistnet      none            none                none
    fourPhase   consistent      none            duplicate       none            none                none
    fourPhase   consistent      none            inconsistent    none            none                none
    fourPhase   consistent      none            none            consistnet      none                none
    fourPhase   consistent      none            none            inconsistent    none                none
    fourPhase   consistent      none            none            none            consistnet          duplicate
    fourPhase   consistent      none            none            none            duplicate           duplicate
    fourPhase   consistent      none            none            none            none                consistnet
    fourPhase   consistent      none            none            none            duplicate           none
    fourPhase   inconsistent    consistent      none            none            none                none
    fourPhase   inconsistent    duplicate       none            none            none                none
    fourPhase   inconsistent    inconsistent    none            none            none                none
    fourPhase   inconsistent    none            consistnet      none            none                none
    fourPhase   inconsistent    none            duplicate       none            none                none
    fourPhase   inconsistent    none            inconsistent    none            none                none
    fourPhase   inconsistent    none            none            consistnet      none                none
    fourPhase   inconsistent    none            none            inconsistent    none                none
    fourPhase   inconsistent    none            none            none            duplicate           consistnet
    fourPhase   inconsistent    none            none            none            none                duplicate
    fourPhase   inconsistent    none            none            none            consistnet          none
    fourPhase   none            consistent      none            none            none                none
    test        none            consistent      none            none            none                none
    test        none            inconsistent    none            none            none                none
    test        none            none            consistnet      none            none                none
    test        none            none            inconsistent    none            none                none
    x test        none            none            none            consistnet      none                none
    x test        none            none            none            inconsistent    none                none
    test        none            none            none            none            consistnet          consistnet
    test        none            none            none            none            consistnet          duplicate
    */
    @AllArgsConstructor
    public static class Fixture {
        public String source;
        public List<String> errors;
    }
    @DataPoints
    public static Fixture[] PARAMs = {
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where c = `3` and d = `4`.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where b = `3` and c = `4`.")
                        .toString(),
                Arrays.asList("b")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where c = `3` and d = `4` and c = `5`.")
                        .toString(),
                Arrays.asList("c")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all c and d in Table 'table'.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all b and c in Table 'table'.")
                        .toString(),
                Arrays.asList("b")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all c, d, c, e and c in Table 'table'.")
                        .toString(),
                Arrays.asList("c", "c")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1`.")
                        .appendln("Table: table")
                        .appendln("|a|b|c|")
                        .appendln("|0|1|2|")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1`.")
                        .appendln("Table: table")
                        .appendln("|a|b|a|c|a")
                        .appendln("|0|1|2|3|4")
                        .toString(),
                Arrays.asList("a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance c of `Exception`")
                        .appendln("such that `c.m()` throws an instance c of `Exception`")
                        .appendln("such that `c.m()` throws nothing.")
                        .toString(),
                Arrays.asList("c")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws an instance b of `Exception`")
                        .appendln("such that `b.m()` throws nothing.")
                        .toString(),
                Arrays.asList("a", "b")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance c of `Exception`")
                        .appendln("such that `c.m()` throws nothing.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and b = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance b of `Exception`")
                        .appendln("such that `b.m()` throws nothing.")
                        .toString(),
                Arrays.asList("b")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where c = `3` and d = `4`.")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where a = `3` and b = `4`.")
                        .toString(),
                Arrays.asList("a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where c = `3` and d = `4` and c = `5`.")
                        .toString(),
                Arrays.asList("a", "c")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all c and d in Table 'table'.")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all a and b in Table 'table'.")
                        .toString(),
                Arrays.asList("a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` for all c, d, c, e and c in Table 'table'.")
                        .toString(),
                Arrays.asList("a", "c", "c")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1`.")
                        .appendln("Table: table")
                        .appendln("|a|b|c|")
                        .appendln("|0|1|2|")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1`.")
                        .appendln("Table: table")
                        .appendln("|a|b|a|c|a")
                        .appendln("|0|1|2|3|4")
                        .toString(),
                Arrays.asList("a", "a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws an instance b of `Exception`")
                        .appendln("such that `b.m()` throws nothing.")
                        .toString(),
                Arrays.asList("a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws nothing.")
                        .toString(),
                Arrays.asList("a", "a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Setup: setup")
                        .appendln("Let a = `1` and a = `2`.")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` throws an instance b of `Exception`")
                        .appendln("such that `b.m()` throws nothing.")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Verify: verify")
                        .appendln("Assert `1` is `1` where a = `1` and b = `2`.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` is `1` where a = `1` and b = `2`.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` is `1` where a = `1` and a = `2`.")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` is `1` for all a and b in Table 'table'.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` is `1` for all a, b and a in Table 'table'.")
                        .toString(),
                Arrays.asList("a")),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws an instance b of `Exception`")
                        .appendln("such that `b.m()` throws nothing.")
                        .toString(),
                Arrays.asList()),
        new Fixture(
                new SBuilder(4)
                        .appendln("Test: test")
                        .appendln("Assert `1` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws an instance a of `Exception`")
                        .appendln("such that `a.m()` throws nothing.")
                        .toString(),
                Arrays.asList("a")),
    };
    @Theory
    public void testCheck(final Fixture fixture) {
        String source = fixture.source;
        Group group = parseDocy(source);
        VariableCheckVisitor instance = new VariableCheckVisitor();
        List<ErrorMessage> actual = instance.check(group);
        List<ErrorMessage> expected =
                fixture.errors.stream()
                        .map(var ->
                                new ErrorMessage("variable " + var + " is already defined", Span.dummySpan()))
                        .collect(Collectors.toList());
        assertThat(actual, is(expected));
    }
}
