package yokohama.unit.translator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import yokohama.unit.ast.Action;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Binding;
import yokohama.unit.ast.Bindings;
import yokohama.unit.ast.Copula;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.LetBinding;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.TableType;
import yokohama.unit.ast.Test;
import yokohama.unit.ast.VerifyPhase;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParserVisitor;

public class ParseTreeToAstVisitor extends AbstractParseTreeVisitor<Object> implements YokohamaUnitParserVisitor<Object> 
{

    @Override
    public Group visitGroup(YokohamaUnitParser.GroupContext ctx) {
        List<Definition>definitions =
                ctx.definition().stream()
                                .map(this::visitDefinition)
                                .collect(Collectors.toList());
        return new Group(definitions);
    }

    @Override
    public Definition visitDefinition(YokohamaUnitParser.DefinitionContext ctx) {
        return (Definition)visitChildren(ctx);
    }

    @Override
    public Test visitTest(YokohamaUnitParser.TestContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        String name = ctx.TestName().getText();
        List<Assertion> assertions =
                ctx.assertion().stream()
                               .map(this::visitAssertion)
                               .collect(Collectors.toList());
        return new Test(name, assertions, numHashes);
    }

    @Override
    public Integer visitHash(YokohamaUnitParser.HashContext ctx) {
        return ctx.getText().length();
    }

    @Override
    public Assertion visitAssertion(YokohamaUnitParser.AssertionContext ctx) {
        List<Proposition> propositions = visitPropositions(ctx.propositions());
        YokohamaUnitParser.ConditionContext conditionCtx = (ctx.condition());
        Fixture fixture =
                conditionCtx == null ? Fixture.none()
                                     : visitCondition(ctx.condition());
        return new Assertion(propositions, fixture);
    }

    @Override
    public List<Proposition> visitPropositions(YokohamaUnitParser.PropositionsContext ctx) {
        return ctx.proposition().stream()
                                .map(this::visitProposition)
                                .collect(Collectors.toList());
    }

    @Override
    public Proposition visitProposition(YokohamaUnitParser.PropositionContext ctx) {
        Expr subject = new Expr(ctx.Expr(0).getText());
        Copula copula = visitCopula(ctx.copula());
        Expr complement = new Expr(ctx.Expr(1).getText());
        return new Proposition(subject, copula, complement);
    }

    @Override
    public Copula visitCopula(YokohamaUnitParser.CopulaContext ctx) {
        String copulaText = ctx.getText();
        switch (copulaText) {
            case "is":
                return Copula.IS;
            case "isnot":
                return Copula.IS_NOT;
            case "throws":
                return Copula.THROWS;
        }
        throw new IllegalArgumentException("'" + copulaText + "' is not a copula.");
    }

    @Override
    public Fixture visitCondition(YokohamaUnitParser.ConditionContext ctx) {
        return (Fixture)visitChildren(ctx);
    }

    @Override
    public TableRef visitTableRef(YokohamaUnitParser.TableRefContext ctx) {
        TableType tableType = visitTableType(ctx.tableType());
        String name = ctx.Quoted().getText();
        return new TableRef(tableType, name);
    }

    @Override
    public TableType visitTableType(YokohamaUnitParser.TableTypeContext ctx) {
        String text = ctx.getText();
        switch (text) {
            case "Table":
                return TableType.INLINE;
            case "CSV":
                return TableType.CSV;
            case "TSV":
                return TableType.TSV;
            case "Excel":
                return TableType.EXCEL;
       }
        throw new IllegalArgumentException("'" + text + "' is not a table type.");
    }

    @Override
    public Bindings visitBindings(YokohamaUnitParser.BindingsContext ctx) {
        List<Binding> bindings = ctx.binding().stream()
                                              .map(this::visitBinding)
                                              .collect(Collectors.toList());
	return new Bindings(bindings);
   }

    @Override
    public Binding visitBinding(YokohamaUnitParser.BindingContext ctx) {
        String ident = ctx.Identifier().getText();
        Expr expr = new Expr(ctx.Expr().getText());
        return new Binding(ident, expr);
    }

    @Override
    public Table visitTableDef(YokohamaUnitParser.TableDefContext ctx) {
        String name = ctx.TableName().getText();
        List<String> header = visitHeader(ctx.header());
        List<Row> rows = visitRows(ctx.rows());
        return new Table(name, header, rows);
    }

    @Override
    public List<String> visitHeader(YokohamaUnitParser.HeaderContext ctx) {
        return ctx.Identifier().stream()
                               .map(TerminalNode::getText)
                               .collect(Collectors.toList());
    }

    @Override
    public List<Row> visitRows(YokohamaUnitParser.RowsContext ctx) {
        return ctx.row().stream()
                        .map(this::visitRow)
                        .collect(Collectors.toList());
    }

    @Override
    public Row visitRow(YokohamaUnitParser.RowContext ctx) {
        List<Expr> exprs = ctx.Expr().stream()
                                     .map(TerminalNode::getText)
                                     .map(String::trim)
                                     .map(Expr::new)
                                     .collect(Collectors.toList());
        return new Row(exprs);
    }

    @Override
    public FourPhaseTest visitFourPhaseTest(YokohamaUnitParser.FourPhaseTestContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        String name = ctx.TestName().getText();
        Optional<Phase> setup =
                ctx.setup() == null ? Optional.empty()
                                    : Optional.of(visitSetup(ctx.setup()));
        Optional<Phase> exercise =
                ctx.exercise() == null ? Optional.empty()
                                       : Optional.of(visitExercise(ctx.exercise()));
        VerifyPhase verify = visitVerify(ctx.verify());
        Optional<Phase> teardown =
                ctx.teardown() == null ? Optional.empty()
                                       : Optional.of(visitTeardown(ctx.teardown()));
        return new FourPhaseTest(numHashes, name, setup, exercise, verify, teardown);
    }

    @Override
    public Phase visitSetup(YokohamaUnitParser.SetupContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        Optional<LetBindings> letBindings =
                ctx.letBindings() == null ? Optional.empty()
                                          : Optional.of(visitLetBindings(ctx.letBindings()));
        List<Action> actions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, letBindings, actions);
    }

    @Override
    public Phase visitExercise(YokohamaUnitParser.ExerciseContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Action> actions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, Optional.empty(), actions);
    }

    @Override
    public VerifyPhase visitVerify(YokohamaUnitParser.VerifyContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Assertion> assertions = ctx.assertion()
                .stream()
                .map(this::visitAssertion)
                .collect(Collectors.toList());
        return new VerifyPhase(numHashes, description, assertions);
    }

    @Override
    public Phase visitTeardown(YokohamaUnitParser.TeardownContext ctx) {
        int numHashes = ctx.hash() == null ? 0 : visitHash(ctx.hash());
        Optional<String> description =
                ctx.PhaseDescription() == null ? Optional.empty()
                                               : Optional.of(ctx.PhaseDescription().getText());
        List<Action> actions = ctx.execution()
                .stream()
                .map(this::visitExecution)
                .collect(Collectors.toList());
        return new Phase(numHashes, description, Optional.empty(), actions);
    }

    @Override
    public LetBindings visitLetBindings(YokohamaUnitParser.LetBindingsContext ctx) {
        return new LetBindings(
                ctx.letBinding().stream()
                        .map(this::visitLetBinding)
                        .collect(Collectors.toList()));
    }

    @Override
    public LetBinding visitLetBinding(YokohamaUnitParser.LetBindingContext ctx) {
        return new LetBinding(
                ctx.Identifier().getText(),
                new Expr(ctx.Expr().getText()));
    }

    @Override
    public Execution visitExecution(YokohamaUnitParser.ExecutionContext ctx) {
        return new Execution(
                ctx.Expr().stream()
                        .map(expr -> new Expr(expr.getText()))
                        .collect(Collectors.toList()));
    }

}
