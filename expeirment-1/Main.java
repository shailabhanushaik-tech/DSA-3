import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

// Represents an individual Article
class Article {
    private final int id;
    private final String title;
    private final String content;
    private final int wordCount;

    public Article(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        // Count words by splitting on whitespace
        this.wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getWordCount() { return wordCount; }
}

// In-Memory Repository storing all loaded articles
class ArticleRepository {
    private final List<Article> articles = new ArrayList<>();

    public void store(Article article) {
        articles.add(article);
    }

    public void displayAll() {
        System.out.println("======================================");
        System.out.println("      TEXTHACK ARTICLE REPOSITORY     ");
        System.out.println("======================================");

        int totalWords = 0;

        for (Article article : articles) {
            System.out.println("-------------------------------------------");
            System.out.println("Article ID : " + article.getId());
            System.out.println("Title      : " + article.getTitle());
            System.out.println("Word Count : " + article.getWordCount());
            System.out.println("Content :");
            System.out.println(article.getContent());
            System.out.println("-------------------------------------------");

            totalWords += article.getWordCount();
        }

        System.out.println("\nRepository Statistics");
        System.out.println("----------------------");
        System.out.println("Total Articles Loaded : " + articles.size());
        System.out.println("Total Words           : " + totalWords);
    }
}

// Loader class responsible for reading files from disk
class CorpusLoader {
    private final Path folderPath;

    public CorpusLoader(String dirPath) {
        this.folderPath = Paths.get(dirPath);
    }

    public void load(ArticleRepository repo, int startingId) {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            System.out.println("Error: Directory '" + folderPath + "' not found.");
            return;
        }

        try (Stream<Path> paths = Files.list(folderPath)) {
            // Filter .txt files and sort them by filename
            List<Path> txtFiles = paths
                    .filter(path -> path.toString().endsWith(".txt"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();

            int currentId = startingId;

            for (Path filePath : txtFiles) {
                // Read all non-empty lines from the file
                List<String> lines = Files.readAllLines(filePath)
                        .stream()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();

                if (!lines.isEmpty()) {
                    String title = lines.get(0); // Line 1 is the Title
                    String content = String.join("\n", lines.subList(1, lines.size())); // Rest is Content

                    repo.store(new Article(currentId, title, content));
                    currentId++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading corpus directory: " + e.getMessage());
        }
    }
}

// Main Driver Class
public class Main {
    public static void main(String[] args) {
        ArticleRepository repo = new ArticleRepository();
        
        // Pass the path to your Corpus directory
        CorpusLoader loader = new CorpusLoader("Corpus");
        
        // Load articles starting with ID 101
        loader.load(repo, 101);

        // Display formatted repository output
        repo.displayAll();
    }
}