/**
 * Abstract Syntax Tree.
 * 
 * <pre>
 * Group ::= Abbreviation* Definition*
 * 
 * Definition ::= Test | FourPhaseTest | Table | Heading | CodeBlock
 * 
 * Test ::= Assertion*
 * 
 * Assertion ::= Clause+ Fixture
 * Clause ::= Proposition+
 * Proposition ::= QuotedExpr Predicate
 * 
 * Predicate ::= IsPredicate | IsNotPredicate | ThrowsPredicate
 * 
 * IsPredicate ::= Matcher
 * IsNotPredicate ::= Matcher
 * ThrowsPredicate ::= Matcher
 * MatchesPredicate ::= Pattern
 * DoesNotMatchPredicate ::= Pattern
 * 
 * Matcher ::= EqualToMatcher
 *           | InstanceOfMatcher
 *           | InstanceSuchThatMatcher
 *           | NullValueMatcher
 * 
 * EqualToMatcher ::= QuotedExpr
 * InstanceOfMatcher ::= ClassType
 * InstanceSuchThatMatcher ::= ClassType Proposition+
 * 
 * Pattern ::= RegExpPattern
 * 
 * Expr ::= QuotedExpr
 *        | StubExpr
 *        | InvocationExpr
 *        | IntegerExpr
 *        | FloatingPointExpr
 *        | BooleanExpr
 *        | CharExpr
 *        | StringExpr
 *        | AnchorExpr
 *        | AsExpr
 *        | ResourceExpr
 * 
 * StubExpr ::= ClassType StubBehavior*
 * 
 * StubBehavior ::= StubReturns | StubThrows
 * StubReturns ::= MethodPattern Expr
 * StubThrows ::= MethodPattern Expr
 * 
 * MethodPattern ::= Type*
 * 
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * InvocationExpr ::= MethodPattern Expr*
 * 
 * AsExpr ::= Expr ClassType
 * 
 * Fixture ::= | TableRef | Bindings
 * 
 * TableRef ::= Ident+ TableType
 * 
 * TableType :: INLINE | CSV | TSV | EXCEL
 * 
 * Bindings ::= Binding*
 * Binding ::= SingleBinding | ChoiceBinding | TableBinding
 * SingleBinding ::= Ident Expr
 * ChoiceBinding ::= Ident Expr+
 * TableBinding ::= Ident* TableType
 * 
 * FourPhaseTest ::= Phase? Phase? VerifyPhase Phase?
 * 
 * Phase ::= LetStatement* Statement*
 * VerifyPhase ::= Assertion+
 * 
 * LetStatement ::= Binding+
 * 
 * Statement ::= Execution | Invoke
 * 
 * Execution ::= QuotedExpr*
 * Invoke ::= MethodPattern Expr*
 * 
 * Table ::= Ident* Row*
 * 
 * Row ::= Cell+
 * 
 * Cell ::= ExprCell | PredCell
 * 
 * Code Block ::= Heading
 * </pre>
 * 
 */
package yokohama.unit.ast;