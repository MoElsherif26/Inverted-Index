import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.HashSet;
import java.util.Set;

public class InvertedIndex {
    private final HashMap<String, DictEntry> index = new HashMap<>();
    private Set<String> links = new HashSet<>();
    private static final int MAX_DEPTH = 2; // Maximum depth for crawling

    public void getPageLinks(String URL, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)

        if (!links.contains(URL)) {
            try {
                if (links.add(URL)) {
                    System.out.println(URL);
                }
                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get(); //jsoup jar to extract web
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth + 1);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    //build the inverted index for the 10 files
    public void buildInvertedIndex() throws IOException {
        for (int i = 1; i <= 10; ++i) {
            String l, f = "file" + i + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((l = br.readLine()) != null) {
                String[] terms = l.split("[^a-zA-Z\\d']+");
                for (String j : terms) {
                    j = j.toLowerCase();
                    if (j.length() > 0) {
                        DictEntry dictEntry = index.getOrDefault(j, new DictEntry());
                        dictEntry.term_freq++;
                        if (dictEntry.pList == null || dictEntry.pList.docId != i) {
                            Posting posting = new Posting();
                            posting.docId = i;
                            posting.dtf = 1;
                            posting.next = dictEntry.pList;
                            dictEntry.pList = posting;
                            dictEntry.doc_freq++;
                        } else {
                            dictEntry.pList.dtf++;
                        }
                        index.put(j, dictEntry);
                    }
                }
            }
        }
    }
    // list the files for terms
    public void listAllFiles() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a term to print it's inverted index: ");
        String term = scanner.nextLine().toLowerCase();
        DictEntry dictEntry = index.get(term);
        if (dictEntry != null) {
            System.out.print("The inverted index for the term (" + term + "): ");
            System.out.print("doc_freq = (" + dictEntry.doc_freq + "), ");
            System.out.print("term_freq = (" + dictEntry.term_freq + "), ");
            System.out.print("posting list = ");
            Posting posting = dictEntry.pList;
            ArrayList<Posting> arrayList = new ArrayList<>();
            while (posting != null) {
                arrayList.add(posting);
                posting = posting.next;
            }
            for (int i = arrayList.size() - 1; i >= 0; i--) {
                System.out.print("(file" + arrayList.get(i).docId + "[" + arrayList.get(i).dtf + "]) ");
            }
            System.out.println();
        } else {
            System.out.println("The term (" + term + ") doesn't exist in the files");
        }
    }

    public void getAQuery() {
        /*compute the cosine similarity between each file and the query*/
        double scoresArr[] = new double[10]; /*scores for every document*/
        double lengthsArr[] = new double[10]; /*Lengths of every document*/

        Scanner scanner = new Scanner(System.in);
        System.out.print("\n\n\nEnter your query to rank the files: ");
        String query = scanner.nextLine().toLowerCase();
        String[] queryTerms = query.split("\\W+");

        for (String term : queryTerms) {
            term = term.toLowerCase();
            DictEntry dictEntry = index.get(term);
            if (dictEntry != null) {
                int tdf = dictEntry.doc_freq; /*number of documents that contains the term */
                //int ttf = dictEntry.term_freq;
                double idf = Math.log10(10.0 / tdf); /*N = 10 (collection size)*/
                Posting posting = dictEntry.pList;
                while (posting != null) {
                    int docId = posting.docId;
                    int dtf = posting.dtf;
                    scoresArr[docId - 1] += (1 + Math.log10((double) dtf)) * idf;
                    lengthsArr[docId - 1] += Math.pow((1 + Math.log10((double) dtf)) * idf, 2);
                    posting = posting.next;
                }
            }
        }

        /*normalize scores by document lengths*/
        for (int i = 0; i < 10; i++) {
            if (lengthsArr[i] != 0) {
                scoresArr[i] /= Math.sqrt(lengthsArr[i]);
            }
        }

        /*Rank the documents based on the cosine similarity*/
        ArrayList<Integer> rankedDocs = new ArrayList<>();
        System.out.println("\nThe cosine similarity for each file for your query: ");
        for (int i = 0; i < 10; i++) {
            double maxScore = -1;
            int maxIndex = -1;
            for (int j = 0; j < 10; j++) {
                if (!rankedDocs.contains(j) && scoresArr[j] > maxScore) {
                    maxScore = scoresArr[j];
                    maxIndex = j;
                }
            }
            if (maxIndex != -1) {
                rankedDocs.add(maxIndex);
                double cosineSimilarity = scoresArr[maxIndex];
                System.out.println("Cosine Similarity for File" + (maxIndex + 1) + ": " + cosineSimilarity);
            }
        }

        /*rank the documents*/
        System.out.println("\nRanked Files based on cosine similarity: ");
        int c = 1;
        for (int i : rankedDocs) {
            System.out.println("Rank " + c + ": File" + (i + 1));
            c++;
        }
    }
}