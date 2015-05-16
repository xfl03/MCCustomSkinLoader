package idv.jlchntoz.lang;

import java.util.Locale;

public class Language {
	
	public static DefaultLang getLanguage(){
		Locale locale = Locale.getDefault();  
		String l=locale.getLanguage().toLowerCase();  
		String c=locale.getCountry().toLowerCase();  
		if(l.equalsIgnoreCase("zh")){
			if(c.equalsIgnoreCase("cn")){
				//zh_cn
				return new ChineseSimpLang();
			}
			//zh
			return new ChineseTranLang();
		}
		//en
		return new DefaultLang();
	}



}
