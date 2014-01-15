/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class IntellisenseItem{
	
	public static class CompletionPositions{
		
		@XmlAttribute
		private	String start;
		
		@XmlAttribute
		private	String end;
		
		public int getStart() {
			return Integer.parseInt(start);
		}

		public void setStart(String start) {
			this.start = start;
		}
		
		
		public int getEnd() {
			return Integer.parseInt(end);
		}

		public void setEnd(String end) {
			this.end = end;
		}
		
	}

	@XmlElement(name = "completion")
	private	CompletionPositions completion;
	
	public static class MatchPositions{
		
		@XmlAttribute
		private	String start;
		
		@XmlAttribute
		private	String end;
		
		public int getStart() {
			return Integer.parseInt(start);
		}

		public void setStart(String start) {
			this.start = start;
		}
		
		
		public int getEnd() {
			return Integer.parseInt(end);
		}

		public void setEnd(String end) {
			this.end = end;
		}
		
	}

	@XmlElement(name = "match")
	private	MatchPositions match;
	
	@XmlElement(name = "option")
	private String option;
	
	@XmlElement(name = "caret")
	private String caret;

	public String getOption() {
		return option;
	}
	
	@XmlElement(name = "prefix")
	private String prefix;
	
	public String getPrefix() {
		return prefix;
	}

	@XmlElement(name = "suffix")
	private String suffix;
	
	public String getSuffix() {
		return suffix;
	}
	
	public String getFullOption(){
		String fullOption = new String();
		if(option != null){
			fullOption = option;
			if(prefix != null){
				fullOption = prefix + fullOption;
			}
			if(suffix != null){
				fullOption = fullOption + suffix;
			}
		}
		
		return fullOption;
	}


	public CompletionPositions getCompletionPositions() {
		return completion;
	}

	public void setCompletionPositions(CompletionPositions completionPositions) {
		this.completion = completionPositions;
	}
	
	public MatchPositions getMatchPositions() {
		return match;
	}

	public void setMatchPositions(MatchPositions matchPositions) {
		this.match = matchPositions;
	}

	public String getCaret() {
		return caret;
	}
}
