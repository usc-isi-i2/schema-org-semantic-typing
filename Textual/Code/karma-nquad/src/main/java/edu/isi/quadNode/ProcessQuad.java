package edu.isi.quadNode;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



//import GenerateSemanticLabelsFromNQuads.QuadNode;



public class ProcessQuad {
	
	private BufferedReader br = null;
	private FileReader fr = null;
	private static final String LITERAL_SEPARATOR = "\n";
	private static final String SEMANTIC_LABEL_SEPARATOR = "\n";
	
	
	private String currLine = null;
	
	private QuadNode currNode = null;
	//private QuadNode prevNode = null;
	
	private String currType = null;
	
	
	
	ProcessQuad(String filename){
		
		try {
			fr = new FileReader(new File(filename));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		br = new BufferedReader(fr);		
	}
	
	//****************************
	
	
public boolean isObjectLiteral(String object){
	//System.out.println(object);
		if(object.contains("\"")){
			return true;
		}
		else
			return false;
		
	}

	public boolean isObjectURL(String object){
		object=sanitizeObject(object);
		if(object.startsWith("http://")){
			return true;
		}
		else 
			return false;
		
	}

	private String removeQuestionMarks(String s){
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for(int i = 0;i<len;i++){
			char c = s.charAt(i);
			if(c != '?'){
				sb.append(c);
			}
		}
		
		String returnString = sb.toString();
		
		if(returnString != null && returnString.trim().equals("")){
			return null;
		}
		
		return returnString;
		
		
	}
	
	
	
	public String sanitizeObject(String literal){
		int firstIndex=literal.indexOf("\"");
		
		int lastIndex=literal.lastIndexOf("\"");
		if(firstIndex==-1 || lastIndex==-1){
			return literal;
		}
		int endIndex=literal.length();
		if(literal.length() - 1 > lastIndex){
			
			if(!literal.substring(lastIndex+1, endIndex).contains("@en")){
				System.out.println("--------------->"+literal+"\n");
				return null;
				
			}
		}
		//System.out.println(literal+firstIndex+"  "+lastIndex);
		String cleanedLiteral=null;
		try{
			cleanedLiteral = literal.substring(firstIndex+1, lastIndex).toString();
			
			if(cleanedLiteral.equalsIgnoreCase("null")){
				/**
				 * Important: No point in training over null data.
				 * This not only improves the accuracy, but it also saves time.
				 * 
				 */
				return null;
			}
			
			cleanedLiteral = removeQuestionMarks(cleanedLiteral);		
			
		
		}catch(Exception e){
			return null;
			//System.out.println("$$$$$$$$$\n"+literal+"$$$$$$$$$$$$$$$$$$$$\n");
		}
		return cleanedLiteral;
		
	}
	
	
	
	public String getSemanticLabelForObject(String classType, String dataProperty){
		// Appending the class type and data property to print the semantic label
		classType=sanitizeClassType(classType);
		dataProperty = sanitizeDataProperty(dataProperty);
		return "label:" + classType +"|" + dataProperty;
	}
	
	

	
	public String sanitizeClassType(String classType){
		// Edit to sanitize classType to be printed in semantic label
		return classType;
	}
	
	public String sanitizeDataProperty(String dataProperty){
		// Edit to sanitize data Property to be printed in semantic label
		return dataProperty;
	}
	
	
	public String getSemanticLabelForQuadNode(String classType, QuadNode qNode){
		
		String predicate = qNode.getQuadPredicate();
		String object = qNode.getQuadObject();		
		//String subject = qNode.getQuadSubject();
		//String cleanedObject = null; 
		String dataProperty = null;
		String semanticLabel = null;
		
		if(isObjectLiteral(object)||isObjectURL(object)){
			
			//cleanedObject = sanitizeObject(object);
			dataProperty = predicate;
			semanticLabel = getSemanticLabelForObject(classType, dataProperty);
			return semanticLabel;
			
			
		}
	
		
		return null;
	}
	
	
	public String getFileNameForSemanticLabel(String classType, QuadNode qNode){
		
		
		String fileName=null;
		String fileClassLabelTokens[] = classType.split("/");
		String fileClassLabel = fileClassLabelTokens[fileClassLabelTokens.length-1];
		
		String dataProperty=qNode.getQuadPredicate();
		String filePropertyLabelTokens[] = dataProperty.split("/");
		String filePropertyLabel = filePropertyLabelTokens[filePropertyLabelTokens.length-1];
		
		fileName= fileClassLabel + "_" + filePropertyLabel + ".txt";
		
		
		
		return fileName;
	}
	
	public void addSemanticLabelToFile(String outputDir, String file, String semanticLabel, String literal){
	
		
		String fileName = outputDir + file ;
		try {
			FileWriter fw = new FileWriter(new File(fileName),true);
			BufferedReader br = new BufferedReader(new FileReader(fileName));     
			if (br.readLine() == null) {
				fw.append(semanticLabel+ProcessQuad.SEMANTIC_LABEL_SEPARATOR);
				
			}
			fw.append(literal+ProcessQuad.LITERAL_SEPARATOR);
			fw.close();
			br.close();
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	public void generateSemanticLabel(String outputDir, QuadNode qNode, String classType){
		
		String semanticLabel = getSemanticLabelForQuadNode(classType, qNode);
		String fileName = getFileNameForSemanticLabel(classType, qNode);
				String literal = null;
		String object= qNode.getQuadObject();
		String predicate=null;
		if(semanticLabel!=null){
			
			//System.out.println("Object: "+object);
			predicate = qNode.getQuadPredicate();
			literal=sanitizeObject(object);
			if(literal!=null){
				if(!predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
						
							addSemanticLabelToFile(outputDir,fileName, semanticLabel, literal);
						
						
				}
			}
		}
		
		
		
	}
	
	
	
	
	
	
	
	//***************************
	
	
	
	
	
	private void callProcessingFunctions(String outputDir, QuadNode qNode,String classType){
		//System.out.println(  "Obj:" + qNode.getQuadObject() + "\n");
		generateSemanticLabel(outputDir, qNode, classType);
		
		
	}
	
	private boolean isValidSubject(QuadNode qn){
		String subject = qn.getQuadSubject();		
		if(subject.startsWith("_:g")){			
			return true;
		}
		return false;
	}
	
	/**
	 * Added on 6th July
	 * For Checking the Type Property
	 * @param predicate
	 * @return
	 */
	/*
	private boolean isDataPropertyTypeProperty(String predicate){
		boolean isType = false;
		if(predicate.endsWith("#type")){
			isType = true;
		}
		return isType;
	}
	*/
	
	public void processFile(String outputDir) throws IOException{
		
		
		/*
		//Base Case Initialization.
		//This is the beginning of the file
		currLine = br.readLine();
		
		boolean ignoreUrlsWhichAreSubjects = true;
		
		while(currLine!=null){
			//System.out.println(currLine);
			if(prevNode == null){
				//This means beginning of the file
				currNode = new QuadNode(currLine);
				
				if(isDataPropertyTypeProperty(currNode.getQuadPredicate())){
					currType = currNode.getQuadObject();
				}else{
					currLine = br.readLine();
					prevNode = currNode;
					
					if(currLine!=null){
						currNode = new QuadNode(currLine);
					}else{
						currLine = br.readLine();
					}
					continue;
				}
				
				
				if(ignoreUrlsWhichAreSubjects){
					
					if(isValidSubject(currNode)){
						callProcessingFunctions(outputDir,currNode,currType);
					}
					
				}else{
					callProcessingFunctions(outputDir,currNode,currType);
				}
				
				
				/*
				if(isValidSubject(currNode)){
					callProcessingFunctions(outputDir,currNode,currType);
				}
				*/
			
			
			/*
				currLine = br.readLine();
				prevNode = currNode;
				
				if(currLine!=null){
					currNode = new QuadNode(currLine);
				}else{
					currLine = br.readLine();
				}
				
			}else{
				if(!currNode.getQuadSubject().equals(prevNode.getQuadSubject())){
					
					if(isDataPropertyTypeProperty(currNode.getQuadPredicate())){
						currType = currNode.getQuadObject();
					}else{
						prevNode = currNode;
						currLine = br.readLine();
						if(currLine == null){
							break;
						}
						
						currNode = new QuadNode(currLine);
						continue;
					}
					
				}
				//Call the respective functions here.
				//You have the current node and the type of the current node
				if(isValidSubject(currNode)){
					callProcessingFunctions(outputDir,currNode,currType);
				}
				
				
				prevNode = currNode;
				currLine = br.readLine();
				if(currLine == null){
					break;
				}
				
				currNode = new QuadNode(currLine);
			}
			
			
			
		}
		
		*/
		
		String predicate=null;
        currLine = br.readLine();
        while(currLine !=null){
            currNode = new QuadNode(currLine);
            if(isValidSubject(currNode)){
                predicate = currNode.getQuadPredicate();
                String predicateTokens[] = predicate.split("/");
                if(predicateTokens.length - 2 > 0){
                    currType= predicateTokens[predicateTokens.length-2];
                
                    callProcessingFunctions(outputDir,currNode,currType);
            }
                
            }
            currLine = br.readLine();
        
        }
		br.close();
		fr.close();
	}
	
	public static void generateSemanticLabelAndContent(File file,String outputDir) {
		
		
		
		
			ProcessQuad pq = new ProcessQuad(file.getAbsolutePath());
			try {
				pq.processFile(outputDir);
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
		
		
		
		
	}
	
	/*
	public static void main(String[] args) {
		
		//String dirName = "/home/pranav/Documents/usc/understanding schema.org/data2/canal_data/";
		//String dirName = "/home/pranav/Documents/usc/understanding schema.org/data2/hospital/";
		//String dirName ="/home/pranav/Documents/usc/understanding schema.org/data2/event_small";
		//String dirName =  "/home/pranav/Documents/usc/understanding fschema.org/data2/URL_AS_SUBJECT/";
		//String dirName = "/home/pranav/Documents/usc/understanding schema.org/data2/ev_az";
		String dirName ="/home/pranav/Documents/usc/understanding schema.org/data2/event_small2";
		File directory = new File(dirName);
		File[] childList = directory.listFiles(); 
		//String filename = "ev_aa";
		//String file = dirName + filename;
		
		Arrays.sort(childList, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {			
				String name1 = o1.getName();
				String name2 = o2.getName();
				
				return name1.compareTo(name2);
			}
			
		});
		
			
		
		
		for(File file: childList){
			String outputDir="TrainingSet/";
			ProcessQuad pq = new ProcessQuad(file.getAbsolutePath());
			try {
				pq.processFile(outputDir);
			} catch (IOException e) {
	
				e.printStackTrace();
				
			}
		}
		
		
	}
	*/
	

}