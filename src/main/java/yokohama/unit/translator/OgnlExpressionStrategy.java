package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
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
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class OgnlExpressionStrategy implements ExpressionStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;

    static final ClassType OGNL = new ClassType(Ognl.class);
    static final ClassType OGNL_CONTEXT = new ClassType(OgnlContext.class);
    static final ClassType OGNL_EXCEPTION = new ClassType(OgnlException.class);
    static final ClassType CLASS_RESOLVER = new ClassType(ognl.ClassResolver.class);
    static final ClassType MAP = new ClassType(Map.class);

    @Override
    public Collection<ClassDecl> auxClasses(ClassResolver classResolver) {
        if (classResolver.isEmpty()) return Collections.emptyList();
        /*
        The following interface is implemented:
            public interface ClassResolver {
                Class classForName(Map context, String className) throws ClassNotFoundException;
            }
        */
        Var contextVar = new Var(genSym.generate("context"));
        Var classNameVar = new Var(genSym.generate("className"));

        Var tableVar = new Var(genSym.generate("table"));
        Stream<Statement> createTable = Stream.of(new VarInitStatement(
                MAP.toType(),
                tableVar.getName(),
                new NewExpr(
                        "java.util.HashMap",
                        Arrays.asList(),
                        Arrays.asList()),
                Span.dummySpan()));

        Stream<Statement> populateTable =
                classResolver.flatMap((shortName, longName) -> {
                    Var shortNameVar = new Var(genSym.generate(SUtils.toIdent(shortName)));
                    Var longNameVar = new Var(genSym.generate(SUtils.toIdent(longName)));
                    Class<?> clazz;
                    try {
                        clazz = classResolver.lookup(longName);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.STRING,
                                    shortNameVar.getName(),
                                    new StrLitExpr(shortName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.CLASS,
                                    longNameVar.getName(),
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

        Var returnedVar = new Var(genSym.generate("returned"));
        Var nullValueVar = new Var(genSym.generate("nullValue"));
        Var notFoundVar = new Var(genSym.generate("notFound"));
        Var fallbackVar = new Var(genSym.generate("fallback"));
        Stream<Statement> lookupTable = Stream.of(
                new VarInitStatement(
                        Type.CLASS,
                        returnedVar.getName(),
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
                        nullValueVar.getName(),
                        new NullExpr(),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.BOOLEAN,
                        notFoundVar.getName(),
                        new EqualOpExpr(returnedVar, nullValueVar),
                        Span.dummySpan()),
                new IfStatement(
                        notFoundVar,
                        Arrays.asList(
                                new VarInitStatement(
                                        Type.CLASS,
                                        fallbackVar.getName(),
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
                        new Pair<Type, String>(Type.STRING, classNameVar.getName()),
                        new Pair<Type, String>(
                                MAP.toType(),
                                contextVar.getName())),
                Optional.of(Type.CLASS),
                Arrays.asList(new ClassType(ClassNotFoundException.class)),
                statements);
        ClassDecl classResolverClass = new ClassDecl(
                false,
                name + "$ClassResolver",
                Optional.empty(),
                Arrays.asList(CLASS_RESOLVER),
                Arrays.asList(method));
        return Arrays.asList(classResolverClass);
    }

    @Override
    public List<Statement> env(String varName, ClassResolver classResolver) {
        if (classResolver.isEmpty()) {
            return Arrays.asList(
                    new VarInitStatement(
                            OGNL_CONTEXT.toType(),
                            varName,
                            new NewExpr(
                                    "ognl.OgnlContext",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()));
        } else {
            Var rootVar = new Var(genSym.generate("root"));
            Var classResolverVar = new Var(genSym.generate("classResolver"));
            return Arrays.asList(
                    new VarInitStatement(
                            Type.OBJECT,
                            rootVar.getName(),
                            new NullExpr(),
                            Span.dummySpan()),
                    new VarInitStatement(
                            CLASS_RESOLVER.toType(),
                            classResolverVar.getName(),
                            new NewExpr(
                                    packageName.equals("")
                                            ? ""
                                            : (packageName + ".") + name + "$ClassResolver",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()),
                    new VarInitStatement(
                            OGNL_CONTEXT.toType(),
                            varName,
                            new InvokeStaticExpr(
                                    OGNL,
                                    Arrays.asList(),
                                    "createDefaultContext",
                                    Arrays.asList(
                                            Type.OBJECT,
                                            CLASS_RESOLVER.toType()),
                                    Arrays.asList(
                                            rootVar,
                                            classResolverVar),
                                    MAP.toType()),
                            Span.dummySpan()));
        }
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs) {
        Var nameVar = new Var(genSym.generate(name));
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        nameVar.getName(),
                        new StrLitExpr(name),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.OBJECT,
                        genSym.generate("__"),
                        new InvokeExpr(
                                OGNL_CONTEXT,
                                new Var(envVarName),
                                "put",
                                Arrays.asList(Type.OBJECT, Type.OBJECT),
                                Arrays.asList(nameVar, rhs),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(String causeVarName) {
        Var caughtVar = new Var(genSym.generate("ex"));
        Var reasonVar = new Var(genSym.generate("reason"));
        Var nullValueVar = new Var(genSym.generate("nullValue"));
        Var condVar = new Var(genSym.generate("cond"));
        return Optional.of(new CatchClause(
                OGNL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar.getName(),
                                new InvokeExpr(
                                        OGNL_EXCEPTION,
                                        caughtVar,
                                        "getReason",
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Type.THROWABLE),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.THROWABLE,
                                nullValueVar.getName(),
                                new NullExpr(),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.BOOLEAN,
                                condVar.getName(),
                                new EqualOpExpr(reasonVar, nullValueVar),
                                Span.dummySpan()),
                        new IfStatement(
                                condVar,
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVarName,
                                                new VarExpr(caughtVar.getName()),
                                                Span.dummySpan())),
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVarName,
                                                new VarExpr(reasonVar.getName()),
                                                Span.dummySpan()))))));
    }

    @Override
    public List<Statement> eval(
            String varName,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            String envVarName) {
        Var exprVar = new Var(genSym.generate("expression"));
        Span span = quotedExpr.getSpan();
        return Arrays.asList(
                new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(Type.fromClass(expectedType), varName,
                        new InvokeStaticExpr(
                                new ClassType(ognl.Ognl.class),
                                Arrays.asList(),
                                "getValue",
                                Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                Arrays.asList(
                                        exprVar,
                                        new Var(envVarName),
                                        new Var(envVarName)),
                                Type.OBJECT),
                        Span.dummySpan()));
    }
}
