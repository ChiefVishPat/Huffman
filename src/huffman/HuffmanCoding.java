package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
	    /* Your code goes here */
        sortedCharFreqList = new ArrayList<CharFreq>();     //initializing the charFreqList
        int[] occurences = new int[128];    //creating an array size of 128 for ASCII characters
        int fileSize = 0;       //keeps track of how many characters are in the file
        
        while(StdIn.hasNextChar()){     //this while loop creates an array of how often each character in the filename occurs
            char character = StdIn.readChar();
            occurences[character] = occurences[character] +1;
            fileSize++;
        }
        
        double p = 0.0;
        for(int i = 0; i < occurences.length; i++){
            if(occurences[i] > 0){
            char c = (char) i;
            p = 1.0*occurences[i]/fileSize;
            CharFreq newCharacter = new CharFreq(c, p);

            sortedCharFreqList.add(newCharacter);
            }
        }
        if(sortedCharFreqList.isEmpty()){
            return;
        }

        if(sortedCharFreqList.size() == 1){
            CharFreq freqs = sortedCharFreqList.get(0);
            char repeating = freqs.getCharacter();
            char nextRepChar = repeating;
            nextRepChar++;
            if(repeating == 127){
                nextRepChar = 0;
            }
            CharFreq temp = new CharFreq(nextRepChar, 0.0);
            sortedCharFreqList.add(temp);
        }

        Collections.sort(sortedCharFreqList);
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
	    /* Your code goes here */
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        Queue<TreeNode> dequeued = new Queue<TreeNode>();   //adding a third queue to make it easier to keep track of enqueue and dequeue nodes

        for(CharFreq c : sortedCharFreqList){                   //populating the source queue with charFreq object nodes
            TreeNode charNode = new TreeNode(c, null, null);
            source.enqueue(charNode);
        }

        while(!source.isEmpty() || target.size() != 1){      //you want to keep this running while the source queue size > 1 per instructions   
            /*dequeued has to be less than 2 nodes for the rest of the algorithm to work in our implementation */           
            while(dequeued.size() < 2){
                
                if(target.isEmpty()){   //if target empty, dequeue first node of source
                    dequeued.enqueue(source.dequeue());
                }
                else if(!source.isEmpty()){   //else compare the lowest prob nodes in source and target as long as source isn't empty
                    if(source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()){
                        dequeued.enqueue(source.dequeue());
                    } else dequeued.enqueue(target.dequeue());
                }
                else if(source.isEmpty()){    //this means that we will queue the node to dequeued in order to combine the nodes outside this while loop
                    dequeued.enqueue(target.dequeue());
                }
            }
            /*outside of the inner while loop but inside outer while loop*/
            TreeNode smallestNode, secondSmallestNode;

            if(dequeued.isEmpty()){
                smallestNode = new TreeNode();
            } else{
                smallestNode = dequeued.dequeue();
            }

            if(dequeued.isEmpty()){
                secondSmallestNode = new TreeNode();
            } else{
                secondSmallestNode = dequeued.dequeue();
            }

            //these 2 doubles will hold the smallest 2 frequencies
            double probOcc1;
            double probOcc2;

            if(smallestNode.getData() == null){
                probOcc1 = 0;
            } else probOcc1 = smallestNode.getData().getProbOcc();

            if(secondSmallestNode.getData() == null){
                probOcc2 = 0;
            } else probOcc2 = secondSmallestNode.getData().getProbOcc();

            TreeNode parent = new TreeNode(new CharFreq(null, probOcc1+probOcc2), smallestNode, secondSmallestNode);

            target.enqueue(parent);
        }

        huffmanRoot = target.dequeue();
    }

    //recursive helper method to make the encodings of each character in the huffman tree
    private void findCharacter(TreeNode currNode, String bitString, String[] encodings){
        if(currNode.getLeft() == null && currNode.getRight() == null){
            encodings[currNode.getData().getCharacter()] = bitString;
            return;
        }

        if(currNode!= null){
        findCharacter(currNode.getLeft(), bitString + "0", encodings);
        findCharacter(currNode.getRight(), bitString + "1", encodings);
        //return;
        }
        return;
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        /* Your code goes here */
        String bitString = "";
        encodings = new String[128];

        findCharacter(huffmanRoot, bitString, encodings);
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
	    /* Your code goes here */
        String encodedString = "";

        while(StdIn.hasNextChar()){
            encodedString += encodings[StdIn.readChar()];
        }

        writeBitString(encodedFile, encodedString);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
	    /* Your code goes here */
        String encodedString = readBitString(encodedFile);
        TreeNode ptr = huffmanRoot;

        for(int i = 0; i < encodedString.length(); i++){
            if(encodedString.charAt(i) == '0'){
                ptr = ptr.getLeft();
            } else{
                ptr = ptr.getRight();
            }

            if(ptr.getLeft() == null && ptr.getRight() == null){
                StdOut.print(ptr.getData().getCharacter());
                ptr = huffmanRoot;
            }
        }
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
