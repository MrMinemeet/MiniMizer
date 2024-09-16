import controlFlowGraph.Block;
import controlFlowGraph.Constant;
import controlFlowGraph.Instruction;
import controlFlowGraph.Node;
import controlFlowGraph.Operation;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import kotlin.Pair;
import symbolTable.Obj;
import symbolTable.Struct;
import symbolTable.SymTab;

public class Parser {
	public static final int maxT = 37;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	private Token t;    // last recognized token
	private Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public final Errors errors;

	private final SymTab symTab = new SymTab(this);

	public Block entryBlock;
	private Block currentBlock;

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
		this.entryBlock = new Block(null);
		this.currentBlock = entryBlock;
	}

	private void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	private void SemErr () {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, "Type mismatch in term");
		errDist = 0;
	}
	
	private void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}
			la = t;
		}
	}
	
	private void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	private boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	private void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	private boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	private void Mini() {
		Expect(Token.IDs.PROGRAM);
		// Open "program" scope
		final Obj program = symTab.insert(Obj.Kind.PROGRAM, t.val, SymTab.Companion.getNO_TYPE(), 0);
		symTab.openScope();

		while (la.kind == Token.IDs.VAR) {
			VarDecl();
		}
		Expect(Token.IDs.BEGIN);
		StatSeq();
		Expect(Token.IDs.END);
		Expect(Token.IDs.DOT);

		// Close "program" scope
		program.getLocals().putAll(symTab.getCurScope().getImmutableLocals());
		symTab.closeScope();
	}

	private void VarDecl() {
		Expect(Token.IDs.VAR);
		while (la.kind == Token.IDs.IDENT) {
			IdListDecl();
			Expect(Token.IDs.SEMICOLON);
		}
	}

	private void StatSeq() {
		Statement();
		while (la.kind == Token.IDs.SEMICOLON) {
			Get();
			Statement();
		}
	}

	private void IdListDecl() {
		final List<Token> ids = new ArrayList<>();
		Expect(Token.IDs.IDENT);
		ids.add(t);
		while (la.kind == Token.IDs.COMMA) {
			Get(); // Remove `","`
			Expect(Token.IDs.IDENT);
			ids.add(t);
		}
		Expect(Token.IDs.COLON);
		final Struct varType = Type();
		if (Global.DEBUG) {
			for (Token id : ids) {
				System.out.format("(Line %s) Inserting variable '%s' of type '%s'\n", scanner.getLineNr(), id.val, varType);
				symTab.insert(Obj.Kind.VARIABLE, id.val, varType, 0);
			}
		}
	}

	private Struct Type() {
		if (la.kind == Token.IDs.IDENT) {
			Get();
			return SymTab.Companion.getINT_TYPE();
		} else if (la.kind == Token.IDs.ARRAY) {
			Get();
			Expect(Token.IDs.NUMBER);
			Expect(Token.IDs.OF);
			Struct subType = Type();
			return new Struct(Struct.Kind.ARRAY, subType);
		} else SynErr(38);
		throw new IllegalStateException("Unknown type of look-ahead");
	}

	private void Statement() {
		if (StartOf(1)) {
			if (la.kind == Token.IDs.IDENT) {
				final Pair<Obj, Node> designatorPair = Designator();
				final Obj designator = designatorPair.getFirst();
				final Node designatorNode = designatorPair.getSecond();
				Expect(Token.IDs.ASSIGN);
				Pair<Obj, Node> exprPair = Expression();
				Obj expr = exprPair.getFirst();
				if (designator.getObjType() != expr.getObjType()) {
					throw new IllegalStateException("Designator and expression have different types!");
				}

				System.out.printf("(Line: %d) Assigning '%s' (%s) to '%s' (%s)\n",
					scanner.getLineNr(),
				    (expr.getKind().equals(Obj.Kind.CONSTANT)) ? expr.getValue() : expr.getName(),
					expr.getObjType(),
					designator.getName(), designator.getObjType());

				// Generate IR instruction and add to current block
				Node ssa = symTab.addAsNewSSA(designator);
				generateInstruction(ssa, Operation.ASS, exprPair.getSecond());
				

			} else if (la.kind == Token.IDs.IF) {
				Get();
				Condition();
				Expect(Token.IDs.THEN);
				StatSeq();
				while (la.kind == Token.IDs.ELSIF) {
					Get();
					Condition();
					Expect(Token.IDs.THEN);
					StatSeq();
				}
				if (la.kind == Token.IDs.ELSE) {
					Get();
					StatSeq();
				}
				Expect(Token.IDs.END);
			} else if (la.kind == Token.IDs.WHILE) {
				Get();
				Condition();
				Expect(Token.IDs.DO);
				StatSeq();
				Expect(Token.IDs.END);
			} else if (la.kind == Token.IDs.READ) {
				Get();
				final Pair<Obj, Node> desgPair = Designator();
				final Obj desg = desgPair.getFirst();
				if (desg.getObjType() != SymTab.Companion.getINT_TYPE()) {
					throw new IllegalStateException(String.format("(Line %d) Read statement can only read integers!", scanner.getLineNr()));
				}
			} else {
				Get();
				final Pair<Obj, Node> exprPair = Expression();
				final Obj expr = exprPair.getFirst();
				if (expr.getObjType() != SymTab.Companion.getINT_TYPE()) {
					throw new IllegalStateException(String.format("(Line %d) Write statement can only write integers!", scanner.getLineNr()));
				}
			}
		}
	}

	private Pair<Obj, Node> Designator() {
		Expect(Token.IDs.IDENT);
		final Obj rootIdent = symTab.find(t.val);
		int indicesBracketCounter = 0;
		while (la.kind == Token.IDs.LBRACKET) {
			Get();
			final Pair<Obj, Node> indexValPair = Expression();
			final Obj indexVal = indexValPair.getFirst();
			if (indexVal.getObjType() != SymTab.Companion.getINT_TYPE()) {
				throw new IllegalStateException(String.format("(Line %d) Array index must be of type integer!", scanner.getLineNr()));
			}
			Expect(Token.IDs.RBRACKET);
			indicesBracketCounter++;
		}
		// Get the type of the indexed array
		Obj obj = rootIdent;
		if (indicesBracketCounter > 0) {
			Struct arrayType = getArrayObj(rootIdent, indicesBracketCounter);
			obj = new Obj(Obj.Kind.TYPE, "arrayAccess", arrayType, 0);
		}
		// TODO: Generate IR instruction node
		return new Pair<>(obj, symTab.find(obj.getName()));
	}

	private Struct getArrayObj(Obj rootObj, int indicesCount) {
		Struct arrayType = rootObj.getObjType();
		for (int i = 0; i < indicesCount; i++) {
			if (arrayType == null) {
				throw new IllegalStateException(String.format("(Line %d) Identifier '%s' is not an array. Likely too many indices brackets!", scanner.getLineNr(), rootObj.getName()));
			}
			if (arrayType.getKind() != Struct.Kind.ARRAY) {
				throw new IllegalStateException(String.format("(Line %d) '%s' is not an array. Cannot access with indices brackets", scanner.getLineNr(), rootObj.getName()));
			}
			arrayType = arrayType.getElemType();
		}
		return arrayType;
	}

	private Pair<Obj, Node> Expression() {
		boolean hasPrefixAddop = false;
		if (la.kind == Token.IDs.PLUS || la.kind == Token.IDs.MINUS) {
			Addop();
			hasPrefixAddop = true;
		}

		final Pair<Obj, Node> termPair = Term();
		final Obj term = termPair.getFirst();
		final Node termNode = termPair.getSecond();
		if (hasPrefixAddop && term.getObjType() != SymTab.Companion.getINT_TYPE()) {
			throw new IllegalStateException("Type mismatch in expression! Addop requires integer type!");
		}

		// Named x and y to be aligned with the examples from the lecture slides
		Node x = termNode;
		if (t.kind == Token.IDs.MINUS) {
			x = new Instruction(x, Operation.NEG);
		}

		while (la.kind == Token.IDs.PLUS || la.kind == Token.IDs.MINUS) {
			final int addOp = Addop();
			final Pair<Obj, Node> otherPair = Term();
			final Obj other = otherPair.getFirst();
			final Node otherNode = otherPair.getSecond();
			if (other.getObjType() != SymTab.Companion.getINT_TYPE()) {
				throw new IllegalStateException("Type mismatch in expression! Addop requires integer type!");
			}

			if (term.getObjType() != other.getObjType()) {
				throw new IllegalStateException(String.format("(Line %d) Terms in expression have different types!", scanner.getLineNr()));
			}

			// Generate additional IR instructions
			x = new Instruction(x, addOp == Token.IDs.PLUS ? Operation.PLUS : Operation.MINUS, otherNode);
		}

		if (x instanceof Instruction) {
			currentBlock.appendInstr((Instruction) x);
		}
		// TODO: Generate IR instruction node
		return new Pair<>(term, x);
	}

	private void Condition() {
		final Pair<Obj, Node> exprPair1 = Expression();
		final Obj expr1 = exprPair1.getFirst();
		Relop();
		final Pair<Obj, Node> exprPair2 = Expression();
		final Obj expr2 = exprPair2.getFirst();

		// Not clearly specified, so I'll disallow comparison of non-integer types
		if (expr1.getObjType() != SymTab.Companion.getINT_TYPE() || expr2.getObjType() != SymTab.Companion.getINT_TYPE()) {
			throw new IllegalStateException("Type mismatch in condition! Can only compare integers!");
		}
		System.out.printf("(Line %d) Comparing '%s' (%s) and '%s' (%s)\n",
			scanner.getLineNr(),
			(expr1.getKind().equals(Obj.Kind.CONSTANT)) ? expr1.getValue() : expr1.getName(),
			expr1.getObjType(),
			(expr2.getKind().equals(Obj.Kind.CONSTANT)) ? expr2.getValue() : expr2.getName(),
			expr2.getObjType());
	}

	private void Relop() {
		switch (la.kind) {
			case Token.IDs.EQUAL:
			case Token.IDs.NOT_EQUAL:
			case Token.IDs.LESS_THAN:
			case Token.IDs.GREATER_THAN:
			case Token.IDs.GREATER_EQUAL:
			case Token.IDs.LESS_EQUAL:
				Get();
				break;

			default:
				SynErr(39);
				break;
		}
	}

	private int Addop() {
		if (la.kind == Token.IDs.PLUS) {
			Get();
			return Token.IDs.PLUS;
		} else if (la.kind == Token.IDs.MINUS) {
			Get();
			return Token.IDs.MINUS;
		} else SynErr(40);
		return -1;
	}

	private Pair<Obj, Node> Term() {
		final Pair<Obj, Node> factorPair = Factor();
		assert factorPair != null;
		final Obj factor = factorPair.getFirst();
		final Node factorNode = factorPair.getSecond();
		while (la.kind == Token.IDs.MULTIPLY || la.kind == Token.IDs.DIVIDE || la.kind == Token.IDs.MODULO) {
			Mulop();
			final Pair<Obj, Node> otherPair = Factor();
			assert otherPair != null;
			final Obj other = otherPair.getFirst();
			if (factor.getObjType() != other.getObjType()) {
				SemErr();
			}
		}
		// TODO: Generate IR instruction node
		return new Pair<>(factor, factorNode);
	}

	private Pair<Obj, Node> Factor() {
		if (la.kind == Token.IDs.IDENT) {
			final Pair<Obj, Node> desgPair = Designator();
			final Obj desg = desgPair.getFirst();

			if (desg.getKind() == Obj.Kind.TYPE) {
				// If of kind "TYPE", then the designator is an array from which the type has been retrieved
				// TODO: Check if this is OK
				return desgPair;
			}

			// Else, do a lookup in the symbol table
			// TODO: Generate IR instruction node
			return new Pair<>(symTab.find(desg.getName()), null);
		} else if (la.kind == Token.IDs.NUMBER) {
			Get();
			final Obj constVal = new Obj(Obj.Kind.CONSTANT, "int", SymTab.Companion.getINT_TYPE(), Integer.parseInt(t.val));
			return new Pair<>(constVal, new Constant(constVal.getValue()));
		} else if (la.kind == Token.IDs.LPAREN) {
			Get();
			final Pair<Obj, Node> exprPair = Expression();
			Obj expr = exprPair.getFirst();
			Expect(Token.IDs.RPAREN);
			// TODO: Generate IR instruction node/get correct Node entry if required
			return new Pair<>(expr, null);
		} else SynErr(41);

		return null;
	}

	private void Mulop() {
		if (la.kind == Token.IDs.MULTIPLY) {
			Get();
		} else if (la.kind == Token.IDs.DIVIDE) {
			Get();
		} else if (la.kind == Token.IDs.MODULO) {
			Get();
		} else SynErr(42);
	}

	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Mini();
		Expect(Token.IDs.EOF);
	}

	/**
	 * Creates a new block and sets it as the current block.
	 * The new block is linked to the previous block as its dominator
	 * @param jump If true, then the new block is set as {@link Block#jumpSuc}, otherwise as {@link Block#seqSuc}.
	 * @return The old block, i.e. the previous {@link Parser#currentBlock}.
	 */
	private Block startNewBlock(boolean jump) {
		final Block oldBlock = currentBlock;
		currentBlock = new Block(currentBlock);
		if (oldBlock != null) {
			if (jump) {
				oldBlock.setJumpSuc(currentBlock);
			} else {
				oldBlock.setSeqSuc(currentBlock);
			}
		}
		return oldBlock;
	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_x, _x,_x,_T,_x, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x}
	};

	protected Instruction generateInstruction(Node first, Operation operator, Node second) {
		final Instruction instr = new Instruction(first, operator, second);
		currentBlock.appendInstr(instr);
		Global.debug(() -> instr.toIrPrintString());
		return instr;
	}
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuilder b = new StringBuilder(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b);
	}
	
	public void SynErr (int line, int col, int n) {
		String s = switch (n) {
			case 0 -> "EOF expected";
			case 1 -> "ident expected";
			case 2 -> "number expected";
			case 3 -> "\"PROGRAM\" expected";
			case 4 -> "\"BEGIN\" expected";
			case 5 -> "\"END\" expected";
			case 6 -> "\".\" expected";
			case 7 -> "\"VAR\" expected";
			case 8 -> "\";\" expected";
			case 9 -> "\",\" expected";
			case 10 -> "\":\" expected";
			case 11 -> "\"ARRAY\" expected";
			case 12 -> "\"OF\" expected";
			case 13 -> "\":=\" expected";
			case 14 -> "\"IF\" expected";
			case 15 -> "\"THEN\" expected";
			case 16 -> "\"ELSIF\" expected";
			case 17 -> "\"ELSE\" expected";
			case 18 -> "\"WHILE\" expected";
			case 19 -> "\"DO\" expected";
			case 20 -> "\"READ\" expected";
			case 21 -> "\"WRITE\" expected";
			case 22 -> "\"(\" expected";
			case 23 -> "\")\" expected";
			case 24 -> "\"[\" expected";
			case 25 -> "\"]\" expected";
			case 26 -> "\"=\" expected";
			case 27 -> "\"#\" expected";
			case 28 -> "\"<\" expected";
			case 29 -> "\">\" expected";
			case 30 -> "\">=\" expected";
			case 31 -> "\"<=\" expected";
			case 32 -> "\"+\" expected";
			case 33 -> "\"-\" expected";
			case 34 -> "\"*\" expected";
			case 35 -> "\"/\" expected";
			case 36 -> "\"%\" expected";
			case 37 -> "??? expected";
			case 38 -> "invalid Type";
			case 39 -> "invalid Relop";
			case 40 -> "invalid Addop";
			case 41 -> "invalid Factor";
			case 42 -> "invalid Mulop";
			default -> "error " + n;
		};
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
