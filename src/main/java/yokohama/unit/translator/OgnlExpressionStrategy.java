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
        Sym contextVar = new Sym(genSym.generate("context"));
        Sym classNameVar = new Sym(genSym.generate("className"));

        Sym tableVar = new Sym(genSym.generate("table"));
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
                    Sym shortNameVar = new Sym(genSym.generate(SUtils.toIdent(shortName)));
                    Sym longNameVar = new Sym(genSym.generate(SUtils.toIdent(longName)));
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

        Sym returnedVar = new Sym(genSym.generate("returned"));
        Sym nullValueVar = new Sym(genSym.generate("nullValue"));
        Sym notFoundVar = new Sym(genSym.generate("notFound"));
        Sym fallbackVar = new Sym(genSym.generate("fallback"));
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
                Arrays.asList(classTypeOf(CLASS_RESOLVER)),
                Arrays.asList(method));
        return Arrays.asList(classResolverClass);
    }

    @Override
    public List<Statement> env(String varName) {
        if (classResolver.isEmpty()) {
            return Arrays.asList(
                    new VarInitStatement(
                            typeOf(OGNL_CONTEXT),
                            varName,
                            new NewExpr(
                                    "ognl.OgnlContext",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()));
        } else {
            Sym rootVar = new Sym(genSym.generate("root"));
            Sym classResolverVar = new Sym(genSym.generate("classResolver"));
            return Arrays.asList(
                    new VarInitStatement(
                            Type.OBJECT,
                            rootVar.getName(),
                            new NullExpr(),
                            Span.dummySpan()),
                    new VarInitStatement(
                            typeOf(CLASS_RESOLVER),
                            classResolverVar.getName(),
                            new NewExpr(
                                    packageName.equals("")
                                            ? ""
                                            : (packageName + ".") + name + "$ClassResolver",
                                    Arrays.asList(),
                                    Arrays.asList()),
                            Span.dummySpan()),
                    new VarInitStatement(
                            typeOf(OGNL_CONTEXT),
                            varName,
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
    public List<Statement> bind(String envVarName, Ident ident, Sym rhs) {
        Sym nameVar = new Sym(genSym.generate(ident.getName()));
        return Arrays.asList(new VarInitStatement(
                        Type.STRING,
                        nameVar.getName(),
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new VarInitStatement(
                        Type.OBJECT,
                        genSym.generate("__"),
                        new InvokeExpr(
                                classTypeOf(OGNL_CONTEXT),
                                new Sym(envVarName),
                                "put",
                                Arrays.asList(Type.OBJECT, Type.OBJECT),
                                Arrays.asList(nameVar, rhs),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(String causeVarName) {
        Sym caughtVar = new Sym(genSym.generate("ex"));
        Sym reasonVar = new Sym(genSym.generate("reason"));
        Sym nullValueVar = new Sym(genSym.generate("nullValue"));
        Sym condVar = new Sym(genSym.generate("cond"));
        return Optional.of(new CatchClause(
                classTypeOf(OGNL_EXCEPTION),
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar.getName(),
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
        Sym exprVar = new Sym(genSym.generate("expression"));
        Span span = quotedExpr.getSpan();
        return Arrays.asList(new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(Type.fromClass(expectedType), varName,
                        new InvokeStaticExpr(
                                classTypeOf(OGNL),
                                Arrays.asList(),
                                "getValue",
                                Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                Arrays.asList(exprVar,
                                        new Sym(envVarName),
                                        new Sym(envVarName)),
                                Type.OBJECT),
                        Span.dummySpan()));
    }
}
