import java.util.ArrayList;
import java.util.List;
import symtab.Obj;
import symtab.Struct;
import symtab.SymTab;

public class Parser {
	public static final int maxT = 37;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private SymTab symTab = new SymTab(this);

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
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
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
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
	
	void Mini() {
		Expect(Token.IDs.PROGRAM);
		// Open "program" scope
		final Obj program = symTab.insert(Obj.Kind.PROGRAM, t.val, SymTab.Companion.getNO_TYPE());
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

	void VarDecl() {
		Expect(Token.IDs.VAR);
		while (la.kind == Token.IDs.IDENT) {
			IdListDecl();
			Expect(Token.IDs.SEMICOLON);
		}
	}

	void StatSeq() {
		Statement();
		while (la.kind == Token.IDs.SEMICOLON) {
			Get();
			Statement();
		}
	}

	void IdListDecl() {
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
		for (Token id : ids) {
			System.out.format("Inserting variable '%s' of type '%s'\n", id.val, varType);
			symTab.insert(Obj.Kind.VARIABLE, id.val, varType);
		}
	}

	Struct Type() {
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

	void Statement() {
		if (StartOf(1)) {
			if (la.kind == Token.IDs.IDENT) {
				Designator();
				Expect(Token.IDs.ASSIGN);
				Expression();
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
				Designator();
			} else {
				Get();
				Expression();
			}
		}
	}

	void Designator() {
		Expect(Token.IDs.IDENT);
		while (la.kind == Token.IDs.LBRACKET) {
			Get();
			Expression();
			Expect(Token.IDs.RBRACKET);
		}
	}

	void Expression() {
		if (la.kind == Token.IDs.PLUS || la.kind == Token.IDs.MINUS) {
			Addop();
		}
		Term();
		while (la.kind == Token.IDs.PLUS || la.kind == Token.IDs.MINUS) {
			Addop();
			Term();
		}
	}

	void Condition() {
		Expression();
		Relop();
		Expression();
	}

	void Relop() {
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

	void Addop() {
		if (la.kind == Token.IDs.PLUS) {
			Get();
		} else if (la.kind == Token.IDs.MINUS) {
			Get();
		} else SynErr(40);
	}

	void Term() {
		Factor();
		while (la.kind == Token.IDs.MULTIPLY || la.kind == Token.IDs.DIVIDE || la.kind == Token.IDs.MODULO) {
			Mulop();
			Factor();
		}
	}

	void Factor() {
		if (la.kind == Token.IDs.IDENT) {
			Designator();
		} else if (la.kind == Token.IDs.NUMBER) {
			Get();
		} else if (la.kind == Token.IDs.LPAREN) {
			Get();
			Expression();
			Expect(Token.IDs.RPAREN);
		} else SynErr(41);
	}

	void Mulop() {
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

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_x, _x,_x,_T,_x, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "\"PROGRAM\" expected"; break;
			case 4: s = "\"BEGIN\" expected"; break;
			case 5: s = "\"END\" expected"; break;
			case 6: s = "\".\" expected"; break;
			case 7: s = "\"VAR\" expected"; break;
			case 8: s = "\";\" expected"; break;
			case 9: s = "\",\" expected"; break;
			case 10: s = "\":\" expected"; break;
			case 11: s = "\"ARRAY\" expected"; break;
			case 12: s = "\"OF\" expected"; break;
			case 13: s = "\":=\" expected"; break;
			case 14: s = "\"IF\" expected"; break;
			case 15: s = "\"THEN\" expected"; break;
			case 16: s = "\"ELSIF\" expected"; break;
			case 17: s = "\"ELSE\" expected"; break;
			case 18: s = "\"WHILE\" expected"; break;
			case 19: s = "\"DO\" expected"; break;
			case 20: s = "\"READ\" expected"; break;
			case 21: s = "\"WRITE\" expected"; break;
			case 22: s = "\"(\" expected"; break;
			case 23: s = "\")\" expected"; break;
			case 24: s = "\"[\" expected"; break;
			case 25: s = "\"]\" expected"; break;
			case 26: s = "\"=\" expected"; break;
			case 27: s = "\"#\" expected"; break;
			case 28: s = "\"<\" expected"; break;
			case 29: s = "\">\" expected"; break;
			case 30: s = "\">=\" expected"; break;
			case 31: s = "\"<=\" expected"; break;
			case 32: s = "\"+\" expected"; break;
			case 33: s = "\"-\" expected"; break;
			case 34: s = "\"*\" expected"; break;
			case 35: s = "\"/\" expected"; break;
			case 36: s = "\"%\" expected"; break;
			case 37: s = "??? expected"; break;
			case 38: s = "invalid Type"; break;
			case 39: s = "invalid Relop"; break;
			case 40: s = "invalid Addop"; break;
			case 41: s = "invalid Factor"; break;
			case 42: s = "invalid Mulop"; break;
			default: s = "error " + n; break;
		}
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
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
