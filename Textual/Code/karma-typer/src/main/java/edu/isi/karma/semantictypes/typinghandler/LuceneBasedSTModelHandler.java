package edu.isi.karma.semantictypes.typinghandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.semantictypes.tfIdf.Indexer;
import edu.isi.karma.semantictypes.tfIdf.Searcher;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * This is the API class for the semantic typing module, implementing the
 * combined approach of TF-IDF based cosine similarity and Kolmogorov-Smirnov
 * test approaches for textual and numeric respectively by 
 * Ramnandan.S.K and Amol Mittal.
 * 
 * @author ramnandan
 * 
 */

public class LuceneBasedSTModelHandler implements ISemanticTypeModelHandler {

	static Logger logger = LoggerFactory
			.getLogger(LuceneBasedSTModelHandler.class.getSimpleName());
	private ArrayList<String> allowedCharacters;

	private boolean modelEnabled = false;
	private String indexDirectory;
	
	

	/**
	 * NOTE: Currently, TF-IDF based approach is used for both textual and
	 * numeric data due to bug in KS test on Apache Commons Math.
	 * 
	 * TODO: Integrate KS test when this bug is resolved :
	 * https://issues.apache.org/jira/browse/MATH-1131
	 */

	public LuceneBasedSTModelHandler() {
		allowedCharacters = allowedCharacters();
		indexDirectory = ServletContextParameterMap
				.getParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY);
	}
	
	public LuceneBasedSTModelHandler(String indexDirectory){
		this();
		if(!this.indexDirectory.equals(indexDirectory) || this.indexDirectory.equals("")){
			
			
			// Find a safe place to store preferences
			String karmaDir = System.getenv("KARMA_USER_HOME");
			if(karmaDir == null)
			{
				karmaDir = System.getProperty("KARMA_USER_HOME");
				if(karmaDir == null) {
					Preferences preferences = Preferences.userRoot().node("WebKarma");
					karmaDir = preferences.get("KARMA_USER_HOME",  null);
				}
			}
			if(karmaDir == null)
			{
				String defaultLocation = System.getProperty("user.home") + File.separator + "karma";
				logger.info("KARMA_USER_HOME not set.  Defaulting to " + defaultLocation);
				File newKarmaDir = new File(defaultLocation);
				karmaDir = newKarmaDir.getAbsolutePath() + File.separator;
				
			}
			if(!karmaDir.endsWith(File.separator))
			{
				karmaDir += File.separator;
			}

			//logger.info("Karma home: " + karmaDir);
			
			this.indexDirectory = karmaDir + "/" + indexDirectory;
			System.out.println(this.indexDirectory);

		}
	}

	/**
	 * Adds the passed list of examples for training
	 * 
	 * @param label
	 *            True label for the list of example.
	 * @param examples
	 *            List of example strings.
	 * @return True if success, else False
	 */
	@Override
	public synchronized boolean addType(String label, List<String> examples) {
		boolean savingSuccessful = false;

		// running basic sanity checks in the input arguments
		if (label == null || label.trim().length() == 0 || examples.size() == 0) {
			logger.warn("@label argument cannot be null or an empty string and the @examples list cannot be empty.");
			return false;
		}
		
		label = label.trim();
		ArrayList<String> cleanedExamples = new ArrayList<String>();
		cleanedExamplesList(examples, cleanedExamples);
		
		// making sure that the condition where the examples list is not empty
		// but contains junk only is not accepted
		if (cleanedExamples.size() == 0) {
			logger.warn("@examples list contains forbidden characters only. The allowed characters are "
					+ allowedCharacters);
			return false;
		}


		// if the column is textual
		try {
			savingSuccessful = indexTrainingColumn(label, cleanedExamples);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return savingSuccessful;
	}

	/**
	 * Indexes the given training column for a specific label
	 * 
	 * @param label
	 * @param selectedExamples
	 * @return
	 * @throws IOException
	 */
	private boolean indexTrainingColumn(String label,
			ArrayList<String> selectedExamples) throws IOException {
		/**
		 * @Patch Applied
		 * @author pranav and aditi
		 * @date 12th June 2015
		 */
		// treat content of column as single document
		StringBuilder sb = new StringBuilder();
		for (String ex : selectedExamples) {
			sb.append(ex);
			sb.append(" ");
		}

		// check if semantic label already exists
		Document labelDoc = null; // document corresponding to existing semantic label if exists
		if (indexDirectoryExists()) {
			try {
				// check if semantic label already exists in index
				Searcher searcher = new Searcher(indexDirectory,
						Indexer.LABEL_FIELD_NAME);
				try {
					labelDoc = searcher.getDocumentForLabel(label);
				} finally {
					searcher.close();
				}
			} catch (Exception e) {
				// Ignore, the searcher might not work if index is empty.
			}
		}

		// index the document
		Indexer indexer = new Indexer(indexDirectory);
		try {
			indexer.open();
			if (labelDoc != null) {
				/*
				IndexableField existingContent = labelDoc.getField(Indexer.CONTENT_FIELD_NAME);
				indexer.updateDocument(existingContent, sb.toString(), label);
				*/
				
				IndexableField[] existingContent = labelDoc.getFields(Indexer.CONTENT_FIELD_NAME);
				indexer.updateDocument(existingContent, sb.toString(), label);
			} else {
				indexer.addDocument(sb.toString(), label);
			}
			indexer.commit();
		} finally {
			indexer.close();
		}

		return true;
	}

	/**
	 * Check if index directory exists and contains files
	 * 
	 * @return
	 */
	private boolean indexDirectoryExists() {
		File dir = new File(indexDirectory);

		if (dir.exists() && dir.listFiles().length > 0) {
			String[] files = dir.list();
			for (String file : files) {
				if (file.equals("segments.gen"))
					return true;
			}
		}
		return false;
	}

	/**
	 * @param examples
	 *            - list of examples of an unknown type
	 * @param numPredictions
	 *            - required number of predictions in descending order
	 * @param predictedLabels
	 *            - the argument in which the ordered list of labels is
	 *            returned. the size of this list could be smaller than
	 *            numPredictions if there aren't that many labels in the model
	 *            already
	 * @param confidenceScores
	 *            - the probability of the examples belonging to the labels
	 *            returned.
	 * @param exampleProbabilities
	 *            - the size() == examples.size(). It contains, for each
	 *            example, in the same order, a double array that contains the
	 *            probability of belonging to the labels returned in
	 *            predictedLabels.
	 * @param columnFeatures
	 *            - this Map supplies ColumnFeatures such as ColumnName, etc.
	 * @return True, if successful, else False
	 */
	@Override
	public List<SemanticTypeLabel> predictType(List<String> examples,
			int numPredictions) {

		if (!this.modelEnabled) {
			logger.warn("Semantic Type Modeling is not enabled");
			return null;
		}

		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive");
			return null;
		}

		logger.debug("Predic Type for " + examples.toArray().toString());
		// get top-k suggestions
		if (indexDirectoryExists()) {
			// construct single text for test column
			StringBuilder sb = new StringBuilder();
			for (String ex : examples) {
				/**
				 * @author pranav
				 * @patch applied on 10th July 2015
				 */
				/*
				if(ex.length() < 3){
					continue;
				}
				*/
				
				sb.append(ex);
				sb.append(" ");
			}
			
			
			
			/**
			 * @author pranav
			 * @date 10th July
			 * To make things faster and correct
			 */
			examples.clear(); //So as to save memory and avoid Out of memory exception
			String toPredictContent = sb.toString().trim();			
			if(toPredictContent == null || toPredictContent.length() ==0){
				return null;
			}
			sb = null;//So as to save memory and avoid Out of memory exception
			
			try {
				Searcher predictor = new Searcher(indexDirectory,
						Indexer.CONTENT_FIELD_NAME);
				try {
					List<SemanticTypeLabel> result = predictor.getTopK(numPredictions, toPredictContent);
					/**
					 * @author pranav
					 * @patch applied 
					 * If the result is null, if condition is not checked it results in an error
					 */
					if(result == null){
						return null;
					}
					logger.debug("Got " + result.size() + " predictions");
					return result;
				} finally {
					predictor.close();
				}
			} catch (ParseException | IOException e) {				
				e.printStackTrace();
				//return null;
			}
		}

		return null;
	}

	/**
	 * @return True if successfully cleared the model. False, otherwise. This
	 *         method removes all labels from the model.
	 * 
	 *         Currently, when only TF-IDF is used, equivalent to deleting all
	 *         documents
	 */
	@Override
	public boolean removeAllLabels() {

		try {
			Indexer indexer = new Indexer(indexDirectory);
			try {
				indexer.open();
				indexer.deleteAllDocuments();
				indexer.commit();
			} finally {
				indexer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * @param uncleanList
	 *            List of all examples
	 * @param cleanedList
	 *            List with examples that don't have unallowed chars and others
	 *            such as nulls or empty strings This method cleans the examples
	 *            list passed to it. Generally, it is used by other methods to
	 *            sanitize lists passed from outside.
	 */
	private void cleanedExamplesList(List<String> uncleanList,
			List<String> cleanedList) {
		cleanedList.clear();
		for (String example : uncleanList) {
			if (example != null) {
				String trimmedExample;
				trimmedExample = getSanitizedString(example);
				
				
				if (trimmedExample.length() != 0) {
					cleanedList.add(trimmedExample);
				}
				
				
				/**
				 * @author pranav
				 * @patch applied on 10th of July
				 * Any and all words which have length less than 2 are being discarded, this time
				 */
				/*
				if(trimmedExample.length() > 2){					
					cleanedList.add(trimmedExample);
				}else{
					System.out.println("\n Ignored Small String : " + trimmedExample);
				}
				*/
				
			}
		}
	}

	/**
	 * @param unsanitizedString
	 *            String to be sanitized
	 * @return sanitizedString
	 */
	private String getSanitizedString(String unsanitizedString) {
		String sanitizedString;
		sanitizedString = "";
		for (int i = 0; i < unsanitizedString.length(); i++) {
			String charAtIndex;
			charAtIndex = unsanitizedString.substring(i, i + 1);
			if (allowedCharacters.contains(charAtIndex)) {
				sanitizedString += charAtIndex;
			}
		}
		return sanitizedString;
	}

	/**
	 * @return Returns list of allowed Characters
	 */
	private ArrayList<String> allowedCharacters() {
		ArrayList<String> allowed = new ArrayList<String>();
		// Adding A-Z
		for (int c = 65; c <= 90; c++) {
			allowed.add(new Character((char) c).toString());
		}
		// Adding a-z
		for (int c = 97; c <= 122; c++) {
			allowed.add(new Character((char) c).toString());
		}
		// Adding 0-9
		for (int c = 48; c <= 57; c++) {
			allowed.add(new Character((char) c).toString());
		}
		allowed.add(" "); // adding space
		allowed.add("."); // adding dot
		allowed.add("%");
		allowed.add("@");
		allowed.add("_");
		allowed.add("-");
		allowed.add("*");
		allowed.add("(");
		allowed.add(")");
		allowed.add("[");
		allowed.add("]");
		allowed.add("+");
		allowed.add("/");
		allowed.add("&");
		allowed.add(":");
		allowed.add(",");
		allowed.add(";");
		allowed.add("?");
		/**
		 * @author pranav and aditi
		 * @patch added. Earlier it did not contain question mark and hash as the allowed characters
		 * We introduced them, as the schema.org data contains lot of hashes.
		 */
		allowed.add("\\");
		allowed.add("#");
		return allowed;
	}
	
	

	@Override
	public boolean readModelFromFile(String filepath) {
		indexDirectory = filepath;
		return true;
	}

	@Override
	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;

	}

}
