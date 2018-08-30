public class SymbolTable {
    String lexeme;
    String type;
    int address;

    public SymbolTable(String lexeme, String type, int address) {
        this.lexeme = lexeme;
        this.type = type;
        this.address = address;
    }

    public String toString(){
        return lexeme +"\t|\t"+ type +"\t|\t"+ address;
    }
}
