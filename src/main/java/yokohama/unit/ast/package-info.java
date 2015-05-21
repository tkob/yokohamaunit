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
 * Assertion ::= Proposition* Fixture
 * 
 * Proposition ::= QuotedExpr Predicate
 * 
 * Predicate ::= IsPredicate | IsNotPredicate | ThrowsPredicate
 * 
 * IsPredicate ::= Matcher
 * IsNotPredicate ::= Matcher
 * ThrowsPredicate ::= Matcher
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
 * Expr ::= QuotedExpr
 *        | StubExpr
 *        | InvocationExpr
 *        | IntegerExpr
 *        | FloatingPointExpr
 *        | BooleanExpr
 *        | CharExpr
 *        | StringExpr
 *        | AnchorExpr
 * 
 * StubExpr ::= ClassType StubBehavior*
 * 
 * StubBehavior ::= MethodPattern Expr
 * 
 * MethodPattern ::= Type*
 * 
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * InvocationExpr ::= MethodPattern Expr*
 * 
 * Fixture ::= | TableRef | Bindings
 * 
 * TableRef ::= Ident+ TableType
 * 
 * TableType :: INLINE | CSV | TSV | EXCEL
 * 
 * Bindings ::= Binding*
 * Binding ::= SingleBinding | ChoiceBinding
 * SingleBinding ::= Ident Expr
 * ChoiceBinding ::= Ident Expr+
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