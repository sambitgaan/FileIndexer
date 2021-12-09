package rb.com.care.purge;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class PurgeDemo {

	static GetPurgeProperties properties;

	public static AtomicInteger threadsInWritingBlock = new AtomicInteger();
	public static AtomicInteger pendingCommits = new AtomicInteger();
	public static int PENDING_COMMIT_THRESHOLD = 1000;
    
	public static void main(String[] args) throws Exception {
		
		int swValue;
		properties = new GetPurgeProperties();
		properties.setProp();
		System.out.println("|   MENU SELECTION DEMO    |");
		System.out.println("| Options:                 |");
		System.out.println("|        1. To index data     |");
		System.out.println("|        2. To search files to be deleted     |");
		System.out.println("|        3. To delete searched files      |");
		System.out.println("|        4. Exit      |");
		swValue = Keyin.inInt(" Select option: ");

		switch (swValue) {
			case 1:
				startIndex();
				break;
			case 2:
				startSearch();
				break;
			case 3:
				startDelete();
				break;
			case 4:
				System.exit(0);
			default:
				System.out.println("Invalid selection");
				break;
		}
	}

	private static void startDelete() {
		System.out.println("Deleting files, please wait ...");
		Instant deleteStart = Instant.now();
		deleteFiles(properties.getSearchedFile(), properties.getDeleteLogFile());
		Instant deleteStop = Instant.now();
		Duration timeElapsedDelete = Duration.between(deleteStart, deleteStop);
		System.out.println("Time taken for deleting: " + timeElapsedDelete.getSeconds());
	}

	private static void startSearch() throws FileNotFoundException, IOException, ParseException {
		System.out.println("Searching files, please wait ...");
		Instant searchStart = Instant.now();
		String st;
		BufferedReader br1 = getFIleBufferedReader();
		BufferedWriter bw = getFileBufferedWriter();
		while ((st = br1.readLine()) != null) {
			searchIndex(st, bw);
		}
		br1.close();
		bw.close();
		Instant searchStop = Instant.now();
		Duration timeElapsedSearch = Duration.between(searchStart, searchStop);
		System.out.println("Time taken for searching: " + timeElapsedSearch.getSeconds());
	}

	public static void startIndex() throws IOException, FileNotFoundException, CorruptIndexException,
			LockObtainFailedException, ParseException {
		System.out.println("Indexing has been started, please wait ...");
		Instant start = Instant.now();
		BufferedReader br = getFIleBufferedReader();
		createIndex(br);
		br.close();
		Instant stop = Instant.now();
		Duration timeElapsed = Duration.between(start, stop);
		System.out.println("Time taken for indexing: " + timeElapsed.getSeconds());
	}

	private static void deleteFiles(String searchedFileStr, String logFileStr) {
		int filesDeleted = 0;
		int filesNotDeleted = 0;
		try {
			File searchedFile = new File(searchedFileStr);
			File logFile = new File(logFileStr);
			BufferedReader br = new BufferedReader(new FileReader(searchedFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(logFile));
			String fileName;
			while ((fileName = br.readLine()) != null) {
				if (Files.deleteIfExists(Paths.get(fileName))) {
					filesDeleted++;
					bw.write(fileName + " : deleted" + "\n");
				} else {
					filesNotDeleted++;
					bw.write(fileName + " : not deleted" + "\n");
				}
			}
			br.close();
			bw.write("Total files deleted: " + Integer.toString(filesDeleted) + "\n");
			bw.write("Total files not deleted: " + Integer.toString(filesNotDeleted) + "\n");
			bw.close();
		} catch(NoSuchFileException e) {
			filesNotDeleted++;
	        System.out.println("No such file/directory exists: " + searchedFileStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Files deleted: " + Integer.toString(filesDeleted) + " Files not deleted: " + Integer.toString(filesNotDeleted));
	}

	private static BufferedWriter getFileBufferedWriter() throws IOException {
		File filePath = new File(properties.getSearchedFile());
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
		return bw;
	}

	public static void createIndex(BufferedReader br) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
		File dataDirectory = new File(properties.getDataDirectory());
		IndexWriter iw = getIndexWriter(properties.getIndexDirectory());
		threadIndexDirectory(iw, dataDirectory);
		iw.commit();
		iw.close();
	}

	public static BufferedReader getFIleBufferedReader() throws IOException {
		File file = new File(properties.getInputFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		return br;
	}

	private static IndexWriter getIndexWriter(String indexDirectory) throws IOException {
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = getIndexDirectory(indexDirectory);
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setCommitOnClose(true);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		iwc.setMergeScheduler(new org.apache.lucene.index.SerialMergeScheduler());
		iwc.setRAMBufferSizeMB(1024);
		IndexWriter iw = new IndexWriter(index, iwc);
		return iw;
	}

	private static Directory getIndexDirectory(String dirPath) throws IOException {
		File idxDirectory = new File(dirPath);
		Directory index = FSDirectory.open(idxDirectory.toPath());
		return index;
	}

	private static void indexDirectory(IndexWriter iw, File dataDirectory) throws IOException, ParseException {
		File[] files = dataDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
					indexDirectory(iw, f);
			} else {
				indexFileWithIndexWriter(iw, f);
			}
		}
	}


	//Thread aproch
	private static void threadIndexDirectory(IndexWriter iw, File dataDirectory) throws IOException, ParseException {
		File[] files = dataDirectory.listFiles();
		List<File> listFIles = Arrays.asList(files);
		int chunkSize = (int) Math.ceil((double) files.length / 10);
		List<List<File>> checkedData = Lists.partition(listFIles, chunkSize); //List of list of files 1425/10 --> 142

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		// executor
//		for (int i = 0; i < checkedData.size(); i++) {
//			executor.submit(new IndexingExecutor(checkedData.get(i), i));
//		}

		// thread approch
		for (int i = 0; i < checkedData.size(); i++) {
			executor.submit(new Indexing(iw, checkedData.get(i), i));
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}


		//Merge Schduler
		ConcurrentMergeScheduler.mergeScheduler(new Object());
	}

	private static void indexFileWithIndexWriter(IndexWriter iw, File f) throws IOException, ParseException {
		if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
			return;
        }
        Document doc = new Document();
        addToIndex(iw, f, doc);
	}

	private static void addToIndex(IndexWriter iw, File f, Document doc) throws IOException, ParseException {
		doc.add(new LongPoint("id", f.hashCode()));
		doc.add(new Field("filename", f.getName(), TextField.TYPE_STORED));
		doc.add(new Field("filepath", f.getCanonicalPath(), TextField.TYPE_STORED));
		iw.addDocument(doc);
		threadsInWritingBlock.incrementAndGet();
		if (threadsInWritingBlock.decrementAndGet() == 0 || pendingCommits.incrementAndGet() > PENDING_COMMIT_THRESHOLD) {
			pendingCommits.set(0);
			iw.commit();
        }
	}

	private static boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

	private static void searchIndex(String searchString, BufferedWriter bw) throws IOException, ParseException {
		Directory directory = getIndexDirectory(properties.getIndexDirectory());
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher searcher = getSearcher(ireader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser("filename", analyzer);
		queryParser.setAllowLeadingWildcard(true);
		queryParser.setSplitOnWhitespace(true);
		Query query = queryParser.parse("*" + searchString + "*");
		ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
		printPath(searcher, hits, bw);
		ireader.close();
	}

	private static void printPath(IndexSearcher searcher, ScoreDoc[] hits, BufferedWriter bw) throws IOException {
		for(int i = 0; i < hits.length; ++i) {
		    int docId = hits[i].doc;
		    Document d = searcher.doc(docId);
		    String filePath = d.get("filepath");
		    bw.write(filePath + "\n");
		}
	}

	private static IndexSearcher getSearcher(DirectoryReader ireader) throws IOException {
		IndexSearcher searcher = new IndexSearcher(ireader);
		return searcher;
	}
}
