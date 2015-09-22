package yokohama.unit.translator.experimental;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.position.Span;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.EqualOpExpr;
import yokohama.unit.ast_junit.IfStatement;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.ReturnStatement;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.util.Sym;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.translator.ExpressionStrategy;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class OgnlExpressionStrategy implements ExpressionStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;
    private final ClassResolver classResolver;

    static final ClassType MAP = new ClassType(Map.class);

    static final String OGNL = "ognl.Ognl";
    static final String OGNL_CONTEXT = "ognl.OgnlContext";
    static final String OGNL_EXCEPTION = "ognl.OgnlException";
    static final String CLASS_RESOLVER = "ognl.ClassResolver";

    @SneakyThrows(ClassNotFoundException.class)
    ClassType classTypeOf(String name) {
        return new ClassType(classResolver.lookup(name));
    }
    Type typeOf(String name) {
        return classTypeOf(name).toType();
    }

    @Override
    public Collection<ClassDecl> auxClasses() {
        if (classResolver.isEmpty()) return Collections.emptyList();
        /*
        The following interface is implemented:
            public interface ClassResolver {
                Class classForName(Map context, String className) throws ClassNotFoundException;
            }
        */
        Sym contextVar = genSym.generate("context");
        Sym classNameVar = genSym.generate("className");

        Sym tableVar = genSym.generate("table");
        Stream<Statement> createTable = Stream.of(new VarInitStatement(
                MAP.toType(),
                tableVar,
                new NewExpr(
                        "java.util.HashMap",
                        Arrays.asList(),
                        Arrays.asList()),
                Span.dummySpan()));

        Stream<Statement> populateTable =
                classResolver.flatMap((shortName, longName) -> {
                    Sym shortNameVar = genSym.generate(SUtils.toIdent(shortName));
                    Sym longNameVar = genSym.generate(SUtils.toIdent(longName));
                    Class<?> clazz;
                    try {
                        clazz = classResolver.lookup(longName);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.STRING,
                                    shortNameVar,
                                    new StrLitExpr(shortName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.CLASS,
                                    longNameVar,
                                    new ClassLitExpr(new ClassType(clazz).toType()),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.OBJECT,
                                    genSym.generate("__"),
                                    new InvokeExpr(
                                            MAP,
                                            tableVar,
                                            "put",
                                            Arrays.asList(Type.OBJECT, Type.OBJECT),
                                            Arrays.asList(shortNameVar, longNameVar),
                                            Type.OBJECT),
                                    Span.dummySpan()));
                });

        Sym returnedVar = genSym.generate("returned");
        Sym nullValueVar = genSym.generate("nullValue");
        Sym notFoundVar = genSym.generate("notFound");
        Sym fallbackVar = genSym.generate("fallback");
        Stream<Statement> lookupTable = Stream.of(
                new VarInitStatement(
                        Type.CLASS,
                        returnedVar,
                        new InvokeExpr(
                                MAP,
                                tableVar,
                                "get",
                                Arrays.asList(Type.OBJECT),
                                Arrays.asList(classNameVar),
                                Type.OBJECT),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.CLASS,
                        nullValueVar,
                        new NullExpr(),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.BOOLEAN,
                        notFoundVar,
                        new EqualOpExpr(returnedVar, nullValueVar),
                        Span.dummySpan()),
                new IfStatement(
                        notFoundVar,
                        Arrays.asList(
                                new VarInitStatement(
                                        Type.CLASS,
                                        fallbackVar,
                                        new InvokeStaticExpr(
                                                new ClassType(Class.class),
                                                Arrays.asList(),
                                                "forName",
                                                Arrays.asList(Type.STRING),
                                                Arrays.asList(classNameVar),
                                                Type.CLASS),
                                        Span.dummySpan()),
                                new ReturnStatement(fallbackVar)),
                        Arrays.asList(
                                new ReturnStatement(returnedVar))));

        List<Statement> statements =
                Stream.concat(createTable, Stream.concat(populateTable, lookupTable))
                .collect(Collectors.toList());

        Method method = new Method(
                Arrays.asList(),
                "classForName",
                Arrays.asList(
                        Pair.of(Type.STRING, classNameVar.getName()),
                        Pair.of(MAP.toType(), contextVar.getName())),
                Optional.of(Type.CLASS),
                Arrays.asList(new ClassType(ClassNotFoundException.class)),
                statements);
        ClassDecl classResolverClass = new ClassDecl(
                false,
                name + "$ClassResolver",
                Optional.empty(),
                Arrays.asList(classTypeOf(CLASS_RESOLVER)),
                Arrays.asList(method));
        return Arrays.asList(classResolverClass);
    }

    @Override
    public List<Statement> env(Sym var) {
        if (classResolver.isEmpty()) {
            return Arrays.asList(
                    new VarInitStatement(
                            typeOf(OGNL_CONTEXT),
                            var,
                            new NewExpr(
                                    "ognl.OgnlContext",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()));
        } else {
            Sym rootVar = genSym.generate("root");
            Sym classResolverVar = genSym.generate("classResolver");
            return Arrays.asList(
                    new VarInitStatement(
                            Type.OBJECT,
                            rootVar,
                            new NullExpr(),
                            Span.dummySpan()),
                    new VarInitStatement(
                            typeOf(CLASS_RESOLVER),
                            classResolverVar,
                            new NewExpr(
                                    packageName.equals("")
                                            ? ""
                                            : (packageName + ".") + name + "$ClassResolver",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()),
                    new VarInitStatement(
                            typeOf(OGNL_CONTEXT),
                            var,
                            new InvokeStaticExpr(
                                    classTypeOf(OGNL),
                                    Arrays.asList(),
                                    "createDefaultContext",
                                    Arrays.asList(
                                            Type.OBJECT,
                                            typeOf(CLASS_RESOLVER)),
                                    Arrays.asList(
                                            rootVar,
                                            classResolverVar),
                                    MAP.toType()),
                            Span.dummySpan()));
        }
    }

    @Override
    public List<Statement> bind(Sym envVar, Ident ident, Sym rhs) {
        Sym nameVar = genSym.generate(ident.getName());
        return Arrays.asList(new VarInitStatement(
                        Type.STRING,
                        nameVar,
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new VarInitStatement(
                        Type.OBJECT,
                        genSym.generate("__"),
                        new InvokeExpr(
                                classTypeOf(OGNL_CONTEXT),
                                envVar,
                                "put",
                                Arrays.asList(Type.OBJECT, Type.OBJECT),
                                Arrays.asList(nameVar, rhs),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(Sym causeVar) {
        Sym caughtVar = genSym.generate("ex");
        Sym reasonVar = genSym.generate("reason");
        Sym nullValueVar = genSym.generate("nullValue");
        Sym condVar = genSym.generate("cond");
        return Optional.of(new CatchClause(
                classTypeOf(OGNL_EXCEPTION),
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar,
                                new InvokeExpr(
                                        classTypeOf(OGNL_EXCEPTION),
                                        caughtVar,
                                        "getReason",
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Type.THROWABLE),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.THROWABLE,
                                nullValueVar,
                                new NullExpr(),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.BOOLEAN,
                                condVar,
                                new EqualOpExpr(reasonVar, nullValueVar),
                                Span.dummySpan()),
                        new IfStatement(
                                condVar,
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVar,
                                                new VarExpr(caughtVar),
                                                Span.dummySpan())),
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVar,
                                                new VarExpr(reasonVar),
                                                Span.dummySpan()))))));
    }

    @Override
    public List<Statement> eval(
            Sym var,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            Sym envVar) {
        Sym exprVar = genSym.generate("expression");
        Span span = quotedExpr.getSpan();
        return Arrays.asList(new VarInitStatement(Type.STRING, exprVar,
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(Type.fromClass(expectedType), var,
                        new InvokeStaticExpr(
                                classTypeOf(OGNL),
                                Arrays.asList(),
                                "getValue",
                                Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                Arrays.asList(exprVar,
                                        envVar,
                                        envVar),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public List<Statement> dumpEnv(Sym var, Sym envVar) {
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        var,
                        new InvokeExpr(
                                classTypeOf(OGNL_CONTEXT),
                                envVar,
                                "toString",
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Type.STRING),
                        Span.dummySpan()));
    }
}
