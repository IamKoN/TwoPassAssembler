import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;

//  Storing my names as enums for comparison.
public class Assembler {
    //Data and instruction memory size
    final int MAX_DATA = 65536;
    final int MAX_CODE = 65536;

    public static String[] opcodes = { "HALT", "PUSH", "RVALUE", "LVALUE", "POP",
        "STO", "COPY", "ADD", "SUB", "MPY", "DIV", "MOD", "NEG", "NOT", "OR",
        "AND", "EQ", "NE", "GT", "GE", "LT", "LE", "LABEL", "GOTO", "GOFALSE",
        "GOTRUE", "PRINT", "READ", "GOSUB", "RET" };
    public static FileOutputStream writer = null;

    public static void main(String[] args) throws IOException {
        try { writer = (new FileOutputStream("output.bin"));
        } catch (Exception e) { }

        Scanner sc = new Scanner(System.in);
        String filename;
        // symtab is a list of symbols and an array list
        List<SymbolTable> symTab = new ArrayList<>();

        // what the filename will be written to
        if (args.length != 0) { filename = args[0];
        } else { 
            System.out.print("Enter file name to open: ");
            filename = sc.nextLine();
        }
        // Open file for input
        Scanner infile = new Scanner(new File(filename));

        // First Pass: build symbol table
        pass1(infile, symTab);
        dumpSymbolTable(symTab);
        infile.close();

        // Second Pass: reopen source file, output binary code
        infile = new Scanner(new File(filename));
        pass2(infile, symTab);
        infile.close();

        // print symbol table
        dumpSymbolTable(symTab);

        // close writer
        writer.close();
        System.out.println("Done");
    }



    //First Pass
    public static void pass1(Scanner infile, List<SymbolTable> tab) {
        // initialize location counter, etc.
        int locationCounter = 0;
        String line;
        Scanner input;
        String lexeme;

        // find start of data section
        do {
            line = infile.nextLine();
            System.out.println(line);
            input = new Scanner(line);
        } while (!input.next().equalsIgnoreCase("Section"));
        if (!input.next().equalsIgnoreCase(".data")) {
            System.err.println("Error:  Missing 'Section .data' directive");
            System.exit(1);
        } else {
            System.out.println("Parsing data section, pass 1");
        }

        // build symbol table from variable declarations
        line = infile.nextLine();
        input = new Scanner(line);

        // data section ends where code section begins
        while (!(lexeme = input.next()).equalsIgnoreCase("Section")) {
            // look for labels (they end with a colon)
            int pos = lexeme.indexOf(':');
            if (pos > 0) {
                    lexeme = lexeme.substring(0, pos);
            } else {
                    System.err.println("error parsing " + line);
            }
            // insert the lexeme, the type, and its address into the symbol table
            tab.add(new SymbolTable(lexeme, "Int", locationCounter));
            locationCounter++;
            line = infile.nextLine();
            input = new Scanner(line);
        }

        // Now, parse the code section, looking for the label directive
        System.out.println("Parsing code section, pass 1");
        locationCounter = 0;
        while (infile.hasNext()) {
            line = infile.nextLine();
            input = new Scanner(line);
            lexeme = input.next();
            // when a label is found, place it and its code offset in the symbol table
            if (lexeme.equalsIgnoreCase("label")) {
                lexeme = input.next();
                // tab.addNewSymbol(lexeme,"Code",locationCounter);
                tab.add(new SymbolTable(lexeme, "Code", locationCounter));
            }
            locationCounter++;
        }
    }

    //Second Pass: generate the code
    public static void pass2(Scanner infile, List<SymbolTable> tab) {
        // initialize location counter
        int locationCounter = 0;
        String line;
        Scanner input;
        String lexeme;
        int symTabPtr;
        SymbolTable entry;
        final int NULL = -1;
        // find start of next section
        do {
                line = infile.nextLine();
                input = new Scanner(line);

        } while (!input.next().equalsIgnoreCase("Section"));
        if (!input.next().equalsIgnoreCase(".data")) {
            System.err.println("Error:  Missing 'Section .data' directive");
            System.exit(1);
        } else { System.out.println("Parsing data section, pass 2");}
        line = infile.nextLine();
        input = new Scanner(line);

        while (!(lexeme = input.next()).equalsIgnoreCase("Section")) {
            // data section has been processed in previous pass, so skip this
            line = infile.nextLine();
            input = new Scanner(line);
        }

        // Generate code
        System.out.println("Parsing code section, pass 2");
        locationCounter = 0;
        // while not end of file keep parsing
        while (infile.hasNext()) {
            line = infile.nextLine();
            input = new Scanner(line);
            lexeme = input.next();
            int ptr;
            //	looUpLexeme opcode and generate appropriate instructions
            // int opcode = lookUpOpcode(lexeme);
            OpcodeType opcode = OpcodeType.values()[lookUpOpcode(lexeme)];
            // System.out.println("debug: " + opcode);
            switch (opcode) {
            case HALT:///////
            case POP:
            case STO:///////
            case COPY:
            case ADD://////
            case SUB://////
            case MPY://////
            case DIV://////
            case MOD:
            case NEG:
            case NOT:
            case OR:
            case AND:
            case EQ:
            case NE:
            case GT:
            case GE:
            case LT:
            case LE:
            case LABEL://////////
            case PRINT://///////
            case READ:
            case RET:
                insertCode(locationCounter, opcode.ordinal());
                break;
            case PUSH:
                lexeme = input.next();
                insertCode(locationCounter, opcode.ordinal(), Integer.parseInt(lexeme));
                break;
            case RVALUE: ///////
            case LVALUE: ///////
            case GOTO:
            case GOFALSE: //////
            case GOTRUE:
            case GOSUB:
                lexeme = input.next();
                // System.out.println("lexeme:" + lexeme);
                ptr = looUpLexeme(tab, lexeme);
                insertCode(locationCounter, opcode.ordinal(), tab.get(ptr).address);
                break;
            default:
                System.err.println("Unimplemented opcode:  " + opcode);
                System.exit(opcode.ordinal());
            }
            locationCounter++;
        }
    }
    
    public static void dumpSymbolTable(List<SymbolTable> tab) {
        System.out.println("\nlexeme\t|\ttype\t|\taddress\n--------|---------------|--------------");
        for (int i = 0; i < tab.size(); i++) {
                System.out.println(tab.get(i));
        }
    }
    
    public static int looUpLexeme(List<SymbolTable> symtab, String lexeme) {
        // loop through symtab and try to match a lexeme
        for (int i = 0; i < symtab.size(); i++) {
            if (symtab.get(i).lexeme.equals(lexeme)) {
                    return i;
            }
        }
        return -1;
    }
    
    // figures out where in the opcode string list the searched opcode is
    public static int lookUpOpcode(String s) {
        for (int i = 0; i < opcodes.length; i++) {
            if (s.equalsIgnoreCase(opcodes[i])) {
                return i;
            }
        }
        System.err.println("\nInvalid opcode:" + s);
        return -1;
    }

    public static void insertCode(int loc, int opcode, int operand) {
        System.out.println(Integer.toString(opcode) + "\t" + Integer.toString(operand));
        try {
            writer.write(toBytes(opcode), 2, 2);
            writer.write(toBytes(operand), 2, 2);
        } catch (Exception err) {
            // System.out.println(err.${getMessage()});
        }
    }

    public static void insertCode(int loc, int opcode) {insertCode(loc, opcode, 0);}

    

    static byte[] toBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);
        return result;
    }
}
