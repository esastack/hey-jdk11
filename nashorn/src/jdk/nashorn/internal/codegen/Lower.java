/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.nashorn.internal.codegen;

import static jdk.nashorn.internal.codegen.CompilerConstants.EVAL;
import static jdk.nashorn.internal.codegen.CompilerConstants.RETURN;
import static jdk.nashorn.internal.codegen.CompilerConstants.THIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.nashorn.internal.ir.BaseNode;
import jdk.nashorn.internal.ir.BinaryNode;
import jdk.nashorn.internal.ir.Block;
import jdk.nashorn.internal.ir.BlockLexicalContext;
import jdk.nashorn.internal.ir.BreakNode;
import jdk.nashorn.internal.ir.CallNode;
import jdk.nashorn.internal.ir.CatchNode;
import jdk.nashorn.internal.ir.ContinueNode;
import jdk.nashorn.internal.ir.EmptyNode;
import jdk.nashorn.internal.ir.ExecuteNode;
import jdk.nashorn.internal.ir.ForNode;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.FunctionNode.CompilationState;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.ir.LabelNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.ir.LineNumberNode;
import jdk.nashorn.internal.ir.LiteralNode;
import jdk.nashorn.internal.ir.LoopNode;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.ReturnNode;
import jdk.nashorn.internal.ir.SwitchNode;
import jdk.nashorn.internal.ir.Symbol;
import jdk.nashorn.internal.ir.ThrowNode;
import jdk.nashorn.internal.ir.TryNode;
import jdk.nashorn.internal.ir.UnaryNode;
import jdk.nashorn.internal.ir.VarNode;
import jdk.nashorn.internal.ir.WhileNode;
import jdk.nashorn.internal.ir.WithNode;
import jdk.nashorn.internal.ir.visitor.NodeOperatorVisitor;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;
import jdk.nashorn.internal.parser.Token;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.DebugLogger;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.Source;

/**
 * Lower to more primitive operations. After lowering, an AST still has no symbols
 * and types, but several nodes have been turned into more low level constructs
 * and control flow termination criteria have been computed.
 *
 * We do things like code copying/inlining of finallies here, as it is much
 * harder and context dependent to do any code copying after symbols have been
 * finalized.
 */

final class Lower extends NodeOperatorVisitor {

    private static final DebugLogger LOG = new DebugLogger("lower");

    /**
     * Constructor.
     *
     * @param compiler the compiler
     */
    Lower() {
        super(new BlockLexicalContext() {

            @Override
            public List<Node> popStatements() {
                List<Node> newStatements = new ArrayList<>();
                boolean terminated = false;

                final List<Node> statements = super.popStatements();
                for (final Node statement : statements) {
                    if (!terminated) {
                        newStatements.add(statement);
                        if (statement.isTerminal()) {
                            terminated = true;
                        }
                    } else {
                        if (statement instanceof VarNode) {
                            newStatements.add(((VarNode)statement).setInit(null));
                        }
                    }
                }
                return newStatements;
            }
        });
    }

    @Override
    public boolean enterBlock(final Block block) {
        final LexicalContext lc = getLexicalContext();
        if (lc.isFunctionBody() && lc.getCurrentFunction().isProgram() && !lc.getCurrentFunction().hasDeclaredFunctions()) {
            new ExecuteNode(block.getSource(), block.getToken(), block.getFinish(), LiteralNode.newInstance(block, ScriptRuntime.UNDEFINED)).accept(this);
        }
        return true;
    }

    @Override
    public Node leaveBlock(final Block block) {
        //now we have committed the entire statement list to the block, but we need to truncate
        //whatever is after the last terminal. block append won't append past it

        final BlockLexicalContext lc = (BlockLexicalContext)getLexicalContext();

        Node last = lc.getLastStatement();

        if (lc.isFunctionBody()) {
            final FunctionNode currentFunction = getLexicalContext().getCurrentFunction();
            final boolean isProgram = currentFunction.isProgram();
            final ReturnNode returnNode = new ReturnNode(
                currentFunction.getSource(),
                currentFunction.getToken(),
                currentFunction.getFinish(),
                isProgram ?
                    compilerConstant(RETURN) :
                    LiteralNode.newInstance(block, ScriptRuntime.UNDEFINED));

            last = returnNode.accept(this);
        }

        if (last != null && last.isTerminal()) {
            return block.setIsTerminal(lc, true);
        }

        return block;
    }

    @Override
    public boolean enterBreakNode(final BreakNode breakNode) {
        addStatement(breakNode);
        return false;
    }

    @Override
    public Node leaveCallNode(final CallNode callNode) {
        return checkEval(callNode.setFunction(markerFunction(callNode.getFunction())));
    }

    @Override
    public Node leaveCatchNode(final CatchNode catchNode) {
        return addStatement(catchNode);
    }

    @Override
    public boolean enterContinueNode(final ContinueNode continueNode) {
        addStatement(continueNode);
        return false;
    }

    @Override
    public boolean enterEmptyNode(final EmptyNode emptyNode) {
        return false;
    }

    @Override
    public Node leaveExecuteNode(final ExecuteNode executeNode) {
        final Node expr = executeNode.getExpression();
        ExecuteNode node = executeNode;

        final FunctionNode currentFunction = getLexicalContext().getCurrentFunction();

        if (currentFunction.isProgram()) {
            if (!(expr instanceof Block) || expr instanceof FunctionNode) { // it's not a block, but can be a function
                if (!isInternalExpression(expr) && !isEvalResultAssignment(expr)) {
                    node = executeNode.setExpression(
                        new BinaryNode(
                            executeNode.getSource(),
                            Token.recast(
                                executeNode.getToken(),
                                TokenType.ASSIGN),
                            compilerConstant(RETURN),
                        expr));
                }
            }
        }

        return addStatement(node);
    }

    @Override
    public Node leaveForNode(final ForNode forNode) {
        ForNode newForNode = forNode;

        final Node  test = forNode.getTest();
        if (!forNode.isForIn() && conservativeAlwaysTrue(test)) {
            newForNode = forNode.setTest(getLexicalContext(), null);
        }

        return addStatement(checkEscape(newForNode));
    }

    @Override
    public boolean enterFunctionNode(final FunctionNode functionNode) {
        return !functionNode.isLazy();
    }

    @Override
    public Node leaveFunctionNode(final FunctionNode functionNode) {
        LOG.info("END FunctionNode: ", functionNode.getName());
        return functionNode.setState(getLexicalContext(), CompilationState.LOWERED);
    }

    @Override
    public Node leaveIfNode(final IfNode ifNode) {
        return addStatement(ifNode);
    }

    @Override
    public Node leaveLabelNode(final LabelNode labelNode) {
        return addStatement(labelNode);
    }

    @Override
    public boolean enterLineNumberNode(final LineNumberNode lineNumberNode) {
        addStatement(lineNumberNode); // don't put it in lastStatement cache
        return false;
    }

    @Override
    public Node leaveReturnNode(final ReturnNode returnNode) {
        addStatement(returnNode); //ReturnNodes are always terminal, marked as such in constructor
        return returnNode;
    }


    @Override
    public Node leaveSwitchNode(final SwitchNode switchNode) {
        return addStatement(switchNode);
    }

    @Override
    public Node leaveThrowNode(final ThrowNode throwNode) {
        addStatement(throwNode); //ThrowNodes are always terminal, marked as such in constructor
        return throwNode;
    }

    private static Node ensureUniqueLabelsIn(final Node node) {
        return node.accept(new NodeVisitor() {
           @Override
           public Node leaveDefault(final Node labelledNode) {
               return labelledNode.ensureUniqueLabels(getLexicalContext());
           }
        });
    }

    private static List<Node> copyFinally(final Block finallyBody) {
        final List<Node> newStatements = new ArrayList<>();
        for (final Node statement : finallyBody.getStatements()) {
            newStatements.add(ensureUniqueLabelsIn(statement));
            if (statement.hasTerminalFlags()) {
                return newStatements;
            }
        }
        return newStatements;
    }

    private Block catchAllBlock(final TryNode tryNode) {
        final Source source = tryNode.getSource();
        final long   token  = tryNode.getToken();
        final int    finish = tryNode.getFinish();

        final IdentNode exception = new IdentNode(source, token, finish, getLexicalContext().getCurrentFunction().uniqueName("catch_all"));

        final Block catchBody = new Block(source, token, finish, new ThrowNode(source, token, finish, new IdentNode(exception))).
                setIsTerminal(getLexicalContext(), true); //ends with throw, so terminal

        final CatchNode catchAllNode  = new CatchNode(source, token, finish, new IdentNode(exception), null, catchBody);
        final Block     catchAllBlock = new Block(source, token, finish, catchAllNode);

        //catchallblock -> catchallnode (catchnode) -> exception -> throw

        return (Block)catchAllBlock.accept(this); //not accepted. has to be accepted by lower
    }

    private IdentNode compilerConstant(final CompilerConstants cc) {
        final FunctionNode functionNode = getLexicalContext().getCurrentFunction();
        return new IdentNode(functionNode.getSource(), functionNode.getToken(), functionNode.getFinish(), cc.symbolName());
    }

    private static boolean isTerminal(final List<Node> statements) {
        return !statements.isEmpty() && statements.get(statements.size() - 1).hasTerminalFlags();
    }

    /**
     * Splice finally code into all endpoints of a trynode
     * @param tryNode the try node
     * @param list of rethrowing throw nodes from synthetic catch blocks
     * @param finallyBody the code in the original finally block
     * @return new try node after splicing finally code (same if nop)
     */
    private Node spliceFinally(final TryNode tryNode, final List<ThrowNode> rethrows, final Block finallyBody) {
        final Source source = tryNode.getSource();
        final int    finish = tryNode.getFinish();

        assert tryNode.getFinallyBody() == null;

        final TryNode newTryNode = (TryNode)tryNode.accept(new NodeVisitor() {
            final List<Node> insideTry = new ArrayList<>();

            @Override
            public boolean enterDefault(final Node node) {
                insideTry.add(node);
                return true;
            }

            @Override
            public boolean enterFunctionNode(final FunctionNode functionNode) {
                // do not enter function nodes - finally code should not be inlined into them
                return false;
            }

            @Override
            public Node leaveThrowNode(final ThrowNode throwNode) {
                if (rethrows.contains(throwNode)) {
                    final List<Node> newStatements = copyFinally(finallyBody);
                    if (!isTerminal(newStatements)) {
                        newStatements.add(throwNode);
                    }
                    return new Block(source, throwNode.getToken(), throwNode.getFinish(), newStatements);
                }
                return throwNode;
            }

            @Override
            public Node leaveBreakNode(final BreakNode breakNode) {
                return copy(breakNode, Lower.this.getLexicalContext().getBreakable(breakNode.getLabel()));
            }

            @Override
            public Node leaveContinueNode(final ContinueNode continueNode) {
                return copy(continueNode, Lower.this.getLexicalContext().getContinueTo(continueNode.getLabel()));
            }

            @Override
            public Node leaveReturnNode(final ReturnNode returnNode) {
                final Node  expr  = returnNode.getExpression();
                final List<Node> newStatements = new ArrayList<>();

                final Node resultNode;
                if (expr != null) {
                    //we need to evaluate the result of the return in case it is complex while
                    //still in the try block, store it in a result value and return it afterwards
                    resultNode = new IdentNode(Lower.this.compilerConstant(RETURN));
                    newStatements.add(new ExecuteNode(new BinaryNode(source, Token.recast(returnNode.getToken(), TokenType.ASSIGN), resultNode, expr)));
                } else {
                    resultNode = null;
                }

                newStatements.addAll(copyFinally(finallyBody));
                if (!isTerminal(newStatements)) {
                    newStatements.add(expr == null ? returnNode : returnNode.setExpression(resultNode));
                }

                return new ExecuteNode(new Block(source, returnNode.getToken(), getLexicalContext().getCurrentBlock().getFinish(), newStatements));
            }

            private Node copy(final Node endpoint, final Node targetNode) {
                if (!insideTry.contains(targetNode)) {
                    final List<Node> newStatements = copyFinally(finallyBody);
                    if (!isTerminal(newStatements)) {
                        newStatements.add(endpoint);
                    }
                    return new ExecuteNode(new Block(source, endpoint.getToken(), finish, newStatements));
                }
                return endpoint;
            }
        });

        addStatement(newTryNode);
        for (final Node statement : finallyBody.getStatements()) {
            addStatement(statement);
        }

        return newTryNode;
    }

    @Override
    public Node leaveTryNode(final TryNode tryNode) {
        final Block finallyBody = tryNode.getFinallyBody();

        if (finallyBody == null) {
            return addStatement(tryNode);
        }

        /*
         * create a new trynode
         *    if we have catches:
         *
         *    try            try
         *       x              try
         *    catch               x
         *       y              catch
         *    finally z           y
         *                   catchall
         *                        rethrow
         *
         *   otheriwse
         *
         *   try              try
         *      x               x
         *   finally          catchall
         *      y               rethrow
         *
         *
         *   now splice in finally code wherever needed
         *
         */
        TryNode newTryNode;

        final Block catchAll = catchAllBlock(tryNode);

        final List<ThrowNode> rethrows = new ArrayList<>();
        catchAll.accept(new NodeVisitor() {
            @Override
            public boolean enterThrowNode(final ThrowNode throwNode) {
                rethrows.add(throwNode);
                return true;
            }
        });
        assert rethrows.size() == 1;

        if (tryNode.getCatchBlocks().isEmpty()) {
            newTryNode = tryNode.setFinallyBody(null);
        } else {
            Block outerBody = new Block(tryNode.getSource(), tryNode.getToken(), tryNode.getFinish(), new ArrayList<Node>(Arrays.asList(tryNode.setFinallyBody(null))));
            newTryNode = tryNode.setBody(outerBody).setCatchBlocks(null);
        }

        newTryNode = newTryNode.setCatchBlocks(Arrays.asList(catchAll)).setFinallyBody(null);

        /*
         * Now that the transform is done, we have to go into the try and splice
         * the finally block in front of any statement that is outside the try
         */
        return spliceFinally(newTryNode, rethrows, finallyBody);
    }

    @Override
    public Node leaveVarNode(final VarNode varNode) {
        addStatement(varNode);
        if (varNode.getFlag(VarNode.IS_LAST_FUNCTION_DECLARATION) && getLexicalContext().getCurrentFunction().isProgram()) {
            new ExecuteNode(varNode.getSource(), varNode.getToken(), varNode.getFinish(), new IdentNode(varNode.getName())).accept(this);
        }
        return varNode;
    }

    @Override
    public Node leaveWhileNode(final WhileNode whileNode) {
        final Node test = whileNode.getTest();
        final Block body = whileNode.getBody();

        if (conservativeAlwaysTrue(test)) {
            //turn it into a for node without a test.
            final ForNode forNode = (ForNode)new ForNode(whileNode.getSource(), whileNode.getToken(), whileNode.getFinish(), null, null, body, null, ForNode.IS_FOR).accept(this);
            getLexicalContext().replace(whileNode, forNode);
            return forNode;
        }

         return addStatement(checkEscape(whileNode));
    }

    @Override
    public Node leaveWithNode(final WithNode withNode) {
        return addStatement(withNode);
    }

    @Override
    public Node leaveDELETE(final UnaryNode unaryNode) {
        final Node rhs = unaryNode.rhs();
        if (rhs instanceof IdentNode || rhs instanceof BaseNode) {
            return unaryNode;
        }
        addStatement(new ExecuteNode(rhs));
        return LiteralNode.newInstance(unaryNode, true);
    }

    /**
     * Given a function node that is a callee in a CallNode, replace it with
     * the appropriate marker function. This is used by {@link CodeGenerator}
     * for fast scope calls
     *
     * @param function function called by a CallNode
     * @return transformed node to marker function or identity if not ident/access/indexnode
     */
    private static Node markerFunction(final Node function) {
        if (function instanceof IdentNode) {
            return ((IdentNode)function).setIsFunction();
        } else if (function instanceof BaseNode) {
            return ((BaseNode)function).setIsFunction();
        }
        return function;
    }

    /**
     * Calculate a synthetic eval location for a node for the stacktrace, for example src#17<eval>
     * @param node a node
     * @return eval location
     */
    private static String evalLocation(final IdentNode node) {
        return new StringBuilder().
            append(node.getSource().getName()).
            append('#').
            append(node.getSource().getLine(node.position())).
            append("<eval>").
            toString();
    }

    /**
     * Check whether a call node may be a call to eval. In that case we
     * clone the args in order to create the following construct in
     * {@link CodeGenerator}
     *
     * <pre>
     * if (calledFuntion == buildInEval) {
     *    eval(cloned arg);
     * } else {
     *    cloned arg;
     * }
     * </pre>
     *
     * @param callNode call node to check if it's an eval
     */
    private CallNode checkEval(final CallNode callNode) {
        if (callNode.getFunction() instanceof IdentNode) {

            final List<Node> args   = callNode.getArgs();
            final IdentNode  callee = (IdentNode)callNode.getFunction();

            // 'eval' call with at least one argument
            if (args.size() >= 1 && EVAL.symbolName().equals(callee.getName())) {
                final FunctionNode currentFunction = getLexicalContext().getCurrentFunction();
                return callNode.setEvalArgs(
                    new CallNode.EvalArgs(
                        ensureUniqueLabelsIn(args.get(0)).accept(this),
                        compilerConstant(THIS),
                        evalLocation(callee),
                        currentFunction.isStrict()));
            }
        }

        return callNode;
    }

    private static boolean conservativeAlwaysTrue(final Node node) {
        return node == null || ((node instanceof LiteralNode) && Boolean.TRUE.equals(((LiteralNode<?>)node).getValue()));
    }

    /**
     * Helper that given a loop body makes sure that it is not terminal if it
     * has a continue that leads to the loop header or to outer loops' loop
     * headers. This means that, even if the body ends with a terminal
     * statement, we cannot tag it as terminal
     *
     * @param loopBody the loop body to check
     * @return true if control flow may escape the loop
     */
    private static boolean controlFlowEscapes(final LexicalContext lex, final Block loopBody) {
        final List<Node> escapes = new ArrayList<>();

        loopBody.accept(new NodeVisitor() {
            @Override
            public Node leaveBreakNode(final BreakNode node) {
                escapes.add(node);
                return node;
            }

            @Override
            public Node leaveContinueNode(final ContinueNode node) {
                // all inner loops have been popped.
                if (lex.contains(lex.getContinueTo(node.getLabel()))) {
                    escapes.add(node);
                }
                return node;
            }
        });

        return !escapes.isEmpty();
    }

    private LoopNode checkEscape(final LoopNode loopNode) {
        final LexicalContext lc = getLexicalContext();
        final boolean escapes = controlFlowEscapes(lc, loopNode.getBody());
        if (escapes) {
            return loopNode.
                setBody(lc, loopNode.getBody().setIsTerminal(lc, false)).
                setControlFlowEscapes(lc, escapes);
        }
        return loopNode;
    }


    private Node addStatement(final Node statement) {
        ((BlockLexicalContext)getLexicalContext()).appendStatement(statement);
        return statement;
    }

    /**
     * An internal expression has a symbol that is tagged internal. Check if
     * this is such a node
     *
     * @param expression expression to check for internal symbol
     * @return true if internal, false otherwise
     */
    private static boolean isInternalExpression(final Node expression) {
        final Symbol symbol = expression.getSymbol();
        return symbol != null && symbol.isInternal();
    }

    /**
     * Is this an assignment to the special variable that hosts scripting eval
     * results, i.e. __return__?
     *
     * @param expression expression to check whether it is $evalresult = X
     * @return true if an assignment to eval result, false otherwise
     */
    private static boolean isEvalResultAssignment(final Node expression) {
        Node e = expression;
        assert e.tokenType() != TokenType.DISCARD; //there are no discards this early anymore
        if (e instanceof BinaryNode) {
            final Node lhs = ((BinaryNode)e).lhs();
            if (lhs instanceof IdentNode) {
                return ((IdentNode)lhs).getName().equals(RETURN.symbolName());
            }
        }
        return false;
    }

}
