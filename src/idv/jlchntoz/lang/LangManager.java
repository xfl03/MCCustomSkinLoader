package idv.jlchntoz.lang;

import java.util.Locale;

public class LangManager {
	public static String langCode="en";
	public static ILanguage getLanguage(){
		Locale locale = Locale.getDefault();  
		String l=locale.getLanguage().toLowerCase();  
		String c=locale.getCountry().toLowerCase();  
		if(l.equalsIgnoreCase("zh")){
			if(c.equalsIgnoreCase("cn")){
				//zh_cn
				langCode="zh_cn";
				return new ChineseSimpLang();
			}
			//zh
			langCode="zh";
			return new ChineseTranLang();
		}
		//en
		langCode="en";
		return new EnglishLang();
	}



}
