/*
(Mohamed Omar Ahmed, 20201154)
(Ahmed Mostafa Sharaf, 20200051)
(Ahmed Yasser Ahmed, 20200061)
*/
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        //build the inverted for the 10 files
        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.buildInvertedIndex();
        //invertedIndex.listAllFiles();
        //invertedIndex.getAQuery();
        invertedIndex.getPageLinks("https://example.com", 0);
    }
}