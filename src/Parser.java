

public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _number = 2;
	public static final int maxT = 37;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	

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
		Expect(3);
		while (la.kind == 7) {
			VarDecl();
		}
		Expect(4);
		StatSeq();
		Expect(5);
		Expect(6);
	}

	void VarDecl() {
		Expect(7);
		while (la.kind == 1) {
			IdListDecl();
			Expect(8);
		}
	}

	void StatSeq() {
		Statement();
		while (la.kind == 8) {
			Get();
			Statement();
		}
	}

	void IdListDecl() {
		Expect(1);
		while (la.kind == 9) {
			Get();
			Expect(1);
		}
		Expect(10);
		Type();
	}

	void Type() {
		if (la.kind == 1) {
			Get();
		} else if (la.kind == 11) {
			Get();
			Expect(2);
			Expect(12);
			Type();
		} else SynErr(38);
	}

	void Statement() {
		if (StartOf(1)) {
			if (la.kind == 1) {
				Designator();
				Expect(13);
				Expression();
			} else if (la.kind == 14) {
				Get();
				Condition();
				Expect(15);
				StatSeq();
				while (la.kind == 16) {
					Get();
					Condition();
					Expect(15);
					StatSeq();
				}
				if (la.kind == 17) {
					Get();
					StatSeq();
				}
				Expect(5);
			} else if (la.kind == 18) {
				Get();
				Condition();
				Expect(19);
				StatSeq();
				Expect(5);
			} else if (la.kind == 20) {
				Get();
				Designator();
			} else {
				Get();
				Expression();
			}
		}
	}

	void Designator() {
		Expect(1);
		while (la.kind == 24) {
			Get();
			Expression();
			Expect(25);
		}
	}

	void Expression() {
		if (la.kind == 32 || la.kind == 33) {
			Addop();
		}
		Term();
		while (la.kind == 32 || la.kind == 33) {
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
		case 26: {
			Get();
			break;
		}
		case 27: {
			Get();
			break;
		}
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			break;
		}
		case 30: {
			Get();
			break;
		}
		case 31: {
			Get();
			break;
		}
		default: SynErr(39); break;
		}
	}

	void Addop() {
		if (la.kind == 32) {
			Get();
		} else if (la.kind == 33) {
			Get();
		} else SynErr(40);
	}

	void Term() {
		Factor();
		while (la.kind == 34 || la.kind == 35 || la.kind == 36) {
			Mulop();
			Factor();
		}
	}

	void Factor() {
		if (la.kind == 1) {
			Designator();
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 22) {
			Get();
			Expression();
			Expect(23);
		} else SynErr(41);
	}

	void Mulop() {
		if (la.kind == 34) {
			Get();
		} else if (la.kind == 35) {
			Get();
		} else if (la.kind == 36) {
			Get();
		} else SynErr(42);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Mini();
		Expect(0);

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
