import java.util.ArrayList;
/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static int Setting = 0;	// 0 Do nothing Setting, 1 Reset(clear dictionary when full) , 2 Monitor Mode (Watch Ratio)
    private static final int R = 256;       
    private static final int STARTING_WIDTH = 9;
    private static final int ENDING_WIDTH = 16;
    private static int W = STARTING_WIDTH;  
    private static int L = 512;       // initial 2^W  

    public static void compress() { 
        boolean currentlyMonitoring = false;
        BinaryStdOut.write(Setting, 2); //prints setting
        int bitLengthOut = 0; 
        int bitLengthIn = 0;        
        String input = BinaryStdIn.readString();
        TST<Integer> st = generateASCIIchar();
        int code = R+1;  // R is codeword for EOF
        float previousCompressionRatio = 1;
        float currentCompressionRatio = 0;
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            bitLengthOut = bitLengthOut + W; 
            bitLengthIn = bitLengthIn + (t * 8);              
            if (t < input.length()){    // Add s to symbol table.
                if ( code >= L){
                    boolean spaceLeft = increaseWidth(W);
                    if (!spaceLeft)	{
                        boolean resetCodebook = false;
                        if (Setting == 2){
                            currentCompressionRatio = bitLengthIn / (float)bitLengthOut;
                            if (!currentlyMonitoring){
                                currentlyMonitoring = true;    
                                previousCompressionRatio = currentCompressionRatio;        
                            }
                            else if (previousCompressionRatio / currentCompressionRatio > 1.1){
                                currentlyMonitoring = false;
                                resetCodebook = true;
                            }
                        } else if (Setting == 1){
                            resetCodebook = true;
                        }
                        if (resetCodebook){
                            code = R + 1;
                            st = generateASCIIchar();
                            resetWidth(); 
                        }
                    }
                }
                if ( code < L) {
                    st.put(input.substring(0, t + 1), code++);
                } 
            }    
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        boolean currentlyMonitoring = false;
        int bitLengthIn = 0;
        int bitLengthOut = 0;
        Setting = BinaryStdIn.readInt(2);
        ArrayList<String> st = generateASCIIcharForExpand();
        int i = R + 1;
        float previousCompressionRatio = 1;
        float currentCompressionRatio = 0;
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st.get(codeword);
        while (true) {
            bitLengthOut = bitLengthOut + W;
            bitLengthIn =bitLengthIn + (val.length() * 8);	
            if (i >= L){
            	boolean spaceLeft = increaseWidth(W);
            	if (!spaceLeft){
                    boolean resetCodebook = false;
                    if (Setting == 2){
                        currentCompressionRatio = bitLengthIn / (float)bitLengthOut;
                        if (!currentlyMonitoring) {
                            currentlyMonitoring = true;
                            previousCompressionRatio = currentCompressionRatio;
                        }
                        else if (previousCompressionRatio / currentCompressionRatio > 1.1){
                            currentlyMonitoring = false;                                
                            resetCodebook = true;
                        }
                    } else if (Setting == 1){
                        resetCodebook = true;
                    }
                    if (resetCodebook){
                        i = R + 1;    
                        st = generateASCIIcharForExpand();
                        resetWidth(); 
                    }
            	}
            }
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String output;
            if (i == codeword) output = val + val.charAt(0);   // special case hack
            else output = st.get(codeword);
            if (i < L){
            	st.add(val + output.charAt(0));
            	i++;
            }
            val = output;
        }
        BinaryStdOut.close();
    } 
    
    public static boolean increaseWidth(int width){
    	width++;
        if (width > ENDING_WIDTH){
            return false;
    	}else{
            W = width;
            L = (int)Math.pow(2, W);
            return true;
        }        
    }
    
    public static void resetWidth(){
        W = STARTING_WIDTH;
        L = (int)Math.pow(2, W);       
    }
    
    public static TST<Integer> generateASCIIchar(){
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++){
            st.put("" + (char) i, i);
        }    
        return st;
    }
    
     public static ArrayList<String> generateASCIIcharForExpand(){
    	ArrayList<String> st = new ArrayList<String>(65536);
        for (int i = 0; i < R; i++){
            st.add("" + (char)i);
        }    
        st.add("");
        return st;
    }
    
    public static void main(String[] args) {
        if(args[0].equals("-")){
            String setting = args[1];
            if (setting.equals("n")){ 
                Setting = 0;
            }
            else if (setting.equals("r")){
                Setting = 1;
            } 
            else if (setting.equals("m")){
                Setting = 2;
            } 
            compress();
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}