package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Span;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.EqualOpExpr;
import yokohama.unit.ast_junit.IfStatement;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeExpr.Instruction;
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

public class OgnlExpressionStrategy implements ExpressionStrategy {
    static final ClassType OGNL = new ClassType("ognl.Ognl", Span.dummySpan());
    static final Type OGNL_CONTEXT = new Type(new ClassType("ognl.OgnlContext", Span.dummySpan()), 0);
    static final ClassType OGNL_EXCEPTION = new ClassType("ognl.OgnlException", Span.dummySpan());

    @Override
    public Collection<ClassDecl> auxClasses(
            String name,
            Group group,
            ClassResolver classResolver) {
        if (classResolver.isEmpty()) return Collections.emptyList();
        /*
        The following interface is implemented:
            public interface ClassResolver {
                Class classForName(Map context, String className) throws ClassNotFoundException;
            }
        */
        GenSym genSym = new GenSym();
        Var contextVar = new Var(genSym.generate("context"));
        Var classNameVar = new Var(genSym.generate("className"));

        Var tableVar = new Var(genSym.generate("table"));
        Stream<Statement> createTable = Stream.of(new VarInitStatement(
                        new Type(new ClassType("java.util.Map", Span.dummySpan()), 0),
                        tableVar.getName(),
                        new NewExpr("java.util.HashMap"),
                        Span.dummySpan()));

        Stream<Statement> populateTable =
                classResolver.flatMap((shortName, longName) -> {
                    Var shortNameVar = new Var(genSym.generate(SUtils.toIdent(shortName)));
                    Var longNameVar = new Var(genSym.generate(SUtils.toIdent(longName)));
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.STRING,
                                    shortNameVar.getName(),
                                    new StrLitExpr(shortName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.CLASS,
                                    longNameVar.getName(),
                                    new ClassLitExpr(new Type(new ClassType(longName, Span.dummySpan()), 0)),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.OBJECT,
                                    genSym.generate("__"),
                                    new InvokeExpr(
                                            Instruction.INTERFACE,
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
                                Instruction.INTERFACE,
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
                                                new ClassType("java.lang.Class", Span.dummySpan()),
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
                                new Type(new ClassType("java.util.Map", Span.dummySpan()), 0),
                                contextVar.getName())),
                Optional.of(Type.CLASS),
                Arrays.asList(new ClassType("java.lang.ClassNotFoundException", Span.dummySpan())),
                statements);
        ClassDecl classResolverClass = new ClassDecl(
                false,
                name + "$ClassResolver",
                Optional.empty(),
                Arrays.asList(new ClassType("ognl.ClassResolver", Span.dummySpan())),
                Arrays.asList(method));
        return Arrays.asList(classResolverClass);
    }

    @Override
    public List<Statement> env(
            String varName,
            String className,
            String packageName,
            ClassResolver classResolver,
            GenSym genSym) {
        if (classResolver.isEmpty()) {
            return Arrays.asList(
                    new VarInitStatement(
                            OGNL_CONTEXT,
                            varName,
                            new NewExpr("ognl.OgnlContext"),
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
                            new Type(new ClassType("ognl.ClassResolver", Span.dummySpan()), 0),
                            classResolverVar.getName(),
                            new NewExpr(packageName.equals("") ? "" : (packageName + ".")
                                    + className
                                    + "$ClassResolver"),
                            Span.dummySpan()),
                    new VarInitStatement(
                            OGNL_CONTEXT,
                            varName,
                            new InvokeStaticExpr(
                                    OGNL,
                                    Arrays.asList(),
                                    "createDefaultContext",
                                    Arrays.asList(
                                            Type.OBJECT,
                                            new Type(new ClassType("ognl.ClassResolver", Span.dummySpan()), 0)),
                                    Arrays.asList(
                                            rootVar,
                                            classResolverVar),
                                    new Type(new ClassType("java.util.Map", Span.dummySpan()), 0)),
                            Span.dummySpan()));
        }
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym) {
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
                                InvokeExpr.Instruction.VIRTUAL,
                                new Var(envVarName),
                                "put",
                                Arrays.asList(Type.OBJECT, Type.OBJECT),
                                Arrays.asList(nameVar, rhs),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public CatchClause catchAndAssignCause(String causeVarName, GenSym genSym) {
        Var caughtVar = new Var(genSym.generate("ex"));
        Var reasonVar = new Var(genSym.generate("reason"));
        Var nullValueVar = new Var(genSym.generate("nullValue"));
        Var condVar = new Var(genSym.generate("cond"));
        return new CatchClause(
                OGNL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar.getName(),
                                new InvokeExpr(
                                        InvokeExpr.Instruction.VIRTUAL,
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
                                                Span.dummySpan())))));
    }

    @Override
    public List<Statement> eval(
            String varName,
            String envVarName,
            QuotedExpr quotedExpr,
            GenSym genSym,
            String className,
            String packageName) {
        Var exprVar = new Var(genSym.generate("expression"));
        Span span = quotedExpr.getSpan();
        return Arrays.asList(
                new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, varName,
                        new InvokeStaticExpr(
                                new ClassType("ognl.Ognl", Span.dummySpan()),
                                Arrays.asList(),
                                "getValue",
                                Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                Arrays.asList(
                                        exprVar,
                                        new Var(envVarName),
                                        new Var(envVarName)),
                                Type.OBJECT),
                        span));
    }
}
