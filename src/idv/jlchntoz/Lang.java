package idv.jlchntoz;

import java.util.Locale;

public class Lang {
	
	public static LangParent refresh(){
		Locale locale = Locale.getDefault();  
		String l=locale.getLanguage().toLowerCase();  
		String c=locale.getCountry().toLowerCase();  
		if(l.equalsIgnoreCase("zh")){
			if(c.equalsIgnoreCase("cn")){
				//zh_cn();
				return new LangChineseSimp();
			}
			//zh();
			return new LangChineseTran();
		}
		//en();
		return new LangParent();
	}



}
