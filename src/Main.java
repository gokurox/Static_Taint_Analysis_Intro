import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Gursimran_Singh
 * @rollno 2014041
 */

/*
 *	ASSUMPTIONS made for this assignment are provided in the file README.pdf
 *	Please have a look for assessment.
 */

public class Main {
	final String FILEPATH = "input/toycode.toy";
	HashSet<String> VARIABLES = new HashSet<>();
	HashMap<String, Integer> TAINTED = new HashMap<>();
	
 	public static void main(String[] args) {
 		Main instance = new Main();
		ArrayList<String> codeInLines = instance.readFile (instance.FILEPATH);
		
		if (codeInLines != null) {
			for (int i = 0; i < codeInLines.size(); i++) {
				String currentLine = codeInLines.get(i);
				int lineNumber = i +1;
				switch (instance.findStatementType (currentLine)) {
					case FUNCTION_PROTOTYPE:
						instance.analyze_FuncProto (currentLine.trim(), lineNumber);
						break;
					case VARIABLE_DECLARATION:
						instance.analyze_VarDecl (currentLine.trim(), lineNumber);
						break;
					case VARIABLE_DEFINITION:
						instance.analyze_VarDef (currentLine.trim(), lineNumber);
						break;
					case WHILE:
						instance.analyze_While (currentLine.trim(), lineNumber);
						break;
					case IF:
						instance.analyze_If (currentLine.trim(), lineNumber);
						break;
					case ELSE:
						instance.analyze_Else (currentLine.trim(), lineNumber);
						break;
					case ASSIGNMENT:
						instance.analyze_Assign (currentLine.trim(), lineNumber);
						break;
					case RETURN:
						instance.analyze_Return (currentLine.trim(), lineNumber);
						break;
					case MISC:
						instance.analyze_Misc (currentLine.trim(), lineNumber);
						break;
					default:
						break;
				}
			}
			
			instance.printAnalysis();
		}
		else {
			System.err.println ("ReadFile returned NULL");
			System.exit (0);
		}
	}
 	
 	public ArrayList<String> readFile (String filePath) {
 		ArrayList<String> fileInLines = new ArrayList<>();
 		BufferedReader br;
 		String currentLine;

 		try {
 			br = new BufferedReader (new FileReader (filePath));
 		} catch (FileNotFoundException fnfEx) {
 			fnfEx.printStackTrace();
 			return null;
 		}
 		
 		try {
 			while ((currentLine = br.readLine()) != null) {
 				fileInLines.add (currentLine);
 			}
 			br.close();
 		} catch (IOException ioEx) {
 			ioEx.printStackTrace();
 			return null;
 		}

 		return fileInLines;
 	}
 	
 	public Statement findStatementType (String input) {
 		// REGEX AS JAVA STRINGS
 		String regex_FUNCTION_PROTOTYPE = "^\\s*int\\s+\\w+\\s*\\((\\s*int\\s+\\w+\\s*,{1})*(\\s*int\\s+\\w+\\s*)?\\)\\s*\\{?[ \\t]*$";
 		String regex_VARIABLE_DECLARATION = "^\\s*int\\s+\\w+\\s*;[ \\t]*$";
 		String regex_VARIABLE_DEFINITION = "^\\s*int\\s+\\w+\\s*=\\s*\\(*\\s*\\w+\\s*\\)*\\s*([+/*%-]{1}\\s*\\(*\\s*\\w+\\s*\\)*\\s*)*;[ \\t]*$";
 		String regex_WHILE = "^\\s*while\\s*\\(\\s*\\w+\\s*(==|!=|<=|>=|<|>){1}\\s*\\w+\\)\\s*\\{?[ \\t]*$";
 		String regex_IF = "^\\s*if\\s*\\(\\s*\\w+\\s*(==|!=|<=|>=|<|>){1}\\s*\\w+\\)\\s*\\{?[ \\t]*$";
 		String regex_ELSE = "^\\s*else\\s*\\{?[ \\t]*$";
 		String regex_ASSIGNMENT = "^\\s*\\w+\\s*=\\s*\\(*\\s*\\w+\\s*\\)*\\s*([+/*%-]{1}\\s*\\(*\\s*\\w+\\s*\\)*\\s*)*;[ \\t]*$";
 		String regex_RETURN = "^\\s*return\\s+\\(*\\s*\\w+\\s*\\)*\\s*([+/*%-]{1}\\s*\\(*\\s*\\w+\\)*\\s*)*;[ \\t]*$";
 		
 		if (Pattern.matches (regex_FUNCTION_PROTOTYPE, input)) {
 			return Statement.FUNCTION_PROTOTYPE;
 		}
 		else if (Pattern.matches (regex_VARIABLE_DECLARATION, input)) {
 			return Statement.VARIABLE_DECLARATION;
 		}
 		else if (Pattern.matches (regex_VARIABLE_DEFINITION, input)) {
 			return Statement.VARIABLE_DEFINITION;
 		}
 		else if (Pattern.matches (regex_WHILE, input)) {
 			return Statement.WHILE;
 		}
 		else if (Pattern.matches (regex_IF, input)) {
 			return Statement.IF;
 		}
 		else if (Pattern.matches (regex_ELSE, input)) {
 			return Statement.ELSE;
 		}
 		else if (Pattern.matches (regex_ASSIGNMENT, input)) {
 			return Statement.ASSIGNMENT;
 		}
 		else if (Pattern.matches (regex_RETURN, input)) {
 			return Statement.RETURN;
 		}
 		else {
 			return Statement.MISC;
 		}
 	}
 	
 	public void analyze_FuncProto (String input, int lineNumber) { 		
 		int startingBracketAt = input.indexOf ('(');
 		int endingBracketAt = input.indexOf (')');
 		
 		if (startingBracketAt == -1 || endingBracketAt == -1) {
 			System.err.println ("Error: No Paranthesis Pair Found in FUNCTION_PROTOTYPE");
 			System.exit (0);
 		}
 		
 		String parameterString = input.substring (startingBracketAt +1, endingBracketAt).trim();
 		
 		if (parameterString.isEmpty()) {
 			return;
 		}
 		
 		String[] argumentStrings = parameterString.split (",");
 		for (int i = 0; i < argumentStrings.length; i++) {
 			String currentArg = argumentStrings[i].trim();
 			String variableName = currentArg.split(" ")[1].trim();
 			
 			VARIABLES.add (variableName);
 			TAINTED.put (variableName, lineNumber);
 		}
 	}
 	
 	public void analyze_VarDecl (String input, int lineNumber) {
 		// Removing ';' From SemiColon
 		int semicolonAt = input.indexOf (';');
 		input = input.substring (0, semicolonAt) + input.substring (semicolonAt +1);
 		
 		String[] splitOnSpace = input.split (" ");
 		
 		String variableName = splitOnSpace[1].trim();
 		VARIABLES.add (variableName);
 	}
 	
 	public void analyze_VarDef (String input, int lineNumber) {
 		// Removing ';' From SemiColon
 		int semicolonAt = input.indexOf (';');
 		input = input.substring (0, semicolonAt) + input.substring (semicolonAt +1);
 		
 		String[] splitOnEqualSign = input.split ("=");
 		
 		// Left Hand Side
 		String leftHandSide = splitOnEqualSign[0].trim();
 		String variableName = leftHandSide.split (" ")[1].trim();
 		VARIABLES.add (variableName);
 		
 		// Right Hand Side
 		String rightHandSide = splitOnEqualSign[1].trim();
 		String[] exprs = rightHandSide.split ("\\b[^a-zA-Z0-9_]+\\b");
 		
 		// Check Taintedness
 		for (int i = 0; i < exprs.length; i++) {
 			exprs[i] = exprs[i].trim();
 			try {
 				Double.parseDouble (exprs[i]);
 			}
 			catch (NumberFormatException nfeEx) {
 				if (TAINTED.containsKey (exprs[i])) {
 					TAINTED.put (variableName, lineNumber);
 				}
 			}
 		}
 	}
 	
 	public void analyze_While (String input, int lineNumber) {
 		// No Action Required
 	}
 	
 	public void analyze_If (String input, int lineNumber) {
 		// No Action Required
 	}
 	
 	public void analyze_Else (String input, int lineNumber) {
 		// No Action Required
 	}
 	
 	public void analyze_Assign (String input, int lineNumber) {
 		// Removing ';' From String
 		int semicolonAt = input.indexOf (';');
 		input = input.substring (0, semicolonAt) + input.substring (semicolonAt +1);
 		
 		String[] splitOnEqualSign = input.split ("=");
 		
 		// Left Hand Side
 		String variableName = splitOnEqualSign[0].trim();
 		
 		// Right Hand Side
 		String rightHandSide = splitOnEqualSign[1].trim();
 		String[] exprs = rightHandSide.split ("\\b[^a-zA-Z0-9_]+\\b");
 		boolean isRHSEvaluable = true;
 		boolean isTainted = false;
 		
 		// Check taintedness
 		for (int i = 0; i < exprs.length; i++) {
 			exprs[i] = exprs[i].trim();
 			try {
 				Double.parseDouble (exprs[i]);
 			}
 			catch (NumberFormatException nfeEx) {
 				isRHSEvaluable = false;
 				if (TAINTED.containsKey (exprs[i])) {
 					isTainted = true;
 					if (!TAINTED.containsKey (variableName))
 						TAINTED.put (variableName, lineNumber);
 				}
 			}
 		}
 		
 		if (isRHSEvaluable || !isTainted) {
 			// Not Tainted for sure
 			if (TAINTED.containsKey (variableName)) {
 				TAINTED.remove (variableName);
 			}
 		}
 	}
 	
 	public void analyze_Return (String input, int lineNumber) {
 		String input_c = new String (input);
 		
 		// Removing ';' From SemiColon
 		int semicolonAt = input_c.indexOf (';');
 		input_c = input_c.substring (0, semicolonAt) + input_c.substring (semicolonAt +1);
 		// Removing '(' and ')' From String
 		input_c = input_c.replace ("(", "");
 		input_c = input_c.replace (")", "");
 		
 		String returnExpr = input_c.split (" ")[1].trim();
 		String[] exprs = returnExpr.split ("\\b[^a-zA-Z0-9_]+\\b");
 		boolean isTainted = false;
 		for (int i = 0; i < exprs.length; i++) {
 			exprs[i] = exprs[i].trim();
 			if (!exprs[i].isEmpty()) {
	 			try {
	 				Double.parseDouble (exprs[i]);
	 			}
	 			catch (NumberFormatException nfeEx) {
	 				if (TAINTED.containsKey (exprs[i])) {
	 					isTainted = true;
	 				}
	 			}
 			}
 		}
 		
 		if (isTainted) {
 			System.out.println ("Potentially Tainted Expression \"" + input + "\" at return statement at line number: " + lineNumber);
 		}
 		else {
 			System.out.println ("Untainted Expression \"" + input + "\" at return statement at line number: " + lineNumber);
 		}
 	}
 	
 	public void analyze_Misc (String input, int lineNumber) {
 		// No Action Required
 	}
 	
 	public void printAnalysis () {
 		System.out.println ();
 		System.out.println ("ANALYSIS RESULT:");
 		
 		String[] Variables = VARIABLES.toArray (new String[VARIABLES.size()]);
 		System.out.println ("The Variables found in code snippet are:");
 		for (int i = 0; i < Variables.length; i++) {
 			System.out.print (Variables[i]);
 			if (i < Variables.length -1)
 				System.out.print (", ");
 		}
 		System.out.println ();
 		
 		Set<String> TaintedKeys = TAINTED.keySet();
 		System.out.println ("The Potentially Tainted Variables are:");
 		for (String Key: TaintedKeys) {
 			System.out.println ("<" + Key + ": " + TAINTED.get(Key) + ">");
 		}
 	}
}

enum Statement {
	FUNCTION_PROTOTYPE,
	VARIABLE_DECLARATION,
	VARIABLE_DEFINITION,
	WHILE,
	IF,
	ELSE,
	ASSIGNMENT,
	RETURN,
	MISC
}