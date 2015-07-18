package edu.isi.quadNode;

public class QuadNode {
	
	private String quadSubject;
	private String quadObject;
	private String quadPredicate;
	private String quadSource; // The fourth thing in the quad array	
	
	public QuadNode(String quad){
		//Here the major code for breaking the quad into its respective things goes
				
		
		char whitespace_char = ' ';
		char doubleQuote_char = '"';
		char angleStart_char = '<';
		char underscore_char = '_';
		char backSlash_char = '\\';
		
		char[] quad_arr = quad.toCharArray();

		int subjectStarts = 0,subjectEnds;
		int objectStarts,objectEnds;
		int predicateStarts,predicateEnds;
		int sourceStarts,sourceEnds;
		
		for(int i = 0;i<quad_arr.length;i++){
			
			try{
				
				//Building Subject
				while(quad_arr[i]!=whitespace_char){
					i++;
				}
				subjectEnds = i;
				i++;
				predicateStarts = i;
				quadSubject = quad.substring(subjectStarts, subjectEnds);
				
				
				//BuildingPredicate;
				while(quad_arr[i]!=whitespace_char){
					i++;
				}
				predicateEnds = i;
				i++;
				objectStarts = i;
				quadPredicate = quad.substring(predicateStarts, predicateEnds);
				
				//Building Object
				if(quad_arr[objectStarts] == doubleQuote_char){
					i++;
					while(quad_arr[i]!=doubleQuote_char || (quad_arr[i]==doubleQuote_char && quad_arr[i-1]==backSlash_char)){
						//char x = quad_arr[i];
						i++;
					}						
					
					
					while(quad_arr[i]!=whitespace_char){
						i++;
					}
					objectEnds=i;
					i++;
					sourceStarts = i;
					
				}else if(quad_arr[objectStarts] == angleStart_char || quad_arr[objectStarts] == underscore_char){
					while(quad_arr[i]!=whitespace_char){
						i++;
					}
					objectEnds = i;
					i++;
					sourceStarts = i;
				}else {
					objectEnds = quad_arr.length;
					sourceStarts = quad_arr.length-1;
					i=quad_arr.length;
					quadObject = quad.substring(objectStarts, objectEnds);
					/*
					System.out.println("\n-----------------------------------\n");
					System.out.println(this);
					System.out.println("\n-----------------------------------\n");
					*/
					
					break;
					
				}				
				quadObject = quad.substring(objectStarts, objectEnds);
				
				while(quad_arr[i]!=whitespace_char){
					i++;
				}
				sourceEnds = i;
				i++;
				quadSource = quad.substring(sourceStarts, sourceEnds);
				
				
				//quadSubject = removeAngleBrackets(quadSubject);
				quadObject = removeAngleBrackets(quadObject);
				quadPredicate = removeAngleBrackets(quadPredicate);
				quadSource = removeAngleBrackets(quadSource);
				

				
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println("\nEXCEPTION:\n"+quad+"\n");
			}
							
		}
	}
	
	private String removeAngleBrackets(String s){
		String s2 = null;
		if(s.startsWith("<") && s.endsWith(">")){
			s2 = s.substring(1, s.length()-1);
			return s2;
		}
		return s;
	}

	public String getQuadSubject() {
		
		return quadSubject;
	}

	public String getQuadObject() {
		return quadObject;
	}

	public String getQuadPredicate() {
		return quadPredicate;
	}

	public String getQuadSource() {
		return quadSource;
	}
	
	public String toString(){
		String s = "Subject:"+getQuadSubject()+"\nPredicate:"+getQuadPredicate()+"\nObject:"+getQuadObject()+"\nSource:"+getQuadSource()+"\n";
		return s;
	}
}


